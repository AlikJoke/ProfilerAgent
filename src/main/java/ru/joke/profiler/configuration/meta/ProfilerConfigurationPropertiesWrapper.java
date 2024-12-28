package ru.joke.profiler.configuration.meta;

import java.lang.annotation.*;

@Target({ ElementType.CONSTRUCTOR, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ProfilerConfigurationPropertiesWrapper {

    String prefix() default "";

    Class<? extends ConfigurationParser> parser() default DefaultConfigurationParser.class;

    String conditionalOn() default "";
}
