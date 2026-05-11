# GameOps

방치형 모바일 RPG의 사내 운영 플랫폼(GM Tool). Spring Boot + React 기반의 어드민 백오피스.

상세 내용은 Day 4에 작성됩니다. (`docs/readme-checklist.md` 기준)

## 빠른 실행

```bash
docker-compose up
```

`local` 프로파일에서 운영자 4명이 자동 시드됩니다. 모두 비밀번호는 `Admin1234!` 입니다.

| username  | role        | 비고           |
|-----------|-------------|----------------|
| super     | SUPER_ADMIN | 최고관리자     |
| gm1       | GM          | 게임 운영      |
| cs1       | CS          | 고객 응대      |
| viewer1   | VIEWER      | 읽기 전용      |

## 디렉토리

- `backend/` — Spring Boot 3.3 (Java 17)
- `frontend/` — React 18 + Vite + TypeScript
- `docs/` — 도메인/백엔드/프론트/보안 등 설계 문서
- `.claude/` — Claude Code 워크플로 (스킬·훅·에이전트)
