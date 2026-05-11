---
name: spring-feature
description: Use when adding or modifying a Spring Boot domain feature in this project (controller/service/repository/entity/dto/test). Triggers when the task involves creating REST APIs, JPA entities, business logic, or service-layer tests for the GEAR2 GM Platform backend.
---

# Spring Feature Skill

## 표준 절차
새 도메인 기능을 추가할 때 이 순서를 따른다.

1. `backend/src/main/java/com/example/gameops/<feature>/` 패키지를 생성한다 (Package by Feature).
2. 다음 파일을 만든다:
   - `domain/Xxx.java` (JPA Entity)
   - `domain/XxxStatus.java` 등 enum
   - `dto/XxxRequest.java`, `dto/XxxResponse.java`
   - `XxxRepository.java` (필요 시 `XxxQueryRepository.java` 추가)
   - `XxxService.java`
   - `XxxController.java`
   - `exception/XxxException.java` (도메인 예외)
3. 테스트 파일을 동시에 만든다:
   - `src/test/java/.../<feature>/XxxServiceTest.java` (Mockito 단위 테스트)
   - 주요 시나리오는 `@SpringBootTest` 통합 테스트 추가

## 레이어 책임 (위반 금지)
- **Controller**: 요청/응답 변환만. `@Valid` 검증. 비즈니스 로직 금지.
- **Service**: `@Transactional`은 public 메서드에만. 다른 도메인은 그 도메인의 Service를 통해서만 호출.
- **Repository**: JPA. 복잡 조회는 QueryDSL로 별도 파일 분리.

## 응답 포맷 (모든 API 공통)
```java
@GetMapping("/{id}")
public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
    UserResponse data = userService.findById(id);
    return ApiResponse.ok(data);
    // 실패 시 GlobalExceptionHandler가 자동 처리
}
```

## DTO ↔ Entity 변환
- Entity를 Controller에서 직접 반환 금지.
- 변환은 DTO의 정적 팩토리 메서드로: `UserResponse.from(user)`.
- 요청 DTO → Entity 변환은 Service에서 명시.

## 어드민 쓰기 API 체크리스트
다음이 누락되면 안 된다.

- [ ] `@PreAuthorize("hasRole('GM')")` 등 권한 어노테이션
- [ ] `@Auditable` 어노테이션 또는 Service에서 명시적 audit 호출
- [ ] 멱등성이 필요한 API는 `Idempotency-Key` 헤더 처리 (자세히는 audit-and-idempotency 스킬 참조)
- [ ] 사유(reason) 필드 입력 강제 (재화 지급, 제재, 메일 발송 등)

## 테스트 작성 원칙
- Service 단위 테스트는 필수.
- 메서드명 한글 허용: `재화_지급_시_같은_멱등키로_두_번_호출되면_한_번만_처리된다()`.
- Given-When-Then 구조.
- 멱등성/권한/제약 조건은 반드시 케이스 추가.

## 의존성 추가
회사 명시 스택 외 라이브러리 도입 전 사용자에게 확인. 회사 스택은 CLAUDE.md HOW 섹션 참조.

## 흔한 실수
- ❌ Service에서 다른 도메인의 Repository 직접 호출 → ✅ 다른 도메인의 Service를 거치기
- ❌ Controller에서 Entity 반환 → ✅ DTO 변환
- ❌ `@Transactional`을 private 메서드에 → ✅ public에만 (Spring AOP 한계)
- ❌ 권한 체크 누락 → ✅ 모든 어드민 API는 `@PreAuthorize`