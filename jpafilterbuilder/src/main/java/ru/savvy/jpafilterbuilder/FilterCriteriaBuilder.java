/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.jpafilterbuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @param <T>
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * @todo make inner joins for compound fields garanteed to have joied data
 */
public class FilterCriteriaBuilder<T> {

    //private final CriteriaQuery<T> query;
    private final Class<T> clazz;

    //private Root<T> root;
    private final CriteriaBuilder cb;

    private final PredicateBuilder pb;

    private final EnumSet<Option> options;

    private final Metamodel metamodel;

    private final List<FieldFilter> filters = new ArrayList<>();

    private final LinkedHashMap<String, Boolean> orders = new LinkedHashMap<>();

    private Log logger = LogFactory.getLog(this.getClass());

    public enum Option {

        /**
         * Makes OR filters conditions instead of AND by default
         */
        OR_FILTERS
    }

    public static final Map<Class<?>, Class<?>> WRAPPERS_TO_PRIMITIVES
            = new HashMap<Class<?>, Class<?>>();
    public static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS
            = new HashMap<Class<?>, Class<?>>();

    static {
        PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
        PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
        PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
        PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
        PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
        PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);

        WRAPPERS_TO_PRIMITIVES.put(Boolean.class, boolean.class);
        WRAPPERS_TO_PRIMITIVES.put(Byte.class, byte.class);
        WRAPPERS_TO_PRIMITIVES.put(Character.class, char.class);
        WRAPPERS_TO_PRIMITIVES.put(Double.class, double.class);
        WRAPPERS_TO_PRIMITIVES.put(Float.class, float.class);
        WRAPPERS_TO_PRIMITIVES.put(Integer.class, int.class);
        WRAPPERS_TO_PRIMITIVES.put(Long.class, long.class);
        WRAPPERS_TO_PRIMITIVES.put(Short.class, short.class);
        WRAPPERS_TO_PRIMITIVES.put(Void.class, void.class);
    }

    /**
     * Creates new instance. Class
     *
     * @param em
     * @param clazz must be JPA Entity annotated
     */
    public FilterCriteriaBuilder(EntityManager em, Class<T> clazz) {
        this.clazz = clazz;
        this.cb = em.getCriteriaBuilder();
        //this.query = cb.createQuery(clazz);
        //this.root = query.from(clazz);
        this.options = EnumSet.noneOf(Option.class);
        this.pb = new PredicateBuilder(cb, options);
        this.metamodel = em.getMetamodel();
    }

    /**
     * Creates new instance from existing query
     *
     * @param em
     * @param query
     */
    public FilterCriteriaBuilder(EntityManager em, CriteriaQuery<T> query) {
        this.cb = em.getCriteriaBuilder();
        this.clazz = query.getResultType();
        //this.query = query;
        this.options = EnumSet.noneOf(Option.class);
        this.pb = new PredicateBuilder(cb, options);
        this.metamodel = em.getMetamodel();
//        Set<Root<?>> roots = query.getRoots();
//        for (Root<?> r : roots) {
//            if (r.getJavaType().equals(this.clazz)) {
//                this.root = (Root<T>) r;
//                break;
//            }
//        }
//        if (this.root == null) {
//            this.root = query.from(this.clazz);
//        }
    }

    /**
     * Checks if filter fieldname is valid for given query. Throws verbose
     * IllegalArgumentException if not. If checkValue = true, checks if filter
     * value is of type of the field or can be converted. This is to avoid
     * unnecessarry joins if filter is invalid
     *
     * @param filter
     * @param checkValue if false than value is not checked
     * @return true if filter is valid
     * @throws IllegalArgumentException
     */
    private boolean checkFilterValid(FieldFilter filter, boolean checkValue) {
        Class<?> fieldType = getJavaType(filter.getField());
        // arrays
        if (filter.getValue().getClass().isArray()) {
            Object[] arr = (Object[]) filter.getValue();
            if (arr.length == 0) {
                return false;
            } else {
                return !checkValue || ((Object[]) filter.getValue())[0].getClass().equals(fieldType);
            }
        }
        if (fieldType.isPrimitive()) {
            return !checkValue || PRIMITIVES_TO_WRAPPERS.get(fieldType).isInstance(filter.getValue());
        } else {
            return !checkValue || fieldType.isInstance(filter.getValue());
        }
    }

    /**
     * This clumsy code is just to get the class of plural attribute mapping
     *
     * @param et
     * @param fieldName
     * @return
     */
    private Class<?> getPluralJavaType(EntityType<?> et, String fieldName) {
        for (PluralAttribute pa : et.getPluralAttributes()) {
            if (pa.getName().equals(fieldName)) {
                switch (pa.getCollectionType()) {
                    case COLLECTION:
                        return et.getCollection(fieldName).getElementType().getJavaType();
                    case LIST:
                        return et.getList(fieldName).getElementType().getJavaType();
                    case SET:
                        return et.getSet(fieldName).getElementType().getJavaType();
                    case MAP:
                        throw new UnsupportedOperationException("Entity Map mapping unsupported for entity: " + et.getName() + " field name: " + fieldName);
                }
            }
        }
        throw new IllegalArgumentException("Field " + fieldName + " of entity " + et.getName() + " is not a plural attribute");
    }

    /**
     * Returns Java type of the fieldName
     *
     * @param fieldName
     * @return
     * @throws IllegalArgumentException if fieldName isn't valid for given
     *                                  entity
     */
    public Class<?> getJavaType(String fieldName) {

        String[] compoundField = fieldName.split("\\.");
        EntityType et = metamodel.entity(clazz);

        for (int i = 0; i < compoundField.length; i++) {
            if (i < (compoundField.length - 1)) {
                try {
                    Attribute att = et.getAttribute(compoundField[i]);
                    if (att.isCollection()) {
                        et = metamodel.entity(getPluralJavaType(et, compoundField[i]));
                    } else {
                        et = metamodel.entity(et.getAttribute(compoundField[i]).getJavaType());
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    throw new IllegalArgumentException(
                            "Illegal field name " + fieldName + " (" + compoundField[i] + ") for root type " + clazz
                    );
                }
            } else {
                try {
                    return et.getAttribute(compoundField[i]).getJavaType();
                } catch (IllegalArgumentException | IllegalStateException e) {
                    throw new IllegalArgumentException(
                            "Illegal field name " + fieldName + " (" + compoundField[i] + ") for root type " + clazz
                    );
                }
            }
        }
        return null; // should never be reached
    }

    /**
     * Adds filters to the query. Preserves existing filters.
     *
     * @param conditions
     * @return this, itself for chain calls
     */
    public FilterCriteriaBuilder<T> addFilters(List<FieldFilter> conditions) {
        if (conditions != null)
            for (FieldFilter f : conditions) {
                if (checkFilterValid(f, true)) {
                    filters.add(f);
                } else {
                    logger.error("Could not apply filter for field: " + f.getField() + " value: " + f.getValue() + " of type: " + f.getValue().getClass());
                }
            }
        // keep it sorted to avoid inner/outer joins messup
        Collections.sort(filters);

        return this;
    }

    /**
     * @param rootPath
     * @param query
     */
    private void applyFilters(Root<T> rootPath, CriteriaQuery<?> query) {

        List<Predicate> predicates = new ArrayList<>();

        for (FieldFilter filter : filters) {
            Path<?> path;
            if (filter.getOptions().contains(FieldFilter.Option.OR_NULL)) {
                path = getCompoundJoinedPath(rootPath, filter.getField(), true);
            } else {
                path = getCompoundJoinedPath(rootPath, filter.getField(), false);
            }

            Predicate p = pb.getPredicate(path, filter);
            if (p != null) {
                predicates.add(p);
            }
        }
        // this does not work for Hibernate!!!
        if (query.getRestriction() != null) {
            predicates.add(query.getRestriction());
        }
        if (options.contains(Option.OR_FILTERS)) {
            query.where(cb.or(predicates.toArray(new Predicate[0])));
        } else {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

    }

    private void applyOrders(Root<T> rootPath, CriteriaQuery<T> query) {
        List<Order> orderList = new ArrayList<>();

        for (Map.Entry<String, Boolean> me : orders.entrySet()) {
            Path<?> path = getCompoundJoinedPath(rootPath, me.getKey(), true);
            if (me.getValue() == null || me.getValue().equals(true)) {
                orderList.add(cb.asc(path));
            } else {
                orderList.add(cb.desc(path));
            }
        }
        query.orderBy(orderList);
    }

    /**
     * Drop all the options for field in order we do not need them
     *
     * @param orderByField
     * @return
     */
    private String clearOrderOptions(String orderByField) {
        int idx = orderByField.indexOf("(");

        if (idx < 0) { // not found
            return orderByField;
        }

        return orderByField.substring(0, orderByField.indexOf("("));
    }

    /**
     * Adds order by expressions to the tail of already existing orders of query
     *
     * @param orders
     * @return
     */
    public FilterCriteriaBuilder<T> addOrders(Map<String, Boolean> orders) {

        for (Map.Entry<String, Boolean> me : orders.entrySet()) {
            checkFilterValid(new FieldFilter(clearOrderOptions(me.getKey()), "", EnumSet.noneOf(FieldFilter.Option.class)), false);
            this.orders.put(clearOrderOptions(me.getKey()), me.getValue());
        }
        return this;
    }

    /**
     * @param fieldName
     * @return Path of compound field to the primitive type
     */
    private Path<?> getCompoundJoinedPath(Root<T> rootPath, String fieldName, boolean outer) {
        String[] compoundField = fieldName.split("\\.");

        Join join;

        if (compoundField.length == 1) {
            return rootPath.get(compoundField[0]);
        } else {
            join = reuseJoin(rootPath, compoundField[0], outer);
        }

        for (int i = 1; i < compoundField.length; i++) {
            if (i < (compoundField.length - 1)) {
                join = reuseJoin(join, compoundField[i], outer);
            } else {
                return join.get(compoundField[i]);
            }
        }

        return null;
    }

    // trying to find already existing joins to reuse
    private Join reuseJoin(From<?, ?> path, String fieldName, boolean outer) {
        for (Join join : path.getJoins()) {
            if (join.getAttribute().getName().equals(fieldName)) {
                if ((join.getJoinType() == JoinType.LEFT) == outer) {
                    logger.debug("Reusing existing join for field " + fieldName);
                    return join;
                }
            }
        }
        return outer ? path.join(fieldName, JoinType.LEFT) : path.join(fieldName);
    }


    /**
     * Get sorting field
     *
     * @param clazz
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private Field getSortAnnotation(Class clazz) throws IllegalArgumentException, IllegalAccessException {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getAnnotation(DefaultOrder.class) != null) {
                return f;
            }
        }
        //if not found, search in superclass. todo recursive search
        for (Field f : clazz.getSuperclass().getDeclaredFields()) {
            if (f.getAnnotation(DefaultOrder.class) != null) {
                return f;
            }
        }
        return null;
    }


    /**
     * Resulting query with filters and orders, if orders are empty, than makes
     * default ascending ordering by root id to prevent paging confuses
     *
     * @return
     */
    public CriteriaQuery<T> getQuery() {
        CriteriaQuery<T> query = cb.createQuery(clazz);
        Root<T> root = query.from(clazz);
        applyFilters(root, query);
        applyOrders(root, query);

        // add default ordering
        if (query.getOrderList() == null || query.getOrderList().isEmpty()) {
            EntityType<T> entityType = root.getModel();
            try {
                Field sortField = getSortAnnotation(entityType.getBindableJavaType());
                if (sortField == null)
                    query.orderBy(cb.asc(root.get(entityType.getId(entityType.getIdType().getJavaType()).getName())));
                else {
                    DefaultOrder order = sortField.getAnnotation(DefaultOrder.class);
                    if (order.asc()) {
                        query.orderBy(cb.asc(root.get(sortField.getName())));
                    } else {
                        query.orderBy(cb.desc(root.get(sortField.getName())));
                    }

                }
            } catch (Exception ex) {
                logger.warn("In" + this.getClass().getName(), ex);
            }
        }
        return query;
    }

    /**
     * @return
     */
    public CriteriaQuery<Long> getCountQuery() {
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<T> root = q.from(clazz);
        q.select(cb.count(root));
        applyFilters(root, q);
        return q;
    }

    public EnumSet<Option> getOptions() {
        return options;
    }

}
