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
package cn.edu.sjtu.ipads.wbridge;

import cn.edu.sjtu.ipads.wbridge.servlet.GetBlogServerlet;
import cn.edu.sjtu.ipads.wbridge.utils.StubApplicationContext;
import gov.nasa.jpf.symbc.Debug;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.ApplicationFilterChain;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.hibernate.id.enhanced.PooledLoOptimizer;
import org.hibernate.mapping.PersistentClass;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import sagan.site.blog.support.BlogAdminController;
import sagan.site.blog.support.BlogController;
import sagan.site.filter.EmptyFilter;
import sagan.site.filter.FrontRecordingFilter;
import sagan.site.filter.StoredProcedureControlFilter;

import javax.persistence.EntityManager;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public class WBridgeBaseEntryPoint {
  public static void main(String[] args) throws Exception {
    if (args.length != 1)
      throw new RuntimeException(
          "Incorrect arguments. Expecting an API name{add,remove,updateQuantity}.");
    injectTestingApi(args);
    final WBridgeBaseEntryPoint entryPoint = new WBridgeBaseEntryPoint();
    Debug.initializeWBridge(entryPoint);
    entryPoint.testWithFilter();
    hack(null, null, null, null, null, null, null, null, null, null);
  }

  private static void injectTestingApi(String[] args) {
    switch (args[0]) {
      case "GetBlog":
        testingServlet = new GetBlogServerlet();
        break;
      default:
        throw new RuntimeException("Unexpected API name:" + args[0]);
    }
    System.err.println(
        "[Warning] Using " + testingServlet.getClass().getName() + " as the testing API.");
  }

  public static EntityManager em;
  public static Map<String, BeanDefinition> beanDefinitionMap;
  public static Map<String, Scope> scopeMap;
  public static Map<String, Object> singletonObjectMap;
  public static ApplicationContext applicationContext;
  public static BeanFactory applicationBeanFactory;
  public static ApplicationContext xmlApplicationContext;
  public static boolean initialized;
  public static Environment env;
  public static Map<String, Object> appCtxAttr;
  public static Map<String, PersistentClass> metadataMap;
  public static BlogController blogController;
  public static BlogAdminController blogAdminController;


  public static ApplicationFilterChain filterChain;
  public static Request request;
  public static Response response;

  public static Servlet testingServlet;

  // a hack stub method used to enlarge the caller's stack frame size
  public static void hack(
      Object o1,
      Object o2,
      Object o3,
      Object o4,
      Object o5,
      Object o6,
      Object o7,
      Object o8,
      Object o9,
      Object o10) {}

  @SuppressWarnings("all")
  public void initApplicationContext() throws NoSuchMethodException {
    final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    if (beanDefinitionMap.isEmpty())
      throw new RuntimeException(
          "Invalid injected bean factory, is the expanded object for bean factory correct?");
    beanDefinitionMap.forEach(beanFactory::registerBeanDefinition);

    if (scopeMap.isEmpty())
      throw new RuntimeException(
          "Invalid injected bean factory, is the expanded object for bean factory correct?");
    scopeMap.forEach(beanFactory::registerScope);

    if (singletonObjectMap.isEmpty())
      throw new RuntimeException(
          "Invalid injected bean factory, is the expanded object for bean factory correct?");

    Method method =
        beanFactory
            .getClass()
            .getSuperclass()
            .getSuperclass()
            .getSuperclass()
            .getSuperclass()
            .getDeclaredMethod("addSingleton", String.class, Object.class);
    method.setAccessible(true);
    singletonObjectMap.forEach(
        (k, v) -> {
          try {
            method.invoke(beanFactory, k, v);
          } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        });

    final StubApplicationContext appCtx = new StubApplicationContext(beanFactory);
    beanFactory.registerSingleton("org.springframework.core.env.Environment", env);
    applicationContext = appCtx;
    appCtx.setEnvironment(env);
    // final GenericXmlApplicationContext genericXmlApplicationContext =
    //     new GenericXmlApplicationContext(
    //         "bl-common-applicationContext-entity.xml",
    //         "bl-profile-applicationContext-entity.xml",
    //         "applicationContext-entity.xml",
    //         "bl-cms-applicationContext-entity.xml",
    //         "bl-open-admin-applicationContext-entity.xml",
    //         "bl-menu-applicationContext-entity.xml",
    //         "bl-framework-applicationContext-entity.xml");
    // // must trigger eager initialization beforehead
    // beanFactory.getBean("blActivityStateManager");
    //
    // Objects.requireNonNull(
    //     genericXmlApplicationContext.getBean("org.broadleafcommerce.profile.core.domain.Customer"));
    // Objects.requireNonNull(
    //     genericXmlApplicationContext.getBean("org.broadleafcommerce.profile.core.domain.Address"));
    // for (String name : genericXmlApplicationContext.getBeanDefinitionNames()) {
    //   beanFactory.registerBeanDefinition(
    //       name, genericXmlApplicationContext.getBeanDefinition(name));
    // }
    // xmlApplicationContext = genericXmlApplicationContext;
    applicationBeanFactory = beanFactory;

    // // register transaction manager
    // final LifecycleAwareJpaTransactionManager manager = new LifecycleAwareJpaTransactionManager();
    // manager.setPersistenceUnitName("blPU");
    // manager.setJpaDialect(new HibernateJpaDialect());
    // manager.setEntityManagerFactory(em.getEntityManagerFactory());
    // beanFactory.registerAlias("blTransactionManager", "transactionManager");
    // systemPropertiesDao = (SystemPropertiesDao) beanFactory.getBean("blSystemPropertiesDao");
    // new HibernateMappingProvider(
    //     Objects.requireNonNull(metadataMap, "Invalid Hibernate Mapping metadata"));
    // // sanity check
    // Objects.requireNonNull(
    //     HibernateMappingProvider.getMapping("org.broadleafcommerce.core.catalog.domain.SkuImpl"));
    initialized = true;
  }

  @SuppressWarnings("all")
  public static void removeUselessFilters(ApplicationFilterConfig[] filterConfigs)
      throws NoSuchFieldException, IllegalAccessException {
    for (int idx = 0; idx < filterConfigs.length; idx++) {
      ApplicationFilterConfig filterConfig = filterConfigs[idx];
      if (filterConfig == null) continue;
      final Class<? extends ApplicationFilterConfig> filterConfigClass = filterConfig.getClass();
      final Field filterDefFld = filterConfigClass.getDeclaredField("filterDef");
      filterDefFld.setAccessible(true);
      final FilterDef filterDef = (FilterDef) filterDefFld.get(filterConfig);
      final Filter filter = filterDef.getFilter();
      final Field filterFld = filterConfigClass.getDeclaredField("filter");
      filterFld.setAccessible(true);
      // filter out the web socket/encoding related
      if (filter instanceof org.apache.tomcat.websocket.server.WsFilter
          || filter instanceof org.springframework.web.filter.CharacterEncodingFilter
          || filter instanceof StoredProcedureControlFilter
          || filter instanceof FrontRecordingFilter) {
        final FilterDef filterDefNew = new FilterDef();
        final EmptyFilter emptyFilter = new EmptyFilter();
        filterDefNew.setFilter(emptyFilter);
        filterDefNew.setFilterName("saganEmpty" + idx);
        filterDefNew.setFilterClass(EmptyFilter.class.getName());
        filterDefFld.set(filterConfig, filterDefNew);
        filterFld.set(filterConfig, emptyFilter);
      }
    }
  }

  @SuppressWarnings("all")
  public void testWithFilter() throws Exception {
    initApplicationContext();

    // skip the first front end recording filter
    final Field servletFld = filterChain.getClass().getDeclaredField("servlet");
    final Field posFld = filterChain.getClass().getDeclaredField("pos");
    posFld.setAccessible(true);
    posFld.set(filterChain, 1);
    servletFld.setAccessible(true);

    final Servlet servlet = Objects.requireNonNull(testingServlet);

    servletFld.set(filterChain, servlet);
    final Field filtersFld = filterChain.getClass().getDeclaredField("filters");
    filtersFld.setAccessible(true);
    final ApplicationFilterConfig[] filterConfigs =
        (ApplicationFilterConfig[]) filtersFld.get(filterChain);

    removeUselessFilters(filterConfigs);

    try {
      // final Class<? extends Filter> filterClz = BroadleafRequestFilter.class;
      // final Method shouldProcessURLMethod =
      //     filterClz.getDeclaredMethod("shouldProcessURL", HttpServletRequest.class, String.class);
      // shouldProcessURLMethod.setAccessible(true);
      //
      // final Field requestProcessorFld = filterClz.getDeclaredField("requestProcessor");
      // final Method skipDispatchMthd =
      //     OncePerRequestFilter.class.getDeclaredMethod("skipDispatch", HttpServletRequest.class);
      // skipDispatchMthd.setAccessible(true);
      // final WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
      // if (asyncManager != null) {
      //   asyncManager.clearConcurrentResult();
      //   System.out.println(
      //       "WebAsyncUtils.getAsyncManager(request).hasConcurrentResult():"
      //           + asyncManager.hasConcurrentResult());
      // }
      // requestProcessorFld.setAccessible(true);
      //
      // PooledLoOptimizer.REGENERATE = true;
      runWithFilter();
    } catch (Throwable e) {
      System.out.println("Stop at " + posFld.get(filterChain));
      throw new RuntimeException(e);
    }
    System.out.println("Done Processing");
  }

  private void runWithFilter() throws IOException, ServletException {
    filterChain.doFilter(request, response);
  }
}
