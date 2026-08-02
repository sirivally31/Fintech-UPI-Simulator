package com.example.demo.aspect;

import com.example.demo.annotation.Auditable;
import com.example.demo.entity.AuditAction;
import com.example.demo.entity.AuditLog;
import com.example.demo.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Spring AOP Aspect for automatically capturing audit events across REST Controllers and Auditable services.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) && !within(com.example.demo.controller.AuditController)")
    public void restControllerPointcut() {
    }

    @Pointcut("@annotation(com.example.demo.annotation.Auditable)")
    public void auditableMethodPointcut() {
    }

    @Around("restControllerPointcut() || auditableMethodPointcut()")
    public Object auditExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        boolean success = true;
        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            success = false;
            exception = t;
            throw t;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            try {
                recordAuditTrail(joinPoint, executionTime, success, exception);
            } catch (Exception e) {
                log.error("Error occurred while recording audit trail in AuditAspect", e);
            }
        }
    }

    private void recordAuditTrail(ProceedingJoinPoint joinPoint, long executionTime, boolean success, Throwable exception) {
        HttpServletRequest request = getHttpServletRequest();
        HttpServletResponse response = getHttpServletResponse();

        String requestMethod = request != null ? request.getMethod() : "N/A";
        String requestUri = request != null ? request.getRequestURI() : "N/A";
        String clientIp = getClientIpAddress(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : "N/A";

        String username = getAuthenticatedUsername();
        Long userId = null; // Will remain null if not embedded in Principal

        AuditAction action = determineAuditAction(joinPoint, requestUri, requestMethod, success);
        String module = determineModule(joinPoint, requestUri);

        Integer httpStatus = response != null ? response.getStatus() : (success ? 200 : 500);
        if (!success && exception != null) {
            String exName = exception.getClass().getSimpleName();
            if (exName.contains("BadCredentials") || exName.contains("Authentication")) {
                httpStatus = 401;
            } else if (exName.contains("AccessDenied") || exName.contains("Security")) {
                httpStatus = 403;
            } else if (exName.contains("NotFound")) {
                httpStatus = 404;
            } else if (exName.contains("IllegalArgument") || exName.contains("Validation")) {
                httpStatus = 400;
            }
        }

        String responseBodySnippet = success ? "SUCCESS" : (exception != null ? exception.getMessage() : "FAILED");
        if (responseBodySnippet != null && responseBodySnippet.length() > 500) {
            responseBodySnippet = responseBodySnippet.substring(0, 500);
        }

        AuditLog auditLog = new AuditLog(
                username,
                userId,
                action,
                module,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                requestMethod,
                requestUri,
                clientIp,
                userAgent,
                "Audit Captured",
                responseBodySnippet,
                httpStatus,
                success,
                executionTime
        );

        auditService.log(auditLog);
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private HttpServletResponse getHttpServletResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ANONYMOUS";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return "UNKNOWN";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "UNKNOWN";
    }

    private AuditAction determineAuditAction(ProceedingJoinPoint joinPoint, String uri, String method, boolean success) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method methodObj = signature.getMethod();
        Auditable auditable = methodObj.getAnnotation(Auditable.class);
        if (auditable != null && auditable.action() != AuditAction.SYSTEM) {
            return auditable.action();
        }

        if (uri.contains("/auth/login") || uri.contains("/login")) {
            return success ? AuditAction.LOGIN : AuditAction.LOGIN_FAILED;
        }
        if (uri.contains("/users/register") || uri.contains("/register")) {
            return AuditAction.REGISTER;
        }
        if (uri.contains("/transfers")) {
            return AuditAction.TRANSFER;
        }
        if (uri.contains("/qr")) {
            return AuditAction.QR_PAYMENT;
        }
        if (uri.contains("/settlement")) {
            return AuditAction.SETTLEMENT;
        }
        if (uri.contains("/fraud")) {
            return AuditAction.FRAUD_BLOCK;
        }
        if (uri.contains("/admin")) {
            return AuditAction.ADMIN_ACTION;
        }
        if (uri.contains("/notifications")) {
            return AuditAction.NOTIFICATION;
        }

        switch (method.toUpperCase()) {
            case "POST":
                return AuditAction.CREATE;
            case "PUT":
            case "PATCH":
                return AuditAction.UPDATE;
            case "DELETE":
                return AuditAction.DELETE;
            default:
                return AuditAction.SYSTEM;
        }
    }

    private String determineModule(ProceedingJoinPoint joinPoint, String uri) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method methodObj = signature.getMethod();
        Auditable auditable = methodObj.getAnnotation(Auditable.class);
        if (auditable != null && !auditable.module().isBlank() && !"GENERAL".equals(auditable.module())) {
            return auditable.module();
        }

        if (uri.contains("/auth") || uri.contains("/login")) return "AUTH";
        if (uri.contains("/users")) return "USER_MANAGEMENT";
        if (uri.contains("/transfers")) return "MONEY_TRANSFER";
        if (uri.contains("/qr")) return "MERCHANT_QR";
        if (uri.contains("/autopay")) return "AUTOPAY";
        if (uri.contains("/beneficiaries")) return "BENEFICIARY";
        if (uri.contains("/fraud")) return "FRAUD_ENGINE";
        if (uri.contains("/settlement")) return "SETTLEMENT";
        if (uri.contains("/admin")) return "ADMIN";
        if (uri.contains("/notifications")) return "NOTIFICATION";
        if (uri.contains("/accounts")) return "BANK_ACCOUNT";
        if (uri.contains("/vpa")) return "UPI_ID";

        return joinPoint.getSignature().getDeclaringType().getSimpleName().replace("Controller", "").toUpperCase();
    }
}
