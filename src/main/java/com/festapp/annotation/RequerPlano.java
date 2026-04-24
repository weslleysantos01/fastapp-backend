package com.festapp.annotation;

import com.festapp.model.enums.PlanoTipo;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequerPlano {
    PlanoTipo valor();
}