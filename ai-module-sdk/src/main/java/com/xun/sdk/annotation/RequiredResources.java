package com.xun.sdk.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiredResources {
    String[] required() default {};

    String[] optional() default {};
}
