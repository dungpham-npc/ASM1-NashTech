package com.dungpham.asm1.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.dungpham.asm1.service..*(..))")
    public void serviceMethods() {}

    @Around("@annotation(com.dungpham.asm1.infrastructure.aspect.Logged)")
    public Object logOnlyIfAnnotated(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("[@Logged] Entering: {} with args: {}", methodName, args);

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("[@Logged] Exiting: {} returned: {} (took {}ms)", methodName, formatResult(result), duration);
            return result;
        } catch (Throwable ex) {
            log.error("[@Logged] Exception in: {} with message: {}", methodName, ex.getMessage());
            throw ex;
        }
    }

    private String formatResult(Object result) {
        if (result == null) return "null";
        String resultStr = result.toString();
        return resultStr.length() > 50 ? resultStr.substring(0, 50) + "... [truncated]" : resultStr;
    }

}

