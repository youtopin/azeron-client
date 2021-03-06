package io.pinect.azeron.client.config;

import io.pinect.azeron.client.EnableAzeronClient;
import io.pinect.azeron.client.service.publisher.Publisher;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

@Configuration
@Log4j2
public class PublisherProxyBeansRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {
    private ClassPathScanner classpathScanner;
    private ClassLoader classLoader;

    public PublisherProxyBeansRegistrar() {
        classpathScanner = new ClassPathScanner(false);
        classpathScanner.addIncludeFilter(new AnnotationTypeFilter(Publisher.class));
    }


    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        String[] basePackages = getBasePackages(importingClassMetadata);
        if (ArrayUtils.isNotEmpty(basePackages)) {
            for (String basePackage : basePackages) {
                createPublisherProxies(basePackage, beanDefinitionRegistry);
            }
        }
    }

    private void createPublisherProxies(String basePackage, BeanDefinitionRegistry beanDefinitionRegistry) {
        try {
            for (BeanDefinition beanDefinition : classpathScanner.findCandidateComponents(basePackage)) {

                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());

                Publisher publisher = clazz.getAnnotation(Publisher.class);

                String beanName = StringUtils.isNotEmpty(publisher.bean())
                        ? publisher.bean() : ClassUtils.getQualifiedName(clazz);

                GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
                proxyBeanDefinition.setBeanClass(clazz);

                ConstructorArgumentValues args = new ConstructorArgumentValues();

                args.addGenericArgumentValue(classLoader);
                args.addGenericArgumentValue(clazz);
                proxyBeanDefinition.setConstructorArgumentValues(args);

                proxyBeanDefinition.setFactoryBeanName("publisherProxyBeanFactory");
                proxyBeanDefinition.setFactoryMethodName("createPublisherProxyBean");

                beanDefinitionRegistry.registerBeanDefinition(beanName, proxyBeanDefinition);
            }
        } catch (Exception e) {
            log.catching(e);
        }
    }

    private String[] getBasePackages(AnnotationMetadata importingClassMetadata) {
        String[] basePackages = null;

        MultiValueMap<String, Object> allAnnotationAttributes =
                importingClassMetadata.getAllAnnotationAttributes(EnableAzeronClient.class.getName());

        if (MapUtils.isNotEmpty(allAnnotationAttributes)) {
            basePackages = (String[]) allAnnotationAttributes.getFirst("basePackages");
        }

        return basePackages;
    }
}
