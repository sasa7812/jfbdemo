/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */

package ru.savvy.springjsf.system;

import com.sun.faces.spi.InjectionProviderException;
import com.sun.faces.vendor.WebContainerInjectionProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.context.FacesContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Custom injector for Mojarra
 * Very simple, it injects only Spring components into fields
 * Just enough for this example
 *
 * @author sasa <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
public class CustomJsfInjectionProvider extends WebContainerInjectionProvider {

    private Log logger = LogFactory.getLog(this.getClass());

    @Override
    public void inject(Object managedBean) throws InjectionProviderException {
        // no op in parent class
        if (logger.isDebugEnabled()) {
            logger.debug("Making injections to bean " + managedBean.getClass().getCanonicalName());
        }
        injectFields(managedBean, javax.inject.Inject.class);
    }

    private void injectFields(Object bean, Class<? extends Annotation> annotation) throws InjectionProviderException {
        // get current WebApplicationContext
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            throw new InjectionProviderException("Unable to get current Faces context", new NullPointerException("Faces context is null"));
        }
        WebApplicationContext wac = FacesContextUtils.getWebApplicationContext(fc);
        if (wac == null) {
            throw new InjectionProviderException("Unable to get Spring WebApplicationContext", new NullPointerException("WebApplicationContext is null"));
        }
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getAnnotationsByType(annotation).length > 0) {
                Class<?> typeClazz = f.getType();
                Object injection;
                try {
                    injection = wac.getBean(typeClazz);
                } catch (NoSuchBeanDefinitionException e) {
                    e.printStackTrace();
                    throw new InjectionProviderException("Unable to inject bean of type " + typeClazz.toString(), e);
                }
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                try {
                    f.set(bean, injection);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Injected field of type " + f.getType().getCanonicalName() + " into " + bean.getClass().getCanonicalName());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
