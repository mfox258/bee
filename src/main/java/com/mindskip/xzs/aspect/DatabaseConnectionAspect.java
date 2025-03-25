package com.mindskip.xzs.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Aspect
@Component
public class DatabaseConnectionAspect {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionAspect.class);

    @Pointcut("execution(* java.sql.DriverManager.getConnection(..))")
    public void databaseConnectionPointcut() {}

    @Before("databaseConnectionPointcut()")
    public void beforeDatabaseConnection(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof String) {
            String url = (String) args[0];
            String username = null;
            String password = null;
            if (args.length > 1 && args[1] instanceof String) {
                username = (String) args[1];
            }
            if (args.length > 2 && args[2] instanceof String) {
                password = (String) args[2];
            }
            logger.info("尝试连接数据库 - URL: {}, 用户名: {}", url, username);
        }
    }
}