---
name: audit-and-idempotency
description: Use when implementing admin write actions that need audit logging, idempotency guarantees, or concurrency control. Triggers on tasks involving currency grants, user sanctions, mail dispatch, coupon redemption, or any operator action that mutates user-facing state.
---

# Audit & Idempotency Skill

## Audit Log 적용 패턴

### 방법 1: @Auditable AOP (권장)
```java
@Auditable(action = "USER_SANCTION", targetType = "GAME_USER")
public void sanctionUser(Long adminId, Long userId, SanctionRequest req) {
    // 비즈니스 로직만 작성
    // AOP가 자동으로 AuditLog 저장
}
```

### 방법 2: 명시적 호출 (복잡한 경우)
```java
public void grantCurrency(Long adminId, Long userId, CurrencyGrantRequest req) {
    UserCurrency before = currencyRepository.findByUserId(userId);
    // ... 비즈니스 로직 ...
    UserCurrency after = currencyRepository.save(updated);

    auditService.record(AuditLog.builder()
        .adminId(adminId)
        .action("CURRENCY_GRANT")
        .targetType("GAME_USER")
        .targetId(userId.toString())
        .before(toJson(before))
        .after(toJson(after))
        .reason(req.reason())
        .build());
}
```

## Audit Log 필수 필드
- `adminId`: 운영자 ID (SecurityContext에서 추출)
- `action`: 액션 종류 enum (USER_SANCTION, CURRENCY_GRANT, MAIL_SEND 등)
- `targetType`, `targetId`: 대상 리소스
- `before`, `after`: 변경 전/후 JSON 스냅샷
- `reason`: **사유 (필수, API 레벨에서 강제)**
- `ipAddress`: 요청 IP
- `createdAt`: 자동 타임스탬프

## 멱등성 처리

### 적용 대상
- 재화 지급/회수
- 메일 발송
- 쿠폰 발급
- 상품 구매 처리

### 구현 패턴
```java
@PostMapping("/currency/grant")
public ApiResponse<Void> grant(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody @Valid CurrencyGrantRequest req
) {
    return idempotencyService.execute(idempotencyKey, () -> {
        currencyService.grant(req);
        return null;
    });
}
```

### 멱등성 저장소
- 테이블: `idempotency_record (key, admin_id, response_body, created_at)`
- 또는 Redis: `idempotency:{key}` TTL 24h
- 같은 키 + 같은 운영자로 재요청 시 저장된 응답 반환

## 동시성 처리

### 시나리오별 선택
| 시나리오 | 방법 |
|---|---|
| 한정 상품 구매 제한 | DB 비관락 `SELECT ... FOR UPDATE` |
| 재화 정산 | DB 비관락 또는 낙관락 |
| 글로벌 한정 상품 (분산환경 가정) | Redis 분산락 (Redisson) |
| 단순 카운터 | Redis `INCR` |

### 결정 기록
어느 곳에 어떤 락을 썼는지 `docs/adr/` 또는 README에 기록한다. 면접 질문 단골이다.

## 흔한 실수
- ❌ 사유 입력을 옵션으로 → ✅ `@NotBlank` 필수
- ❌ Audit Log 실패해도 무시 → ✅ 트랜잭션에 포함시켜 같이 롤백 (단, 별도 트랜잭션 분리 옵션도 고려)
- ❌ 멱등키를 사용자가 안 보내면 그냥 처리 → ✅ 멱등키 누락 시 400 응답
- ❌ before/after를 toString()으로 저장 → ✅ JSON 직렬화 (검색/diff 가능)