package com.saasovation.common.spring;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

/**
 * Created by marten on 09-03-17.
 */
public class HibernateSessionFactoryProcessor implements BeanPostProcessor, ResourceLoaderAware {

    private ResourcePatternResolver resourceLoader;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LocalSessionFactoryBean) {
            try {
                Resource[] mappingFiles = resourceLoader.getResources("classpath*:*.hbm.xml");
                System.out.println("Detected: " + mappingFiles.length);
                ((LocalSessionFactoryBean) bean).setMappingLocations(mappingFiles);
            } catch (IOException e) {
                throw new ApplicationContextException("Error detecting hibernate mapping files.", e);
            }

        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader= (ResourcePatternResolver) resourceLoader;
    }
}
