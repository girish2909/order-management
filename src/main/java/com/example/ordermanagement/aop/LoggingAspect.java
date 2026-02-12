package com.example.ordermanagement.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.ordermanagement.service.*.*(..))")
    public void serviceLayerExecution() {
    }

    @Pointcut("execution(* com.example.ordermanagement.controller.*.*(..))")
    public void controllerLayerExecution() {
    }

    @Around("serviceLayerExecution()")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        logger.info("Entering service method: {}.{}", className, methodName);

        long start = System.currentTimeMillis();
        Object proceed;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - start;
            logger.error("Service method {}.{} failed in {} ms with exception: {}", className, methodName,
                    executionTime, e.getMessage());
            throw e;
        }

        long executionTime = System.currentTimeMillis() - start;
        logger.info("Exiting service method: {}.{}. Execution time: {} ms", className, methodName, executionTime);

        return proceed;
    }

    @Around("controllerLayerExecution()")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();

        logger.info("Incoming request: {} {} -> {}.{}", httpMethod, endpoint, className, methodName);

        long start = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - start;
            logger.error("Request {} {} failed in {} ms with exception: {}", httpMethod, endpoint, executionTime,
                    e.getMessage());
            throw e;
        }

        long executionTime = System.currentTimeMillis() - start;
        int statusCode = 200; // Default
        if (result instanceof ResponseEntity<?>) {
            statusCode = ((ResponseEntity<?>) result).getStatusCode().value();
        }

        logger.info("Completed request: {} {} -> Status: {}, Time: {} ms", httpMethod, endpoint, statusCode,
                executionTime);

        return result;
    }
}