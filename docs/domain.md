# Domain Model

> 도메인 엔티티/필드를 작업할 때 읽는다.

## 운영자(Admin)
- 역할: `SUPER_ADMIN`, `GM`, `CS`, `VIEWER`
- 역할별 메뉴/액션 권한 다름. 권한 정책은 @audit-and-security.md 참조.

## 게임 유저(GameUser)
- 식별자: `userId`, `nickname`, `deviceId`
- 상태: `ACTIVE`, `BANNED`, `CHAT_MUTED`, `DORMANT`
- 다중 재화 보유(cash/diamond/gold). 재화 변경은 별도 트랜잭션 + 멱등성.
- 누적 결제액(LTV) 노출 — 운영 의사결정 지표.

## 인게임 메일(InGameMail)
- 발송 대상: 개별 / 전체 / 조건부(레벨/국가/VIP)
- 첨부: 재화 + 아이템 N개
- 상태: `SCHEDULED`, `SENT`, `EXPIRED`
- 예약 발송 + 수령 기한 설정 가능.
- 단체 발송은 비동기 처리(@Async).

## 인게임 상품(Product)
- 노출 조건: 기간(start/end), 국가(KR/US/JP/GLOBAL), 최소 레벨, 활성 토글.
- 가격(재화 종류 + 금액) + 지급 구성(아이템 N개, ProductReward).
- 구매 제한: 1회 한정 / 일일 N회 / 주간 N회 / 무제한.
- Redis 캐싱 대상. 쓰기 시 무효화 필수.

## BI 지표(Metric)
- DAU, MAU, 신규 가입, 매출(일/주/월), ARPU, ARPPU.
- 리텐션 D1/D7/D30.
- 사전 집계 테이블(`daily_metric`)에서 조회. 실시간 계산 금지.

## 시드 데이터 정책
- 게임 유저 5,000명 이상(검색 페이지네이션 시연용).
- 결제/접속 로그 최근 90일치.
- 상품 카테고리별 10개 이상.
- 운영자 계정은 역할별 1개씩.
- 시드 SQL은 `backend/src/main/resources/db/seed/`.