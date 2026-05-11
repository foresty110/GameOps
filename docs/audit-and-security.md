# Security, Audit & Idempotency

> 인증/권한/Audit Log/멱등성/동시성 관련 작업 시 읽는다.

## 인증
- JWT 기반. Access + Refresh.
- 로그인 시 운영자 계정 검증 → 토큰 발급.
- Spring Security FilterChain에서 JWT 파싱 → SecurityContext에 인증 정보 주입.

## 권한 (RBAC)
- 역할: `SUPER_ADMIN`, `GM`, `CS`, `VIEWER`
- 정책 예시:
    - 유저 제재: `GM`, `SUPER_ADMIN`
    - 재화 지급: `GM`, `SUPER_ADMIN`
    - 전체 메일 발송: `SUPER_ADMIN` 전용
    - 대시보드 조회: 모든 역할
    - 상품 관리: `GM`, `SUPER_ADMIN`
    - CS 답변/개별 메일: `CS`, `GM`, `SUPER_ADMIN`
- 컨트롤러 메서드에 `@PreAuthorize("hasRole('GM')")` 등으로 강제.
- 사이드바 메뉴도 권한에 따라 숨김.

## Audit Log
- 모든 어드민 쓰기 액션 자동 기록.
- 구현: `@Auditable` 커스텀 어노테이션 + AOP, 또는 Service에서 명시 호출.
- 기록 항목: 운영자 ID, 액션 종류, 대상 리소스 ID, 변경 전/후(JSON), **사유(reason)**, 타임스탬프, IP.
- 중요 액션(재화 지급, 제재, 메일 발송)은 사유 입력을 API 레벨에서 필수화.

## 멱등성
- 재화 지급, 메일 발송 등 부수효과 있는 API는 `Idempotency-Key` 헤더 지원.
- 처리 흐름:
    1. 키 + 운영자 ID로 처리 기록 조회.
    2. 있으면 이전 응답 반환.
    3. 없으면 처리 후 결과 저장.
- 저장소: `idempotency_record` 테이블 또는 Redis(TTL 24h).

## 동시성
- 상품 구매 제한, 재화 정산처럼 동시성이 중요한 곳:
    - 단일 인스턴스: 비관락(`SELECT ... FOR UPDATE`).
    - 분산 환경 고려: Redis 분산락(Redisson 또는 자체 SETNX).
- 어디에 어떤 락을 썼는지 ADR로 기록.