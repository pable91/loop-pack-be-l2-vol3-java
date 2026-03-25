# 이벤트 기반 아키텍처

## 1. Event vs Command

| | Event | Command |
|---|---|---|
| 의미 | "이런 일이 일어났다" (사실) | "이것을 해라" (명령) |
| 수신자 | 불특정 다수 (누가 듣든 상관없음) | 특정 대상 |
| 응답 | 기다리지 않음 | 응답을 기다림 |
| 예시 | `OrderConfirmedEvent` | `IssueCouponCommand` |

---

## 2. 왜 ApplicationEvent가 필요한가

### 현재 문제

`PaymentFacade.handleCallback()` 안에 모든 처리를 넣으면:

```java
payment.markSuccess(...)   // 결제 성공
order.confirm()            // 주문 확정
couponService.issue(...)   // 쿠폰 발급  ← 실패하면?
pointService.earn(...)     // 포인트 적립
notificationService.send() // 알림 발송
```

**문제 1**: 하나의 트랜잭션이므로 쿠폰 발급 실패 시 결제/주문까지 롤백된다.

**문제 2**: 모든 처리가 동기이므로 응답이 느려진다.

### 해결: ApplicationEvent로 분리

```java
payment.markSuccess(...)
order.confirm()
eventPublisher.publishEvent(new OrderConfirmedEvent(order.getId()))
// 여기서 트랜잭션 끝. 쿠폰/포인트/알림은 리스너에서 처리
```

쿠폰 발급이 실패해도 결제/주문에 영향이 없다.

### @EventListener의 한계

`ApplicationEvent`로 전환하면 코드 결합도는 낮아진다. 하지만 `@EventListener`는 동기 실행이므로 런타임 결합도는 여전하다.

| | 직접 호출 | `@EventListener` |
|---|---|---|
| 코드 결합도 | 높음 (OrderService가 CouponService를 import) | 낮음 (서로 모름) |
| 런타임 결합도 | 높음 | **여전히 높음** |

리스너에서 예외가 발생하면 발행자 트랜잭션까지 롤백된다.

```
OrderService.confirm() 트랜잭션 시작
  → order.confirm()        ✅
  → publishEvent()
       → couponService.issue() 💥 예외 발생
  ← 예외가 confirm()까지 전파
트랜잭션 롤백 → order.confirm()도 롤백됨
```

> `@EventListener`는 의존성 방향만 없앤 것. 같은 트랜잭션 안에서 동기 실행된다는 사실은 변하지 않는다.

→ 런타임 결합도를 끊으려면 `@TransactionalEventListener(AFTER_COMMIT)`이 필요하다.

---

## 3. 트랜잭션 커밋 타이밍 문제

### 문제

`publishEvent()`는 트랜잭션 커밋 **전**에 호출된다.

→ 리스너가 먼저 실행된 뒤 트랜잭션이 롤백되면, 결제는 실패했는데 쿠폰은 이미 발급된 정합성 이슈가 발생한다.

### 해결: @TransactionalEventListener

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handle(OrderConfirmedEvent event) {
    // 트랜잭션 커밋 성공 이후에만 실행
    couponService.issue(event.getOrderId());
}
```

커밋이 성공했을 때만 리스너가 실행된다.

---

## 4. 서버 장애 시 이벤트 소실 문제

### 문제

`@TransactionalEventListener`는 이벤트를 메모리에만 가지고 있다.

→ 커밋 직후 서버가 죽으면 이벤트가 사라진다.
→ 결제는 성공했는데 쿠폰이 영영 발급되지 않는다.

### 해결 예고: Transactional Outbox Pattern

이벤트를 DB에 저장(Outbox 테이블)해서, 서버가 죽어도 재처리할 수 있도록 보장한다.

```
트랜잭션 안에서:
  1. order.confirm()
  2. outbox 테이블에 이벤트 INSERT  ← DB에 저장되므로 유실 없음

트랜잭션 밖에서:
  3. Outbox 테이블 폴링 → Kafka 발행
```

---

## 5. @EventListener vs @TransactionalEventListener 구분

| | `@EventListener` | `@TransactionalEventListener(AFTER_COMMIT)` |
|---|---|---|
| 실행 시점 | `publishEvent()` 호출 즉시 | 트랜잭션 커밋 성공 후 |
| 트랜잭션 | 같은 트랜잭션 안에서 실행 | 트랜잭션 밖에서 실행 |

### `@EventListener` 사용 상황
- 트랜잭션이 없는 곳 (앱 시작 이벤트, 캐시 워밍업 등)
- 리스너가 같은 트랜잭션에 묶여도 되는 경우 (실패 시 함께 롤백되어야 하는 경우)
- 로깅처럼 실패해도 비즈니스에 영향 없는 경우

> 주의: 코드 결합도는 낮아지지만 런타임 결합도는 여전하다. (섹션 2 참고)

### 핵심 판단 기준
> "이 후처리가 DB 트랜잭션 성공에 의존하는가?"
- 의존한다 → `@TransactionalEventListener(AFTER_COMMIT)`
- 의존 안 한다 → `@EventListener`

---

## 6. 구현: ApplicationEvent + 쿠폰 리스너

### 파일 구조
```
domain/order/OrderConfirmedEvent.java         ← 이벤트 (orderId, userId)
application/payment/PaymentFacade.java        ← 이벤트 발행
application/order/OrderConfirmedEventListener ← 이벤트 수신 + 쿠폰 발급
```

### 리스너 핵심 패턴
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handle(OrderConfirmedEvent event) {
    CouponTemplate template = couponTemplateService.getById(couponTemplateId);
    couponService.issue(event.getUserId(), template);
}
```

**주의:** `@TransactionalEventListener`와 `@Transactional`을 함께 쓸 때는 반드시 `REQUIRES_NEW` 또는 `NOT_SUPPORTED`를 명시해야 한다.
→ AFTER_COMMIT 시점에는 원래 트랜잭션이 이미 끝났으므로, 새 트랜잭션(`REQUIRES_NEW`)을 열어야 DB 작업이 가능하다.

---

## 7. 좋아요-집계 이벤트 분리 (Eventual Consistency)

### 파일 구조
```
domain/like/LikedEvent.java           ← 집계용 이벤트 (productId)
domain/like/UnlikedEvent.java         ← 집계용 이벤트 (productId)
application/like/LikeFacade.java      ← 이벤트 발행 (AFTER_COMMIT 컨텍스트)
application/like/LikeEventListener   ← 이벤트 수신 + 집계 업데이트
```

### 왜 "이벤트 분리"가 아닌 "Eventual Consistency"인가

주문-결제는 "이벤트 분리", 좋아요-집계는 "eventual consistency"로 표현하는 이유:

| | 주문-결제 | 좋아요-집계 |
|---|---|---|
| 강조 | 책임 분리, 결합도 제거 | 데이터 일관성 트레이드오프 |
| 핵심 질문 | "쿠폰 실패가 결제에 영향을 줘야 하나?" | "카운트가 잠깐 틀려도 괜찮은가?" |
| 관점 | 아키텍처 설계 | 데이터 모델 |

집계(count)는 파생 데이터이므로 잠깐 부정확해도 비즈니스적으로 허용 가능 → Eventual Consistency 적용이 합리적.

---

## 8. 유저 행동 로깅 이벤트

### 설계 결정

- **발행 위치**: Controller (행동 추적 목적, 실패 포함해도 OK)
- **리스너**: `@EventListener` (트랜잭션 불필요, log.info()만)
- **DB 저장 없음**: 서버 로그에만 기록

### 파일 구조
```
domain/product/ProductViewedEvent.java          ← 상품 조회
domain/like/ProductLikedEvent.java              ← 좋아요 (로깅용, 집계용 LikedEvent와 별개)
domain/like/ProductUnlikedEvent.java            ← 좋아요 취소 (로깅용)
domain/order/OrderRequestedEvent.java           ← 주문 요청
domain/like/LikeAction.java                     ← enum (LIKED, UNLIKED)
application/logging/UserActionLogListener.java  ← @EventListener 4개
```

### 핵심 패턴
```java
// Controller에서 발행
LikeAction action = likeFacade.toggleLike(productId, userId);
if (action == LikeAction.LIKED) {
    eventPublisher.publishEvent(new ProductLikedEvent(productId, userId));
} else {
    eventPublisher.publishEvent(new ProductUnlikedEvent(productId, userId));
}

// 리스너
@EventListener
public void handleProductLiked(ProductLikedEvent event) {
    log.info("좋아요. productId={}, userId={}", event.getProductId(), event.getUserId());
}
```

**주의:** 집계용 `LikedEvent`/`UnlikedEvent`는 Facade에서 발행(AFTER_COMMIT), 로깅용 `ProductLikedEvent`/`ProductUnlikedEvent`는 Controller에서 발행(@EventListener). 목적이 다르므로 이벤트를 분리했다.

---

## 9. Outbox Pattern이 필요한 이유

### @TransactionalEventListener(AFTER_COMMIT)에서 바로 Kafka로 발행하면 안 되나?

```
[DB 커밋] ✅
    ↓
[AFTER_COMMIT 리스너 실행]
    ↓
[kafkaTemplate.send()] ← 여기서 서버가 죽으면 이벤트 소실
```

확률은 낮지만, **이벤트 종류에 따라 소실 허용 여부가 다르다.**

| 이벤트 | 소실 허용? | 이유 |
|---|---|---|
| `ProductViewedEvent` | 허용 가능 | 조회 수 1번 틀려도 비즈니스 영향 없음 |
| `LikedEvent` / `UnlikedEvent` | 허용 가능 | 집계 수치 약간 틀려도 괜찮음 |
| `OrderConfirmedEvent` | **허용 불가** | 판매량은 정산/재고와 연결, 소실 불가 |

→ Outbox는 모든 이벤트에 필수가 아니라, **소실이 허용되지 않는 이벤트**에 적용한다.
→ 이 과제에서는 `OrderConfirmedEvent`가 있으므로 Outbox Pattern이 정당화된다.

---

## 10. 멱등 처리가 필요한 이유

### Kafka는 At Least Once를 보장한다

> "최소 한 번은 전달한다" = 중복 전달될 수 있다

Ack를 받기 전까지 재전송하는 구조이므로, 처리 후 Ack 전송에 실패하면 같은 이벤트가 다시 온다.

```
Consumer → 처리 완료 → Ack 전송 실패 → Kafka가 재전송 → 중복 처리
```

→ **Kafka를 쓴다 = 중복 수신을 가정해야 한다**

### acks=all, idempotence=true는 충분하지 않다

Producer 쪽 중복 발행을 막는 설정이다. Consumer 쪽 중복은 애플리케이션 레벨에서 직접 처리해야 한다.

### 해결: event_handled 테이블

```
이벤트 수신
    ↓
event_handled 테이블에 이 이벤트 ID 있나?
    ├── 있다 → 이미 처리됨, 건너뜀
    └── 없다 → 처리 후 event_handled에 INSERT
```

같은 이벤트가 두 번 와도 한 번만 처리되도록 보장한다.

---

## 11. CDC vs Polling

### Polling (이 과제에서 사용하는 방식)

앱이 주기적으로 DB에 직접 물어보는 방식.

```java
@Scheduled(fixedDelay = 1000)
void relay() {
    List<OutboxEvent> events = outboxRepository.findUnpublished();
    for (OutboxEvent e : events) {
        kafkaTemplate.send(...);
        e.markPublished();
    }
}
```

```
앱: "새로운 outbox 있어?" (1초마다)
DB: "응 있어 / 없어"
```

### CDC (Change Data Capture)

MySQL은 모든 변경사항(INSERT/UPDATE/DELETE)을 **binlog**라는 파일에 순서대로 기록한다. 원래는 복제(replication) 용도다.

CDC는 이 binlog를 **실시간으로 따라가며 읽는** 방식. Debezium 같은 별도 도구가 MySQL에 복제 슬레이브처럼 붙어서 변경사항을 감지 → Kafka로 전달한다.

```
MySQL binlog → Debezium → Kafka
               (별도 프로세스가 binlog tail)
```

```
DB: "방금 INSERT 됐어!" (즉시)
Debezium: "알겠어, Kafka에 전달할게"
```

### 비교

| | Polling | CDC |
|---|---|---|
| 구현 주체 | 앱 코드 (@Scheduled) | 외부 도구 (Debezium) |
| 지연 | 수초 (설정 주기) | 수십ms (실시간) |
| DB 부하 | SELECT 쿼리 부하 | binlog 읽기 (낮음) |
| 복잡도 | 낮음 | 높음 (인프라 추가) |

### 왜 이 과제에서는 Polling을 쓰나

CDC는 Debezium 서버 설치/설정/운영이 별도로 필요하다. Outbox + Polling으로도 **"이벤트 소실 없음"** 이라는 핵심 목표는 달성 가능하므로, 복잡도가 낮은 Polling으로 구현한다.

---

## 12. Outbox Pattern 구현

### 파일 구조

```
domain/outbox/
  OutboxEvent.java            ← POJO (create/restore factory)
  OutboxEventRepository.java  ← save, findUnpublished, markPublished

infrastructure/outbox/
  OutboxEventEntity.java          ← JPA Entity (extends BaseEntity)
  OutboxEventJpaRepository.java   ← Spring Data JPA
  OutboxEventRepositoryImpl.java  ← 구현체

application/outbox/
  OutboxRelayScheduler.java   ← @Scheduled → Kafka 발행
```

### 발행 완료 처리 방식: publishedAt vs DELETE

두 가지 방식이 있다.

| | publishedAt 업데이트 | 발행 후 DELETE |
|---|---|---|
| 구현 | UPDATE SET published_at = now | DELETE |
| 이력 | 언제 발행됐는지 기록 남음 | 사라짐 |
| stuck 감지 | published_at IS NULL AND created_at < 5분 전 | created_at만으로 판단 |
| 테이블 크기 | 계속 쌓임 (별도 정리 필요) | 항상 작음 |

→ **publishedAt 방식 선택**: 발행 이력 추적, stuck 이벤트 감지(발행 실패 알람) 목적

### OutboxEventService 미생성 이유

Outbox는 "이벤트 유실 방지" 인프라 메커니즘이지 비즈니스 로직이 아니다.
Service를 만들어봤자 Repository 호출만 위임하는 껍데기가 된다.

→ `OutboxRelayScheduler` (application)가 `OutboxEventRepository` (domain 인터페이스)를 직접 사용

### relay 핵심 패턴

```java
@Scheduled(fixedDelay = 1000)
public void relay() {
    List<OutboxEvent> events = outboxEventRepository.findUnpublished();
    for (OutboxEvent event : events) {
        kafkaTemplate.send(...);
        outboxEventRepository.markPublished(event.getId());
    }
}
```

### 루프 내 예외 처리 전략

unpublished 이벤트가 여러 개일 때, 하나 실패해도 나머지를 계속 처리하려면 개별 try-catch가 필요하다.

```java
for (OutboxEvent event : unpublished) {
    try {
        kafkaTemplate.send(...);
        outboxEventRepository.markPublished(event.getId());
    } catch (Exception e) {
        log.error("Failed to publish outbox event. id={}", event.getId(), e);
        // 실패한 건 다음 주기에 재시도
    }
}
```

**선택지 비교:**

| 방식 | 동작 | 사용 상황 |
|---|---|---|
| **개별 try-catch** (현재) | 하나 실패해도 나머지 계속 처리 | 각 이벤트가 독립적인 경우 (Outbox relay) |
| **예외 전파 (try-catch 없음)** | 하나 실패하면 루프 전체 중단, 다음 주기에 처음부터 재시도 | 순서 중요 + 앞 것 실패하면 뒤 것도 의미 없는 경우 (마이그레이션, 데이터 파이프라인) |
| **Stream + 예외 무시** | 실패를 조용히 넘김 | 일부 누락이 허용되는 경우 (캐시 워밍업, 통계 전처리) |
| **CompletableFuture (병렬)** | 각 이벤트 독립적으로 비동기 처리 | 순서 보장 불필요 + 처리량이 중요한 경우 (단, Outbox처럼 partitionKey 순서가 필요하면 부적합) |

---

## 13. Outbox 저장 로직 구현

### 저장 위치

각 Facade의 기존 트랜잭션 안에서 outbox INSERT한다. 트랜잭션이 롤백되면 outbox도 함께 롤백되므로 이벤트 유실이 없다.

| Facade | 시점 | eventType | partitionKey |
|---|---|---|---|
| PaymentFacade.handleCallback() | order.confirm() 직후 | ORDER_CONFIRMED | orderId |
| LikeFacade.toggleLike() | like/unlike 직후 | LIKED / UNLIKED | productId |
| ProductFacade.recordView() | 신규 메서드 | PRODUCT_VIEWED | productId |

### payload 직렬화

ObjectMapper를 Facade(application 레이어)에서 직접 사용한다.

직렬화는 기술적으로 인프라 관심사지만, ObjectMapper는 비즈니스 로직이 아니고 Spring이 기본 빈으로 등록해준다. 이를 위해 별도 인터페이스/구현체를 만드는 건 과도한 추상화다.

### OutboxEventHelper

3개 Facade에서 `toJson()` 메서드가 중복되어 `application` 패키지에 static util로 분리했다.

```java
// application/OutboxEventHelper.java
public class OutboxEventHelper {
    public static String toJson(ObjectMapper objectMapper, Object value) { ... }
}
```

---

## 14. BEFORE_COMMIT vs AFTER_COMMIT 트레이드오프

### 실행 시점

```
트랜잭션 시작
  └── 도메인 로직 실행
      ↓
  [BEFORE_COMMIT 리스너] ← 아직 트랜잭션 안
      ↓
  COMMIT
      ↓
  [AFTER_COMMIT 리스너] ← 트랜잭션 밖
```

### BEFORE_COMMIT에서 외부 시스템(SNS/Kafka) 발행하면?

```
외부 발행 성공 → 트랜잭션 커밋
외부 발행 실패 → 트랜잭션 전체 롤백
```

→ "외부 발행 성공이 도메인 행위의 일부"로 간주하는 것. 외부 시스템 장애가 내 시스템 장애로 전파된다.

### AFTER_COMMIT + Outbox가 필요한 이유

```
트랜잭션 커밋 ✅
    ↓
AFTER_COMMIT → 외부 발행 시도
  ├── 성공 ✅
  └── 실패해도 Outbox에 기록 있음 → 배치가 재발행
```

→ 도메인 행위(DB 저장)와 외부 발행을 분리. 외부 시스템 장애가 내 트랜잭션에 영향을 주지 않는다.

### 비교

| | BEFORE_COMMIT | AFTER_COMMIT + Outbox |
|---|---|---|
| 외부 장애 영향 | 내 트랜잭션 롤백 | 없음 |
| 발행 보장 | 성공해야 커밋됨 | Outbox로 재발행 가능 |
| 언제 쓰나 | 발행 실패가 곧 도메인 실패인 경우 | 대부분의 경우 |

### 실무 레퍼런스: 배달의민족 회원시스템

배민도 처음엔 BEFORE_COMMIT에서 SNS 발행 → SNS 장애 시 로그인/가입 전체 실패 문제 발생 → AFTER_COMMIT으로 전환 + 이벤트 저장소(= Outbox) 구축.

배민의 "이벤트 저장소(member_event 테이블)"는 Outbox Pattern의 또 다른 구현이다. 이름만 다를 뿐 핵심은 동일: **도메인 트랜잭션 안에서 이벤트를 DB에 저장 → 발행 보장**.

---

## 15. 전체 흐름 요약

```
1단계: handleCallback() 안에 다 넣기
       → 실패 시 결제까지 롤백, 응답 느림

2단계: ApplicationEvent 발행
       → 분리됐지만 커밋 전 발행 위험

3단계: @TransactionalEventListener(AFTER_COMMIT)
       → 커밋 후 실행, 근데 서버 죽으면 이벤트 소실

4단계: Transactional Outbox Pattern
       → 이벤트를 DB에 저장해서 유실 방지 (Kafka와 연결)

5단계: OutboxRelayScheduler
       → @Scheduled가 미발행 outbox 조회 → Kafka 발행 → markPublished()
```

---

## 16. Kafka Topic 설계

### Topic 이름 = eventType

```
outbox_event.event_type → Kafka topic 이름으로 그대로 사용

PRODUCT_VIEWED   → topic: "PRODUCT_VIEWED"
LIKED            → topic: "LIKED"
UNLIKED          → topic: "UNLIKED"
ORDER_CONFIRMED  → topic: "ORDER_CONFIRMED"
```

### 왜 eventType별로 topic을 분리하나?

**단일 topic(`commerce.events`)이면:**
- Consumer가 모든 이벤트를 다 받고 eventType으로 분기해야 함
- 관심 없는 이벤트도 전부 consume

**eventType별 topic이면:**
- Consumer가 필요한 topic만 선택해서 구독 가능
- Consumer Group 분리 시 각 그룹이 관심 있는 topic만 구독

### Consumer Group이란?

같은 topic을 여러 그룹이 독립적으로 읽는 구조.

```
topic: ORDER_CONFIRMED
  ├── metrics-group      → sales_count 집계
  └── notification-group → 주문 완료 알림 발송
```

두 그룹은 같은 메시지를 각자 독립적으로 처리한다. 같은 그룹 내에서는 파티션을 나눠서 부하 분산.

### Partition Key

```java
kafkaTemplate.send(event.getEventType(), event.getPartitionKey(), event.getPayload());
//                 topic                 key(파티션 키)          value
```

같은 partitionKey를 가진 메시지는 항상 같은 파티션으로 라우팅 → **같은 엔티티에 대한 이벤트 순서 보장**.

예: productId=123인 PRODUCT_VIEWED 이벤트는 항상 같은 파티션 → Consumer가 순서대로 처리.
```

## 17. Consumer 동시성과 원자적 쿼리

### 파티션 수 = 활성 Consumer 수 상한

Consumer Group 내에서 파티션 수만큼 Consumer 인스턴스가 병렬로 활성화된다.

```
topic: PRODUCT_VIEWED (파티션 3개)
  └── metrics-group
        ├── Consumer1 → 파티션0 처리
        ├── Consumer2 → 파티션1 처리
        └── Consumer3 → 파티션2 처리
```

파티션이 1개면 Consumer도 1개만 활성화된다 (나머지는 대기). 파티션 수는 topic 생성 시 지정한다.

### RMW(Read-Modify-Write) 문제

파티션이 여러 개면 서로 다른 Consumer가 같은 productId 이벤트를 거의 동시에 처리할 수 있다.

```
Consumer1: read(view_count=10) → +1 → save(11)
Consumer2: read(view_count=10) → +1 → save(11)  ← 하나 유실
```

### 해결: 원자적 쿼리

DB가 `SET view_count = view_count + 1`을 한 번에 처리 → 동시 요청도 순차적으로 안전하게 반영된다.

```java
@Modifying
@Query("UPDATE ProductMetricsEntity m SET m.viewCount = m.viewCount + 1 WHERE m.productId = :productId")
void incrementViewCount(@Param("productId") Long productId);
```

```
Consumer1: UPDATE SET view_count = view_count + 1  → 11
Consumer2: UPDATE SET view_count = view_count + 1  → 12  ← 정확
```

이 과제에서는 파티션 1개(기본값)이므로 동시성 문제가 발생하지 않지만, 파티션을 늘릴 때를 대비해 원자적 쿼리를 사용한다.

---

## 18. 멱등 처리 구현: EventHandled

### 역할

같은 이벤트가 두 번 이상 수신됐을 때 중복 처리를 막는 테이블.

Consumer가 메시지를 처리하기 전에 `event_handled` 테이블에서 해당 이벤트 ID를 조회한다. 이미 처리된 기록이 있으면 skip, 없으면 처리 후 기록을 남긴다.

### 파일 구조

```
domain/event/
  EventHandled.java            ← 도메인 모델 (eventId, eventType)
  EventHandledRepository.java  ← existsByEventIdAndEventType, save

infrastructure/event/
  EventHandledEntity.java          ← JPA Entity (extends BaseEntity)
  EventHandledJpaRepository.java   ← Spring Data JPA
  EventHandledRepositoryImpl.java  ← 구현체
```

### 핵심 설계 결정

**왜 (eventId, eventType) 복합 unique constraint인가?**

같은 eventId라도 eventType이 다르면 서로 다른 Consumer가 다른 목적으로 처리할 수 있다.
나중에 Consumer Group을 분리할 때도 각 그룹이 독립적으로 멱등 체크를 할 수 있다.

```java
@UniqueConstraint(name = "uk_event_handled_event_id_type", columnNames = {"event_id", "event_type"})
```

**왜 event_id에 단독 index도 추가했나?**

`existsByEventIdAndEventType` 쿼리에서 event_id로 먼저 좁혀야 빠르다.
복합 unique constraint만으로도 쿼리는 되지만, 명시적 index로 의도를 드러냈다.

### Consumer에서 사용하는 흐름

```
이벤트 수신
    ↓
existsByEventIdAndEventType(eventId, eventType)?
    ├── true  → 이미 처리됨, 건너뜀 (return)
    └── false → 처리 후 EventHandled.create(eventId, eventType) 저장
```

이 로직은 Step 8의 `ProductMetricsConsumer`에서 실제로 사용된다.
