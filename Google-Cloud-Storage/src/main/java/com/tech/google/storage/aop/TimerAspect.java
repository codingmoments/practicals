package com.tech.google.storage.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimerAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimerAspect.class);

	@Around("@annotation(com.tech.google.storage.aop.LogExecutionTime)")
	public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		Object proceed = joinPoint.proceed();
		long executionTime = System.currentTimeMillis() - start;
		LOGGER.info("{} executed in {} ms", joinPoint.getSignature().getName(), executionTime);
		return proceed;

	}
}
