/*
 * #%L
 * React API Starter
 * %%
 * Copyright (C) 2009 - 2020 Broadleaf Commerce
 * %%
 * Broadleaf Commerce React Starter
 * 
 * Written in 2017 by Broadleaf Commerce info@broadleafcommerce.com
 * 
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 * 
 * Please Note - The scope of CC0 Public Domain Dedication extends to Broadleaf Commerce React Starter demo application alone. Linked libraries (including all Broadleaf Commerce Framework libraries) are subject to their respective licenses, including the requirements and restrictions specified therein.
 * #L%
 */
package cn.edu.sjtu.ipads.wbridge.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

public class StubApplicationContext implements ApplicationContext {
  private final DefaultListableBeanFactory beanFactory;
  
  public StubApplicationContext(DefaultListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }
  
  @Override
  public String getId() {
    throw new RuntimeException();
  }

  @Override
  public String getApplicationName() {
    throw new RuntimeException();
  }

  @Override
  public String getDisplayName() {
    throw new RuntimeException();
  }

  @Override
  public long getStartupDate() {
    return 0;
  }

  @Override
  public ApplicationContext getParent() {
    throw new RuntimeException();
  }

  @Override
  public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
    throw new RuntimeException();
  }

  @Override
  public BeanFactory getParentBeanFactory() {
    throw new RuntimeException();
  }

  @Override
  public boolean containsLocalBean(String name) {
    throw new RuntimeException();
  }

  @Override
  public boolean containsBeanDefinition(String beanName) {
    throw new RuntimeException();
  }

  @Override
  public int getBeanDefinitionCount() {
    return 0;
  }

  @Override
  public String[] getBeanDefinitionNames() {
    throw new RuntimeException();
  }

  @Override
  public String[] getBeanNamesForType(ResolvableType type) {
    throw new RuntimeException();
  }

  @Override
  public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
    return new String[0];
  }

  @Override
  public String[] getBeanNamesForType(Class<?> type) {
    throw new RuntimeException();
  }

  @Override
  public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
    throw new RuntimeException();
  }

  @Override
  public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
    throw new RuntimeException();
  }

  @Override
  public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
    throw new RuntimeException();
  }

  @Override
  public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
    throw new RuntimeException();
  }

  @Override
  public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
    throw new RuntimeException();
  }

  @Override
  public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
    throw new RuntimeException();
  }

  @Override
  public Object getBean(String name) throws BeansException {
    return beanFactory.getBean(name);
  }

  @Override
  public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
    return beanFactory.getBean(name, requiredType);
  }

  @Override
  public Object getBean(String name, Object... args) throws BeansException {
    return beanFactory.getBean(name, args);
  }

  @Override
  public <T> T getBean(Class<T> requiredType) throws BeansException {
    return beanFactory.getBean(requiredType);
  }

  @Override
  public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
    return beanFactory.getBean(requiredType, args);
  }

  @Override
  public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
    return null;
  }

  @Override
  public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
    return null;
  }

  @Override
  public boolean containsBean(String name) {
    return beanFactory.containsBean(name);
  }

  @Override
  public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
    throw new RuntimeException();
  }

  @Override
  public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
    throw new RuntimeException();
  }

  @Override
  public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
    throw new RuntimeException();
  }

  @Override
  public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
    throw new RuntimeException();
  }

  @Override
  public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
    throw new RuntimeException();
  }

  @Override
  public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
    return null;
  }

  @Override
  public String[] getAliases(String name) {
    throw new RuntimeException();
  }

  @Override
  public void publishEvent(ApplicationEvent applicationEvent) {

  }

  @Override
  public void publishEvent(Object event) {

  }

  @Override
  public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
    throw new RuntimeException();
  }

  @Override
  public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
    throw new RuntimeException();
  }

  @Override
  public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
    throw new RuntimeException();
  }

  @Override
  public Environment getEnvironment() {
    return environment;
  }

  private Environment environment;

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public Resource[] getResources(String locationPattern) throws IOException {
    return new Resource[0];
  }

  @Override
  public Resource getResource(String location) {
    throw new RuntimeException();
  }

  @Override
  public ClassLoader getClassLoader() {
    throw new RuntimeException();
  }
}
