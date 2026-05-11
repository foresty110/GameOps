package com.example.gameops.audit;

import com.example.gameops.audit.domain.AuditLog;
import com.example.gameops.auth.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

  static final long SYSTEM_ADMIN_ID = 0L;

  private final AuditLogRepository auditLogRepository;

  public AuditAspect(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Around("@annotation(auditable)")
  public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
    Object result = pjp.proceed();
    persistAuditLog(pjp, auditable);
    return result;
  }

  private void persistAuditLog(ProceedingJoinPoint pjp, Auditable auditable) {
    AuditLog log =
        AuditLog.builder()
            .adminId(currentAdminId())
            .actionType(auditable.actionType())
            .targetType(auditable.targetType())
            .reason(extractReason(pjp, auditable))
            .ipAddress(currentIp())
            .build();
    auditLogRepository.save(log);
  }

  private Long currentAdminId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return SYSTEM_ADMIN_ID;
    }
    Object principal = auth.getPrincipal();
    if (principal instanceof AdminPrincipal ap) {
      return ap.id();
    }
    if (principal instanceof Long id) {
      return id;
    }
    return SYSTEM_ADMIN_ID;
  }

  private String currentIp() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return null;
    }
    HttpServletRequest req = attrs.getRequest();
    String forwarded = req.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return req.getRemoteAddr();
  }

  private String extractReason(ProceedingJoinPoint pjp, Auditable auditable) {
    String paramName = auditable.reasonParam();
    if (paramName.isEmpty()) {
      return null;
    }
    MethodSignature signature = (MethodSignature) pjp.getSignature();
    Method method = signature.getMethod();
    String[] names = signature.getParameterNames();
    Object[] args = pjp.getArgs();
    if (names == null) {
      return null;
    }
    for (int i = 0; i < names.length; i++) {
      if (paramName.equals(names[i])) {
        Object value = args[i];
        return value == null ? null : value.toString();
      }
    }
    return null;
  }
}
