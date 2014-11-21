/*
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.jpafilterbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author s.polshin
 *  Mark field to be ordered by default by jpafilterbuilder
 *  if no explicite orders were given
 *  if there is no field marked, @Id field is used as sorting key by default
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultOrder {
    public boolean asc() default true;     //Default order ASCENDING
}
