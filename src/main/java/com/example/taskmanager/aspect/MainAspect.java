package com.example.taskmanager.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MainAspect {
    private static final Logger logger = LoggerFactory.getLogger(MainAspect.class.getName());

    @Before("@annotation(LogExecution)")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Before annotation execution method: {}", joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "execution(public * com.example.taskmanager.service..*(..))", throwing = "ex")
    public void logAfterThrowing(Exception ex) {
        logger.error("AfterThrowing: Метод выбросил исключение: {}", ex.getMessage());
    }

    @AfterReturning(pointcut = "execution(public * com.example.taskmanager.service..*(..))", returning = "result")
    public void logAfterReturning(Object result) {
        logger.info("AfterReturning: Метод успешно выполнен. Возвращён результат: {}", result);
    }

    @Around("@annotation(LogTracking)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("Around: Начало выполнения метода: {}", joinPoint.getSignature());
        Object result;
        try {
            result = joinPoint.proceed();
            logger.info("Around: Метод успешно выполнен: {}", joinPoint.getSignature());
        } catch (Throwable ex) {
            logger.error("Around: Ошибка при выполнении метода: {}", ex.getMessage());
            throw ex;
        }
        return result;
    }

}
