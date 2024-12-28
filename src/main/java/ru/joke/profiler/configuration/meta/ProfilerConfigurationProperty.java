package ru.joke.profiler.configuration.meta;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ProfilerConfigurationProperty {

    String name();

    boolean required() default false;

    String defaultValue() default "";

    Class<? extends ConfigurationPropertyParser> parser() default DefaultConfigurationPropertyParser.class;
}
