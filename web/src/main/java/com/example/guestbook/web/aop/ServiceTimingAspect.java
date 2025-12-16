package com.example.guestbook.web.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceTimingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceTimingAspect.class);

    @Around("execution(* com.example.guestbook.core.service..*(..))")
    public Object measureServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long time = System.currentTimeMillis() - start;
            log.info("{} executed in {} ms", pjp.getSignature(), time);
            return result;
        } catch (Exception ex) {
            long time = System.currentTimeMillis() - start;
            log.warn("{} failed in {} ms: {}", pjp.getSignature(), time, ex.getMessage());
            throw ex;
        }
    }
}
