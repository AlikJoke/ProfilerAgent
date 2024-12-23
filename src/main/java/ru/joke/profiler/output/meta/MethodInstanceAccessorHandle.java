package ru.joke.profiler.output.meta;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MethodInstanceAccessorHandle {
}
