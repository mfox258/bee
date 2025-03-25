package com.mindskip.xzs.aspect.annotation;
import java.lang.annotation.*;

/**
 *  @Author jiahong.zheng
 *  @Description 日志记录
 *  @Date created in 2023/10/10 14:28
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogRecord {
    String behavior()  default "";
}