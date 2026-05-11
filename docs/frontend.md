# Frontend Guide

> React 코드를 작성/수정할 때 읽는다.

## 폴더 구조 (Feature-based)
```
features/users/
├── api/         # axios 호출 함수
├── hooks/       # react-query 훅
├── components/  # 도메인 전용 컴포넌트
└── types.ts     # API 타입
```

공통은 `shared/`(UI, hooks, utils), 라우트 단위는 `pages/`.

## 데이터 페칭
- **TanStack Query 전용**. `useEffect` 안 axios 호출, 직접 fetch 금지.
- 쿼리 키 패턴: `['users', { page, keyword }]`.
- 뮤테이션 후 `invalidateQueries`로 갱신.

## 타입
- API 요청/응답은 `types.ts`에 정의.
- `any` 금지. 불가피하면 `unknown` 후 type guard.
- enum보다 union type 선호.

## 스타일링
- Tailwind 유틸리티 클래스만 사용.
- 반복 패턴은 컴포넌트화(`<Button variant="primary" />`).
- 디자인 토큰은 `tailwind.config.js`에서.
- shadcn/ui 도입 허용(Tailwind 기반이라 회사 스택과 충돌 없음).

## 라우팅
- `react-router-dom` v6.
- 인증 가드: `<ProtectedRoute requiredRole="GM">` 패턴.
- 권한 없는 메뉴는 사이드바에서 숨김 처리.

## 폼
- React Hook Form + Zod 권장(선택).

## 차트
- Recharts(대시보드용).
- 차트 종류는 데이터 성격에 맞게: 추세=라인, 비교=바, 비율=파이.