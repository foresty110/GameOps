# 4-Day Schedule

> Day별 우선순위 확인 시 읽는다. 체크박스는 진행하며 갱신.

## Day 1: 기반 구축
- [x] Spring Boot 프로젝트 셋업 + Gradle (SB 3.3.5, Java 17)
- [x] docker-compose (mysql + redis + backend + frontend)
- [x] Spring Security + JWT (jjwt 0.12.x)
- [x] RBAC 4개 역할(@docs/audit-and-security.md)
- [x] 운영자 계정 + 로그인 API (CRUD는 Day 2~로 이월)
- [x] React + Vite + Tailwind + TanStack Query 셋업
- [x] 공통 레이아웃 + 로그인 페이지
- [x] Audit Log 인프라(엔티티 + AOP)
- [x] 글로벌 ExceptionHandler + ApiResponse
- [x] 시드 데이터 스크립트 (LocalSeeder, @Profile("local"))

## Day 2: 유저 관리
- [ ] GameUser 엔티티 + 검색 API (페이지네이션, 다중 조건)
- [ ] 유저 상세 조회 API
- [ ] 제재(밴/채팅금지) API + 사유 기록
- [ ] 재화 지급/회수 API (멱등키 적용)
- [ ] 유저 목록 페이지 + 상세 모달
- [ ] 제재/지급 UI + 사유 입력
- [ ] Service 레이어 테스트(제재, 재화 지급 멱등성)

## Day 3: 대시보드 + 메일
- [ ] 지표 집계 테이블 + 더미 90일치
- [ ] DAU/MAU/매출/리텐션 조회 API
- [ ] 대시보드 페이지 (차트 4~5종, 기간 필터)
- [ ] InGameMail 엔티티 + 발송 API
- [ ] 단체 발송 비동기 처리(@Async)
- [ ] 메일 발송/이력 페이지
- [ ] 예약 발송 스케줄러

## Day 4: 상품 관리 + 마무리
- [ ] Product/ProductReward/Item 엔티티 + CRUD API
- [ ] 노출 조건 필터(기간/국가/레벨/활성)
- [ ] Redis 캐싱 + 수정 시 무효화
- [ ] 상품 관리 페이지(목록/생성/수정/미리보기)
- [ ] Swagger 마무리
- [ ] README(@docs/readme-checklist.md)
- [ ] 아키텍처 다이어그램 + ERD
- [ ] 시연 GIF
- [ ] GitHub Actions(PR 시 백엔드 테스트)

## 우선순위 원칙
시간이 부족하면 다음 순서로 포기한다(뒤로 갈수록 먼저 포기):
1. **유지**: 유저 관리, 대시보드, README, Audit Log, 테스트 코드
2. **축소 가능**: 상품 관리(CRUD + 활성 토글까지만), 메일(개별만)
3. **포기 가능**: 예약 발송, Redis 캐싱, GitHub Actions