package ru.joke.profiler.core.output.meta;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MethodExitHandle {

    boolean forTimeRegistration();
}
