# CLAUDE.md

## WHY
GEAR2(기어세컨드) 서버팀 풀스택 포지션 지원용 포트폴리오. JD가 강조하는 **풀스택 책임, 운영툴 도메인 이해, 품질(TDD/리뷰), BI 프로덕트 감각**을 4일 안에 증명하는 것이 목표.

## WHAT
방치형 모바일 RPG의 사내 운영 플랫폼(GM Tool). 사내 운영자(GM/CS/BI)가 사용하는 어드민 백오피스이며 B2C 서비스가 아니다. 핵심 도메인: 운영자 권한 관리, 게임 유저 관리, 인게임 메일 발송, BI 대시보드, 인게임 상품 관리.

## HOW
- 회사 스택 그대로 사용: Spring Boot 3.3.x (LTS) + Java 17 + MySQL 8 + Redis 7 / React 18 + TS + Vite + Tailwind + TanStack Query / Docker + GitHub Actions.
- 도메인은 패키지로 분리(Package by Feature). Layer 분리는 도메인 내부에서만.
- 모든 어드민 쓰기 액션은 Audit Log에 기록(누가/언제/무엇을/왜).
- Service 레이어는 테스트 코드 필수. 멱등성·동시성·트랜잭션 경계는 의식적으로 설계.
- 코드 스타일은 linter/formatter가 강제한다. 이 문서에서 다루지 않는다.

## 명령어
```bash
# 전체 실행
docker-compose up

# 백엔드
cd backend && ./gradlew bootRun          # 실행
cd backend && ./gradlew test             # 테스트
cd backend && ./gradlew spotlessApply    # 포맷

# 프론트
cd frontend && pnpm dev                  # 실행
cd frontend && pnpm test                 # 테스트
cd frontend && pnpm lint && pnpm format  # 린트/포맷
```

## 워크플로우: Explore → Plan → Implement → Commit
새 기능 요청을 받으면 즉시 코드를 쓰지 않는다. 다음 순서를 따른다.

1. **Explore**: 관련 도메인 파일과 기존 코드를 먼저 읽는다. @docs/domain.md, @docs/backend.md 등 해당 작업에 필요한 세부 문서를 로드한다.
2. **Plan**: 구현 전에 계획을 제시한다. 큰 기능(파일 3개 이상 변경)이면 먼저 **AskUserQuestion 도구로 인터뷰**한다 — 엣지 케이스, 권한 정책, 트랜잭션 경계, UI 흐름, 트레이드오프를 객관식으로 사용자에게 묻는다. 인터뷰 후 계획을 정리하고, Plan Mode가 켜져 있으면 `ExitPlanMode`로 승인을 받는다.
3. **Implement**: 사용자가 계획에 동의한 후에만 코드를 작성한다. 작업 단위는 작게 쪼개고, 각 단계마다 테스트가 통과하는 상태를 유지한다.
4. **Commit**: 의미 있는 단위로 커밋한다. 메시지에 "무엇/왜"를 명시.

"바로 만들어줘" 요청에도 위 순서를 건너뛰지 않는다. 대신 Plan 단계를 짧게 압축한다.

## 컨텍스트 위생
메인 세션이 오염되면 판단력이 떨어진다. 다음 원칙을 지킨다.

- **무거운 작업은 서브에이전트에 위임한다** (테스트 실행, 코드 리뷰, 대규모 grep 등). `.claude/agents/` 참조.
- **파일 전체 덤프 대신 필요한 라인만 읽는다** (`view`의 `view_range` 활용).
- **작업이 한 단위 끝나면 사용자에게 `/clear` 또는 `/compact` 사용을 제안한다**.
- 같은 패턴을 두 번 반복했다면 그건 스킬로 만들 신호다. 사용자에게 스킬화를 제안한다.

## 스킬 사용
`.claude/skills/<name>/SKILL.md`는 작업 패턴과 매칭될 때 자동 로드된다. 작업 시작 시 관련 스킬이 이미 로드되어 있는지 확인하고, 없으면 명시적으로 참조한다.

현재 사용 가능한 스킬:
- `spring-feature`: Spring 도메인 추가/수정 시
- `audit-and-idempotency`: Audit Log, 멱등성, 동시성 처리 시
- (추가될 예정: `react-feature`, `bi-dashboard`)

## 서브에이전트 사용
독립된 컨텍스트를 갖는 전문 작업자. 메인 세션을 오염시키지 않는다.

| 에이전트 | 호출 시점 |
|---|---|
| `code-reviewer` | 코드 작성/수정 후 자동 호출 (proactive) |
| `test-runner` | 테스트 실행 요청 시 |

**단일 세션이 더 빠른 경우** (서브에이전트 쓰지 말 것):
- 단순 버그 수정
- 1~2개 파일 작은 변경
- 즉답 가능한 질문

## Hooks (자동 강제 장치)
`.claude/settings.json`에 등록된 hook이 다음을 자동 강제한다. Claude는 이를 우회할 수 없다.

- **위험 명령 차단**: `rm -rf /`, `git push --force`, `DROP DATABASE`, 비밀 파일 cat 등
- **보호 파일 차단**: `.env`, `application-prod.yml`, `*.pem`, `id_rsa` 편집 차단
- **자동 포맷**: Java/TS 파일 수정 후 spotless/prettier 자동 실행
- **권한 누락 경고**: Controller의 쓰기 엔드포인트에 `@PreAuthorize`가 없으면 경고

차단되면(exit 2) 사유 메시지가 표시된다. 그 경우 다른 방법으로 우회하지 말고, 사용자에게 의도를 확인한다.

## 작업 시 항상 지킬 것
1. 회사 명시 스택을 벗어나는 라이브러리 도입 전 사용자에게 확인.
2. 새 기능에는 Service 레이어 테스트를 함께 작성.
3. 어드민 쓰기 API에는 Audit Log + 권한 체크(`@PreAuthorize`) 누락 금지.
4. Entity를 컨트롤러 응답으로 직접 반환 금지. DTO 변환 명시.
5. React 데이터 페칭은 TanStack Query만 사용.
6. Day별 목표(@docs/schedule.md)를 벗어나는 작업은 사용자에게 확인 후 진행.

## 모호할 때
실제 운영툴에서 어떻게 동작할지를 기준으로 판단. 그래도 모호하면 묻는다(추측 금지).

---

## 세부 문서 (작업 시작 시 필수 로드)

**작업을 시작하기 전에 해당하는 문서를 반드시 Read tool로 먼저 로드한다.
"이미 안다"고 가정하지 않는다.**

| 작업 종류                        | 필수 로드                                 |
|------------------------------|---------------------------------------|
| 모든 작업 시작 시                   | @docs/schedule.md (오늘 우선순위 확인)        |
| Spring/백엔드 코드 작성             | @docs/backend.md, @docs/domain.md  둘다 |
| React/프론트엔드 코드 작성            | @docs/frontend.md, @docs/domain.md 둘다 |
| 인증/권한/Audit Log/멱등성 관련 작업 시  | @docs/audit-and-security.md           |
| README 작성/갱신 시               | @docs/readme-checklist.md             |
|   설계 결정의 "왜"를 코드/문서에 녹일 때 |@docs/interview-prep.md|
|  Day별 작업 우선순위 확인 시 |@docs/schedule.md  |

위 규칙을 어기고 코드부터 작성하면 안 된다.
docs를 안 읽고 시작했다면 작업을 멈추고 먼저 로드한다.