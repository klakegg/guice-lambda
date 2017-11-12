package net.klakegg.guice.lambda.annotation;

import com.google.inject.Module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author erlend
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Dependencies {

    Class<? extends Module>[] value();

}
