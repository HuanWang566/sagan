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
package sagan.site;

import cn.edu.sjtu.ipads.ClassInfoContainer;
import cn.edu.sjtu.ipads.wbridge.LoadWrapper;
import cn.edu.sjtu.ipads.wbridge.expansion.ExpansionConfig;
import cn.edu.sjtu.ipads.wbridge.expansion.ObjectExtraction;
import cn.edu.sjtu.ipads.wbridge.utils.WBridgeSaganParamUtils;
import sagan.site.blog.support.BlogAdminController;
import sagan.site.blog.support.BlogController;
import sagan.site.filter.FrontRecordingFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.envers.tools.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

import static cn.edu.sjtu.ipads.wbridge.expansion.ObjectExtraction.*;
import static gov.nasa.jpf.symbc.Constants.ENTITY_CLZ_HIERARCHY;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MetadataMarshalTest {
  private static final Log logger = LogFactory.getLog(MetadataMarshalTest.class);
  private static final boolean useOneFile = true;

  private static final String DIR_PREFIX = "/tmp/";

  @Autowired private EntityManager em;

  @Autowired public BlogController blogController;

  @Autowired public BlogAdminController blogAdminController;

  @Autowired private AnnotationConfigServletWebServerApplicationContext context;

  @Autowired private TestRestTemplate testRestTemplate;

  @LocalServerPort int randomServerPort;

  @Value("${enable.collect.input}")
  public boolean enableCollectInput;

  @Value("${enable.collect.filter}")
  public boolean enableCollectFilter;

  @Test
  @Transactional
  public void marshal() throws Exception {
    List<Pair<Object, String>> containerList = new ArrayList<>();

    containerList.add(recordEntityManagerAndController());
    System.out.println("Finish recordEntityManagerAndController");

    containerList.add(recordBeanFactory());
    System.out.println("Finish recordBeanFactory");

    containerList.addAll(recordFilter());
    System.out.println("Finish recordFilter");

    containerList.add(recordEnvironment());
    System.out.println("Finish recordEnvironment");

    // containerList.add(recordHibernateMappingProvider());
    // System.out.println("Finish recordHibernateMappingProvider");

    if (useOneFile) {
      saveAllContainersToOneFile(containerList);
    } else {
      saveAllContainers(containerList);
    }
  }

  // private Pair<Object, String> recordHibernateMappingProvider() throws Exception {
  //   ExpansionConfig.set(10, false, false);
  //   ExpansionConfig.setCustomFilter(
  //       o ->
  //           o instanceof BeanFactory
  //               || o instanceof MetadataBuildingContext
  //               || o.getClass()
  //                   .getName()
  //                   .startsWith(MetadataBuildingContextRootImpl.class.getName()));
  //   final Field metadataFld =
  //       ReflectionUtils.findField(HibernateMappingProvider.class, "metadataMap");
  //   metadataFld.setAccessible(true);
  //   final Map<String, PersistentClass> metadataMap =
  //       (Map<String, PersistentClass>) metadataFld.get(null);
  //   System.out.println(metadataMap.size());
  //   final ClassInfoContainer metadataContainer = expandInstanceValues(metadataMap, "hib-metadata");
  //   final String filename = DIR_PREFIX + String.format("hib-metadata-%d.data", getMaxDepth());
  //   ExpansionConfig.setCustomFilter(null);
  //   return Pair.make(metadataContainer, filename);
  // }

  private Pair<Object, String> recordEnvironment() throws Exception {
    ExpansionConfig.set(20, false, false);
    ExpansionConfig.setCustomFilter(o -> o instanceof BeanFactory);
    final ConfigurableEnvironment environment = context.getEnvironment();
    final ClassInfoContainer envContainer = expandInstanceValues(environment, "env");
    final String filename = DIR_PREFIX + String.format("env-%d.data", getMaxDepth());
    ExpansionConfig.setCustomFilter(null);
    return Pair.make(envContainer, filename);
  }

  private List<Pair<Object, String>> recordFilter() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    //    map.add("productId", "[3]"); // Hoppin' Hot Sauce

    final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
    WBridgeSaganParamUtils.SP_URI_LIST.clear();
    WBridgeSaganParamUtils.SP_URI_LIST.add(
        org.apache.commons.lang3.tuple.Triple.of("/blog", "GET", "GetBlog"));
    assert !enableCollectInput;
    assert enableCollectFilter;
    testRestTemplate.getForEntity(
        "https://localhost:" + randomServerPort + "/blog", String.class);
    assert FrontRecordingFilter.FILTER_COLLECTED : "Fail to record filter info";
    return FrontRecordingFilter.containerList;
  }

  private Pair<Object, String>

  recordEntityManagerAndController() throws IOException {
    ExpansionConfig.set(20, false, false);
    ExpansionConfig.setCustomFilter(
        o -> o instanceof BeanFactory || o instanceof Environment || o instanceof MessageSource);

    final LoadWrapper wrapper =
        new LoadWrapper(
            em.unwrap(Session.class),
                blogController,
                blogAdminController);

    final ClassInfoContainer emcic = expandInstanceValues(wrapper, "em");
    emcic.setAttribute(ENTITY_CLZ_HIERARCHY, ObjectExtraction.entities);
    final String filename = DIR_PREFIX + "hib.data";
    ExpansionConfig.setCustomFilter(null);
    return Pair.make(emcic, filename);
  }

  private Pair<Object, String> recordBeanFactory() throws IOException {
    ExpansionConfig.set(15, false, false);

    final GenericWebApplicationContext ctx = this.context;
    final DefaultListableBeanFactory beanFactory =
        (DefaultListableBeanFactory) ctx.getBeanFactory();
    final Map<String, ClassInfoContainer> containerMap = new HashMap<>();
    final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
      beanDefinitionMap.put(beanDefinitionName, beanFactory.getBeanDefinition(beanDefinitionName));
    }

    // Objects.requireNonNull(beanDefinitionMap.get("blRequestCustomerResolver"));
    logger.warn(String.format("Expanding beanFactory of size %d", beanDefinitionMap.size()));
    final ClassInfoContainer beanDefinitionContainer =
        expandInstanceValues(beanDefinitionMap, "beanFactory");
    containerMap.put("beanDefinitionMap", beanDefinitionContainer);

    final Map<String, Scope> scopeMap = new HashMap<>();
    for (String scopeName : beanFactory.getRegisteredScopeNames()) {
      scopeMap.put(scopeName, beanFactory.getRegisteredScope(scopeName));
    }
    logger.warn(
        String.format(
            "Expanding scopeMap of size %d", beanFactory.getRegisteredScopeNames().length));
    final ClassInfoContainer scopeContainer = expandInstanceValues(scopeMap, "beanFactory");
    containerMap.put("scopeMap", scopeContainer);

    final Map<String, Object> singletonObjectMap = new HashMap<>();
    for (String singletonObjectName : beanFactory.getSingletonNames()) {
      singletonObjectMap.put(singletonObjectName, beanFactory.getSingleton(singletonObjectName));
    }
    ExpansionConfig.set(15, false, true);
    logger.warn(
        String.format("Expanding singletonObjectMap of size %d", singletonObjectMap.size()));
    final ClassInfoContainer singletonObjectContainer =
        expandInstanceValues(singletonObjectMap, "singletonObjects");
    containerMap.put("singletonObjectMap", singletonObjectContainer);

    final String filename = DIR_PREFIX + String.format("bean-factory-%d.data", getMaxDepth());
    return Pair.make(containerMap, filename);
  }

  private static void saveAllContainers(List<Pair<Object, String>> containerList) {
    containerList.forEach(pair -> saveToFile(pair.getFirst(), pair.getSecond()));
  }

  private static void saveAllContainersToOneFile(List<Pair<Object, String>> containerList) {
    final Map<String, Object> allInOne = new HashMap<>();
    containerList.forEach(pair -> allInOne.put(pair.getSecond(), pair.getFirst()));
    saveToFile(allInOne, DIR_PREFIX + "hib.data");
  }
}
