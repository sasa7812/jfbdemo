package ru.savvy.jpafilterbuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Helper class containig converters for simple datatypes
 *
 * @author sasa <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
public class FilterConverterHelper {

    private static Log logger = LogFactory.getLog(FilterConverterHelper.class.getCanonicalName());

    public static <T extends Number> T floatingPointConvert(String value, Class<?> clazz) {
        BigDecimal check;
        try {
            // try to convert to the largest type
            check = new BigDecimal(value);
        } catch (NumberFormatException e) {
            // ignore
            return null;
        }
        if (clazz.equals(Double.class)) {
            return (T) ((Double) check.doubleValue());
        }
        if (clazz.equals(Float.class)) {
            return (T) ((Float) check.floatValue());
        }
        throw new IllegalArgumentException("Type " + clazz + " must be one of the floating point types");
    }

    public static <T extends Number> List<T> integerArrayConvert(String value, Class<?> clazz) {
        // Array conversion
        String[] vals = value.split(",");
        List<T> convertedValues = new ArrayList<>();

        if (vals.length > 0) {
            for (String v : vals) {
                T converted =  (T) integerConvert(v, clazz);
                if (converted != null) {
                    convertedValues.add(converted);
                }
            }
        }
        return convertedValues;

    }

    public static <T extends Number> T integerConvert(String value, Class<?> clazz) {

        BigInteger check;

        try {
            // try to convert to the largest type
            check = new BigInteger(value);
        } catch (NumberFormatException e) {
            // ignore
            return null;
        }

        if (clazz.equals(Long.class)) {
            try {
                return (T) ((Long) check.longValue());
            } catch (ArithmeticException e) {
                return null;
            }
        }
        if (clazz.equals(Integer.class)) {
            try {
                return (T) ((Integer) check.intValue());
            } catch (ArithmeticException e) {
                return null;
            }
        }
        if (clazz.equals(Short.class)) {
            try {
                return (T) ((Short) check.shortValue());
            } catch (ArithmeticException e) {
                return null;
            }
        }
        if (clazz.equals(Byte.class)) {
            try {
                return (T) ((Byte) check.byteValue());
            } catch (ArithmeticException e) {
                return null;
            }
        }
        throw new IllegalArgumentException("Type " + clazz + " must be one of the integer types");
    }

    public static Object booleanConvert(String value, Class clazz) {
        return Boolean.valueOf(value);
    }


    // todo: unit test this for "2015-01-01"

    public static Date dateConvert(String value) {

        if (value == null) {
            return null;
        }

        Map<String, String> formats = new HashMap<>();

        formats.put("dd.MM.yyyy HH:mm", "^\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}$");
        formats.put("dd-MM-yyyy HH:mm", "^\\d{2}-\\d{2}-\\d{4}\\s+\\d{2}:\\d{2}$");
        formats.put("dd/MM/yyyy HH:mm", "^\\d{2}/\\d{2}/\\d{4}\\s+\\d{2}:\\d{2}$");
        formats.put("yyyy/dd/MM HH:mm", "^\\d{4}/\\d{2}/\\d{2}\\s+\\d{2}:\\d{2}$");
        formats.put("yyyy.MM.dd HH:mm", "^\\d{4}\\.\\d{2}\\.\\d{2}\\s+\\d{2}:\\d{2}$");
        formats.put("yyyy-MM-dd HH:mm", "^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}$");
        formats.put("dd.MM.yyyy", "^\\d{2}\\.\\d{2}\\.\\d{4}$");
        formats.put("dd-MM-yyyy", "^\\d{2}-\\d{2}-\\d{4}$");
        formats.put("dd/MM/yyyy", "^\\d{2}/\\d{2}/\\d{4}$");
        formats.put("yyyy/dd/MM", "^\\d{4}/\\d{2}/\\d{2}$");
        formats.put("yyyy.MM.dd", "^\\d{4}\\.\\d{2}\\.\\d{2}$");
        formats.put("yyyy-MM-dd", "^\\d{4}-\\d{2}-\\d{2}$");

        for (Map.Entry<String, String> format : formats.entrySet()) {
            try {
                if (value.trim().matches(format.getValue())) {
                    Date result = new SimpleDateFormat(format.getKey()).parse(value.trim());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Matched format " + format +" for " + value);
                    }
                    return result;
                }
            } catch (ParseException ex) {
                // ignore
            }
        }
        return null;
    }
}
