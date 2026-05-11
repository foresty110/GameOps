# Backend Guide

> Spring 코드를 작성/수정할 때 읽는다.

## 패키지 구조 (도메인 내부)
```
user/
├── UserController.java
├── UserService.java
├── UserRepository.java
├── domain/        # JPA Entity, Enum
├── dto/           # Request/Response
└── exception/     # 도메인 예외
```

복잡한 조회는 `UserQueryRepository` 별도 분리(QueryDSL).

## 레이어 책임
- **Controller**: 요청/응답 변환만. `@Valid` 검증. 비즈니스 로직 금지.
- **Service**: 트랜잭션 경계(`@Transactional`은 public 메서드에). 비즈니스 로직 위치. 다른 도메인은 Service를 통해서만 호출.
- **Repository**: JPA. 복잡 조회는 QueryDSL.

## 응답 포맷
모든 API는 공통 래퍼 사용.
```java
public record ApiResponse<T>(boolean success, T data, ErrorInfo error) {
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static ApiResponse<Void> fail(ErrorInfo error) { ... }
}
```

## 예외 처리
- 도메인 예외는 `BusinessException` 상속.
- `@RestControllerAdvice` 글로벌 핸들링.
- 에러 코드는 `ErrorCode` enum(code + message + HTTP status).

## 테스트
- Service 레이어 단위 테스트 필수(Mockito).
- 주요 시나리오(노출 기간 필터링, 제재 처리, 멱등성)는 통합 테스트도.
- 메서드명은 한글 허용: `상품_노출기간_외에는_조회되지_않는다()`.

## Redis 캐싱
- 자주 읽고 적게 변경되는 데이터에만(상품 목록, 운영자 권한).
- 쓰기 시 무효화 패턴 명시.
- TTL: 상품 5분, 권한 정보 1시간(데이터 특성에 맞게 조정).

## DTO ↔ Entity
- Entity를 컨트롤러 응답으로 직접 반환 금지.
- 변환은 Service 또는 DTO 정적 팩토리 메서드(`Response.from(entity)`)에서.

## 의존성 추가 시
- 회사 명시 스택(@CLAUDE.md HOW 섹션) 외 라이브러리는 사용자 확인 후.