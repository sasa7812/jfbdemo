/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.springjsf.lazymodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.savvy.jpafilterbuilder.FieldFilter;
import ru.savvy.jpafilterbuilder.FilterConverter;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import ru.savvy.springjsf.entity.AbstractEntity;
import ru.savvy.springjsf.service.lazyloaders.LazyLoadService;

/**
 *
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * @param <T>
 */
public abstract class AbstractPrimefacesLazyModel<T extends AbstractEntity> extends LazyDataModel<T> {

    protected abstract LazyLoadService<T> getLazyLoadService();

    protected abstract Class<T> getEntityClass();

    private final List<FieldFilter> customFilters = new ArrayList<>();

    private List<FieldFilter> argumentFilters;

    /**
     * Since primefaces 4 we need this converter
     * @param mapToConvert
     * @return
     */
    private Map<String, String> convertMaps(Map<String, Object> mapToConvert){
        Map<String , String> result = new HashMap<>();
        for(Map.Entry<String ,Object> entry : mapToConvert.entrySet()){
            if (!(entry.getValue() instanceof String)){
                throw new IllegalArgumentException("Filter values are expected to be strings");
            }
            result.put(entry.getKey(), (String) entry.getValue());
        }
        return result;
    }


    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<T> resultList;
        Map<String, Boolean> sorts = new HashMap<>();
        if (sortOrder != SortOrder.UNSORTED && sortField != null) {
            sorts.put(sortField, sortOrder == SortOrder.ASCENDING);
        }
        if (customFilters == null || customFilters.isEmpty()) {
            FilterConverter fc = new FilterConverter(getLazyLoadService().getEntityManager(), getEntityClass());
            argumentFilters = fc.convert(convertMaps(filters));
            resultList = getLazyLoadService().loadFilterBuilder(first, pageSize, sorts, argumentFilters, getEntityClass());
        }
        else {
            resultList = getLazyLoadService().loadFilterBuilder(first, pageSize, sorts, customFilters, getEntityClass());
        }

        return resultList;
    }

    @Override
    public int getRowCount() {
        if (customFilters == null || customFilters.isEmpty()) {
            return getLazyLoadService().countFilterBuilder(argumentFilters, getEntityClass());
        }
        else {
            return getLazyLoadService().countFilterBuilder(customFilters, getEntityClass());
        }
    }

    @Override
    public Object getRowKey(T object) {
        if (object != null) {
            return object.getId();
        }
        else {
            return null;
        }
    }

    @Override
    public T getRowData(String rowKey) {
        if (rowKey == null || "null".equals(rowKey)) {
            return null;
        }
        return getLazyLoadService().find(Long.parseLong(rowKey), getEntityClass());
    }

    public List<FieldFilter> getCustomFilters() {
        return customFilters;
    }

    public void setCustomFilters(List<FieldFilter> customFilters) {
            this.customFilters.addAll(customFilters);
    }
}
