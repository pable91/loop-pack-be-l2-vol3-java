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
- 리스너가 같은 트랜잭션에 묶여도 되는 경우
- 의존성을 직접 갖고 싶지 않을 때 (결합도 분리 목적)

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

## 7. 전체 흐름 요약

```
1단계: handleCallback() 안에 다 넣기
       → 실패 시 결제까지 롤백, 응답 느림

2단계: ApplicationEvent 발행
       → 분리됐지만 커밋 전 발행 위험

3단계: @TransactionalEventListener(AFTER_COMMIT)
       → 커밋 후 실행, 근데 서버 죽으면 이벤트 소실

4단계: Transactional Outbox Pattern
       → 이벤트를 DB에 저장해서 유실 방지 (Kafka와 연결)
```
