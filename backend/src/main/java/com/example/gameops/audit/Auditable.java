package com.example.gameops.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

  String actionType();

  String targetType();

  /**
   * Name of the method parameter to extract the audit reason from. Empty string means no reason
   * captured.
   */
  String reasonParam() default "";
}
