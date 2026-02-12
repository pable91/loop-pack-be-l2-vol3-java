
# 주요 시퀀스 다이어그램

### 1. 주문 요청
```mermaid
sequenceDiagram
actor User as 사용자
participant Ctrl as OrderController
participant Facade as OrderFacade
participant P_Svc as ProductService
participant O_Svc as OrderService
participant H_Svc as OrderStatusHistoryService
participant DB as MySQL (Repository)

    User->>Ctrl: 주문 요청
    Ctrl->>Facade: createOrder(request)
    activate Facade

    Note over Facade, DB: [ 트랜잭션 시작 ]

    %% 2. 상품 수량 확인 및 감소
    Facade->>P_Svc: checkAndDecreaseStock(productId, quantity)
    P_Svc->>DB: 상품 조회 (Lock)
    DB-->>P_Svc: 상품 정보 반환
    
    alt 수량 부족 (Exception)
        P_Svc-->>Facade: 수량 부족 예외 던짐
        Facade-->>Ctrl: 예외 전달
        Ctrl-->>User: "주문 실패: 재고 부족"
    else 수량 충분
        P_Svc->>DB: 상품 수량 감소 (Update)
        P_Svc-->>Facade: 성공 응답 (스냅샷용 상품 정보 포함)
        
        %% 4. ORDER_ITEM 및 주문 생성
        Facade->>O_Svc: createOrder(userId, productInfo, quantity)
        O_Svc->>DB: INSERT ORDER & ORDER_ITEM (스냅샷 저장)
        O_Svc-->>Facade: Order ID 반환
        
        %% 5. 주문 상태 히스토리 저장
        Facade->>H_Svc: saveHistory(orderId, 'ORDERED')
        H_Svc->>DB: INSERT ORDER_STATUS_HISTORY
        
        Note over Facade, DB: [ 트랜잭션 커밋 ]
        Facade-->>Ctrl: 주문 완료 (Order ID)
        deactivate Facade
        
        Ctrl-->>User: 200 OK (주문 성공)
    end
```

### 2. 좋아요 클릭
```mermaid
sequenceDiagram
    actor User as 사용자
    participant Ctrl as LikeController
    participant Facade as OrderFacade (Retry)
    participant P_Svc as ProductService
    participant L_Svc as LikeService
    participant DB as MySQL

    User->>Ctrl: 좋아요 클릭
    Ctrl->>Facade: toggleLike(userId, productId)
    
    loop 최대 3회 재시도 (Retry Loop)
        Facade->>Facade: [ 트랜잭션 시작 ]
        
        Facade->>P_Svc: getProduct(productId)
        P_Svc->>DB: SELECT id, like_count, version FROM PRODUCT
        DB-->>P_Svc: 상품 정보 (Version: 1) 반환
        
        Facade->>L_Svc: addLike(userId, productId)
        L_Svc->>DB: INSERT INTO LIKE (Unique Key 체크)
        
        Facade->>P_Svc: increaseLikeCount(productId, version: 1)
        P_Svc->>DB: UPDATE PRODUCT SET like_count=11, version=2 <br/> WHERE id=? AND version=1
        
        alt 업데이트 성공 (Rows Affected = 1)
            DB-->>P_Svc: 성공
            Facade->>Facade: [ 트랜잭션 커밋 ]
            Note over Facade: 루프 종료 (성공)
        else 버전 충돌 (Rows Affected = 0)
            DB-->>P_Svc: 실패 (OptimisticLockException)
            P_Svc-->>Facade: 예외 발생
            Facade->>Facade: [ 트랜잭션 롤백 ]
            Note over Facade: 잠시 대기 후 재시도 결정
        end
    end

    Facade-->>Ctrl: 최종 결과 반환
    Ctrl-->>User: 응답