package com.example.test;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TestAspect {
    
    @Before("execution(* com.example..*(..))")
    public void beforeAdvice() {
        System.out.println("Before advice");
    }
    
    @After("execution(* com.example..*(..))")
    public void afterAdvice() {
        System.out.println("After advice");
    }
    
    @Around("execution(* com.example.service.*.*(..))")
    public void aroundAdvice() {
        System.out.println("Around advice");
    }
}
