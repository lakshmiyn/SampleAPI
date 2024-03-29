package com.skillsoft.provisioning.api.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Make this annotation accessible at runtime via reflection.
@Target({ElementType.METHOD})       // This annotation can only be applied to class methods.
public @interface RunOnEnvironments {
    String[] environments() default "";
}
