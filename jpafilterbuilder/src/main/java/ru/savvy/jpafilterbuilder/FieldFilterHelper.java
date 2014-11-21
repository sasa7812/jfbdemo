/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.jpafilterbuilder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.savvy.jpafilterbuilder.FieldFilter.Option;

/**
 * There are 5 type groups: String Integer - all interger persistable number *
 * types Float - all float persistable number types Boolean Date - all date
 * persistable
 *
 * @author <a href=mailto:sasa7812@gmail.com>Alexander Nikitin</a>
 */
public class FieldFilterHelper {

    private class FieldFilterMutable {

        private String field;

        private Object typedValue;

        private EnumSet<FieldFilter.Option> options;



        public FieldFilterMutable() {

        }

        public FieldFilterMutable(String field, Object typedValue, EnumSet<FieldFilter.Option> options) {
            this.field = field;
            this.typedValue = typedValue;
            this.options = options;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getValue() {
            return typedValue;
        }

        public void setValue(Object typedValue) {
            this.typedValue = typedValue;
        }

        public EnumSet<Option> getOptions() {
            return options;
        }

    }

    private final static Map<FieldType, FieldOptions> optionsSet = new HashMap<>();

    private final Map<String, FieldFilterMutable> fields = new HashMap<>();

    private Log logger = LogFactory.getLog(this.getClass());

    public enum FieldType {

        STRING,
        INTEGER,
        FLOAT,
        DATE,
        BOOLEAN
    }

    private static class FieldOptions {

        private final Map<String, Option> switches;

        private final Map<String, Option> options;

        public FieldOptions(Map<String, Option> switches, Map<String, Option> options) {
            this.switches = switches;
            this.options = options;
        }

        public Map<String, Option> getSwitches() {
            return Collections.unmodifiableMap(switches);
        }

        public Map<String, Option> getOptions() {
            return Collections.unmodifiableMap(options);
        }
    }

    static {
        Map<String, Option> STRING_SWITCHERS = new HashMap<>();
        final String BUNDLE_LOCATION = "Bundle";
        STRING_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("ANY PART MATCH"), Option.PART_STRING);
        STRING_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("EXACT MATCH"), Option.FULL_STRING);
        STRING_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("BEGINNING MATCH"), Option.HEAD_STRING);
        STRING_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("END MATCH"), Option.TAIL_STRING);

        Map<String, Option> STRING_OPTIONS = new HashMap<>();
        STRING_OPTIONS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("OR NULL"), Option.OR_NULL);
        STRING_OPTIONS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("CASE SENSITIVE"), Option.CS_STRING);

        optionsSet.put(FieldType.STRING, new FieldOptions(STRING_SWITCHERS, STRING_OPTIONS));

        Map<String, Option> INTEGER_SWITCHERS = new HashMap<>();
        INTEGER_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("LESS THAN"), Option.LT);
        INTEGER_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("GREATER THAN"), Option.GT);
        INTEGER_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("LESS OR EQUAL"), Option.LE);
        INTEGER_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("GREATER OF EQUAL"), Option.GE);
        INTEGER_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("EQUAL"), Option.EQ);
        INTEGER_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("NOT EQUAL"), Option.NE);

        Map<String, Option> INTEGER_OPTIONS = new HashMap<>();
        INTEGER_OPTIONS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("OR NULL"), Option.OR_NULL);

        optionsSet.put(FieldType.INTEGER, new FieldOptions(INTEGER_SWITCHERS, INTEGER_OPTIONS));

        optionsSet.put(FieldType.FLOAT, new FieldOptions(INTEGER_SWITCHERS, INTEGER_OPTIONS));

        optionsSet.put(FieldType.DATE, new FieldOptions(INTEGER_SWITCHERS, INTEGER_OPTIONS));

        Map<String, Option> BOOLEAN_SWITCHERS = new HashMap<>();
        BOOLEAN_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("EQUAL"), Option.EQ);
        BOOLEAN_SWITCHERS.put(java.util.ResourceBundle.getBundle(BUNDLE_LOCATION).getString("NOT EQUAL"), Option.NE);

        optionsSet.put(FieldType.DATE, new FieldOptions(BOOLEAN_SWITCHERS, INTEGER_OPTIONS));
        optionsSet.put(FieldType.BOOLEAN, new FieldOptions(BOOLEAN_SWITCHERS,INTEGER_OPTIONS));
    }

    private void initIfEmpty(String fieldName) {
        if (!fields.containsKey(fieldName)) {
            fields.put(fieldName, new FieldFilterMutable(fieldName, null, EnumSet.noneOf(Option.class)));
        }
    }

    public void clearAllFilters() {
        for (FieldFilterMutable ff : fields.values()) {
            ff.getOptions().clear();
            ff.setValue(null);
        }
    }

    public Class getFieldNativeType(Class clazz, String field) {
        try {
            if (!field.contains(".")) {
                Field f = clazz.getDeclaredField(field);
                return f.getType();
            } else {
                int propertyDepth=1;
                String [] properties = field.split("\\.");
                String fld = properties[0];
                StringBuilder builder= new StringBuilder("");
                do {
                    builder.append(properties[propertyDepth]);
                    propertyDepth++;
                } while (propertyDepth<(properties.length-1));
                Field f = clazz.getDeclaredField(properties[0]);
                return f.getType();
            }
        } catch (Exception ex) {
            Logger.getLogger(FieldFilterHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return String.class;
    }

    public String getFullField(String fieldname) {
        initIfEmpty(fieldname);
        StringBuilder sb = new StringBuilder(fieldname);
        sb.append("( "); // it is a space after "(" to make deletion of none existing comma possible
        for (Option o : fields.get(fieldname).getOptions()) {
            sb.append(o.name()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        sb.append(")");
        if (logger.isDebugEnabled()){
            logger.debug(this.getClass().getCanonicalName() + "#getFullField result: " + sb);
        }
        return sb.toString();
    }

    /**
     *
     * @return for fields where typedValue set
     */
    public List<FieldFilter> getAllFilters() {
        List<FieldFilter> result = new ArrayList<>();
        for (FieldFilterMutable ffm : fields.values()) {
            if (ffm.getValue() != null) {
                if (!(ffm.getValue() instanceof String) || !((String) (ffm.getValue())).trim().isEmpty()) {
                    result.add(new FieldFilter(ffm.getField(), ffm.getValue(), ffm.getOptions()));
                    if (logger.isDebugEnabled()){
                        logger.debug(this.getClass().getCanonicalName() + ".getAllFilters() filter helper found value of type: " +  ffm.getValue().getClass());
                    }
                }
            }
        }
        return result;
    }

    public Map<String, Option> getAllOptions(String fieldType) {
        FieldType type;
        type = FieldType.valueOf(fieldType);

        return optionsSet.get(type).getOptions();
    }

    public Map<String, Option> getAllSwitches(String fieldType) {
        FieldType type;
        type = FieldType.valueOf(fieldType);

        return optionsSet.get(type).getSwitches();
    }

    public interface Accessor {

        public String getSwitchOptions();

        public void setSwitchOptions(String option);

        public List<String> getOptions();

        public void setOptions(List<String> options);

        public String getFieldName();

        public Object getValue();

        public void setValue(Object typedValue);

    }

    public Accessor getAccessor(final String fieldName, final String fieldType) {

        initIfEmpty(fieldName);

        return new Accessor() {

            @Override
            public String getSwitchOptions() {
                List<Option> currentSwitch = new ArrayList<>(getAllSwitches(fieldType).values());
                currentSwitch.retainAll(fields.get(fieldName).getOptions());
                if (!currentSwitch.isEmpty()) {
                    return currentSwitch.get(0).name();
                }
                else {
                    return null;
                }
            }

            @Override
            public void setSwitchOptions(String option) {
                if (option != null && option.length() > 0) {
                    fields.get(fieldName).getOptions().removeAll(getAllSwitches(fieldType).values());
                    fields.get(fieldName).getOptions().add(Option.valueOf(option));
                }
            }

            @Override
            public List<String> getOptions() {
                List<Option> currentOptions = new ArrayList<>(getAllOptions(fieldType).values());
                currentOptions.retainAll(fields.get(fieldName).getOptions());
                List<String> result = new ArrayList<>();
                for (Option o : currentOptions) {
                    result.add(o.name());
                }
                return result;
            }

            @Override
            public void setOptions(List<String> options) {
                if (options != null) {
                    fields.get(fieldName).getOptions().removeAll(getAllOptions(fieldType).values());
                    for (String o : options) {
                        fields.get(fieldName).getOptions().add(Option.valueOf(o));
                    }
                }
            }

            @Override
            public String getFieldName() {
                return fieldName;
            }

            @Override
            public Object getValue() {
                return fields.get(fieldName).getValue();
            }

            /**
             * does not accept empty or blank strings
             *
             * @param value
             */
            @Override
            public void setValue(Object value) {
                if (value != null && (value instanceof String) && ((String) value).trim().isEmpty()) {
                    value = null;
                }
                fields.get(fieldName).setValue(value);
            }
        };

    }

}
