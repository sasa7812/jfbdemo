/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.jpafilterbuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.savvy.jpafilterbuilder.FieldFilter.Option;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static ru.savvy.jpafilterbuilder.FilterConverterHelper.*;

/**
 * @author <a href=mailto:sasa7812@gmail.com>Alexander Nikitin</a>
 */
public class FilterConverter {

    private final FilterCriteriaBuilder fcb;

    private Log logger = LogFactory.getLog(this.getClass());

    public FilterConverter(EntityManager em, Class<?> clazz) {
        fcb = new FilterCriteriaBuilder(em, clazz);
    }

    public FilterConverter(FilterCriteriaBuilder fcb) {
        this.fcb = fcb;
    }

    private Map.Entry<String, EnumSet<Option>> extractOptions(String fieldName) throws IllegalArgumentException {
        int firstPar = fieldName.indexOf("(");
        int secondPar = fieldName.indexOf(")");

        EnumSet<Option> options = EnumSet.noneOf(Option.class);

        if (secondPar > firstPar && firstPar >= 0) {
            String field = fieldName.substring(firstPar + 1, secondPar);
            if (field.trim().length() > 0) {
                String sopts[] = field.split(",");
                for (String sopt : sopts) {
                    String s = sopt.trim();
                    try {
                        options.add(Option.valueOf(s.toUpperCase(Locale.US)));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Field option \"" + s + "\" does not exist");
                    }
                }
            }
            return new HashMap.SimpleEntry<>(fieldName.substring(0, firstPar).trim(), options);

        }
        return new HashMap.SimpleEntry<>(fieldName, options);
    }

    public List<FieldFilter> convert(Map<String, String> conditions) {
        List<FieldFilter> result = new ArrayList<>();
        if (conditions!=null)
         for (Map.Entry<String, String> entry : conditions.entrySet()) {
            FieldFilter ff = convert(entry.getKey(), entry.getValue());
            if (ff != null) {
                result.add(ff);
            }
         }
        return result;
    }

    public FieldFilter convert(String fieldName, String fieldValue) {
        Map.Entry<String, EnumSet<Option>> optionsExtracted = extractOptions(fieldName);

        Class<?> fieldType = fcb.getJavaType(optionsExtracted.getKey());
        Object value = null;
        if (fieldType.isPrimitive())
            fieldType = FilterCriteriaBuilder.PRIMITIVES_TO_WRAPPERS.get(fieldType);
        if (fieldType.equals(BigDecimal.class)
                || fieldType.equals(Double.class)
                || fieldType.equals(Float.class)) {
            value = floatingPointConvert(fieldValue, fieldType);
        } else if (fieldType.equals(Long.class)
                || fieldType.equals(BigInteger.class)
                || fieldType.equals(Integer.class)
                || fieldType.equals(Short.class)
                || fieldType.equals(Byte.class)) {
            if(fieldValue.contains(",")){
                value = integerArrayConvert(fieldValue,fieldType).toArray();
            }else {
                value = integerConvert(fieldValue, fieldType);
            }
        } else if (fieldType.equals(Boolean.class)) {
            value = booleanConvert(fieldValue,fieldType);
        } else if (fieldType.equals(java.util.Date.class)
                || fieldType.equals(java.util.Calendar.class)
                || fieldType.equals(java.sql.Date.class)
                || fieldType.equals(java.sql.Timestamp.class)
                || fieldType.equals(java.sql.Time.class)) {
            value = dateConvert(fieldValue);
        } else if (fieldType.equals(String.class)) {
            value = fieldValue;
        }
        if (value != null) {
            return new FieldFilter(optionsExtracted.getKey(), value, optionsExtracted.getValue());
        } else {
            if (logger.isWarnEnabled()){
                logger.warn("Could not convert value \"" + fieldValue + "\" to field type: \"" + fieldType + "\" of field \"" + fieldName + "\"");
            }
            return null;
        }
    }

}
