package ru.joke.profiler.configuration.meta;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ProfilerDefaultEnumProperty {
}
