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
package cn.edu.sjtu.ipads.wbridge.expansion;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class WhiteList {
  // if the whitelist is disabled, nothing will be filtered out
  // if enabled, only registered ones will be preserved
  private static boolean isEnabled;
  private static final boolean ENABLE_DYNAMIC_GENERATED_CLASS_EXPANSION = true;

  public static void setIsEnabled(boolean isEnabled) {
    WhiteList.isEnabled = isEnabled;
  }

  public static void enable() {
    isEnabled = true;
  }

  public static boolean isPreservedSetsName(String name, boolean enableWhiteList) {
    return !enableWhiteList || PRESERVED_SETS_NAME.contains(name);
  }

  public static boolean isPreservedMapsName(String name, boolean enableWhiteList) {
    return !enableWhiteList || PRESERVED_MAPS_NAME.contains(name);
  }

  public static void disable() {
    isEnabled = false;
  }

  public static boolean isEnabled() {
    return isEnabled;
  }

  private static final Set<String> PRESERVED_MAPS_NAME =
      ImmutableSet.of(
          // --> ApplicationContext
          "appCtxAttr",
          // --> EhancedClasses
          "methodCache",
          "attributeCache",
          "transactionManagerCache",
          "shadowMatchCache",
          "allBeanNamesByType",
          "beanDefinitionMap",
          "resolvableDependencies",
          "singletonBeanNamesByType",
          "singletonObjects",
          "singletonFactories",
          "earlySingletonObjects",
          "tokenizedPatternCache",
          "stringMatcherCache",
          "requestMap",
          "retrieverCache",
          "retrievalMutex",
          "cache",
          // -------------> begin of em.find
          "entityPersisterMap",
          "initializedServiceByRole",
          "loaders",
          "properties",
          "proxiesByKey",
          "entitiesByKey",
          "collectionPersisterMap",
          "collectionsByKey",
          "collectionEntries",
          "xref",
          "postLoads",
          "enabledFilters",
          "batchLoadableCollections",
          "entityProxyInterfaceMap",
          "subclassesByDiscriminatorValue",
          // --------------> end of em.find
          // -------------> begin of em.contains
          "entityNameResolvers",
          "entityPersisterMap",
          // --------------> end of em.contains
          // -------------> begin of em.remove
          "entityPersisterMap",
          "initializedServiceByRole",
          "listenerClassToInstanceMap",
          "nonEnhancedEntityXref",
          "entityNameResolvers",
          "entitySnapshotsByKey",
          "xref",
          "collectionPersisterMap",
          "preRemoves",
          "postRemoves",
          // --------------> end of em.remove
          // --------------> begin of em.refresh
          "entitiesByKey",
          "entitiesByUniqueKey",
          "parentsByChild",
          "entitySnapshotsByKey",
          "subselectsByEntityKey",
          "batchLoadableEntityKeys",
          // --------------> end of em.refresh
          // --------------> begin of em.merge
          "serviceBindingMap",
          "namedStrategyImplementorByStrategyMap",
          "mapVal",
          "entityPersisterMap",
          "entityNameResolvers",
          "entitiesByKey",
          "mergeToManagedEntityXref",
          "mergeEntityToOperatedOnFlagMap",
          "managedToMergeEntityXref",
          "settings",
          "xrefLoadingCollectionEntries",
          "preCreates",
          // --------------> end of em.merge
          // --------------> begin of em.createNamedQuery
          "namedQueryDefinitionMap",
          "queryPlanCache",
          "imports",
          "typesByPropertyPath",
          "duplicateIncompatiblePaths",
          "columnsByPropertyPath",
          "columnReadersByPropertyPath",
          "columnReaderTemplatesByPropertyPath",
          "formulaTemplatesByPropertyPath",
          "entitiesByUniqueKey",
          // --------------> end of em.createNamedQuery
          // --------------> begin of em.criteria
          "jpaEntityTypesByEntityName",
          "jpaEntityTypeMap",
          "declaredAttributes",
          "entityTypes",
          "roleXref",
          "queryPlanCache",
          "imports",
          "enabledFiltersh",
          "typesByPropertyPath",
          "duplicateIncompatiblePaths",
          "columnsByPropertyPath",
          "columnReadersByPropertyPath",
          "columnReaderTemplatesByPropertyPath",
          "formulaTemplatesByPropertyPath",
          "functionMap",
          // --------------> end of em.criteria
          // --------------> begin of em.persist
          "preCreates",
          "postCreates",
          // --------------> end of em.persist
          // --------------> begin of em.flush
          "naturalIdResolutionCacheMap",
          "preUpdates",
          "associationsPerEntityPersister",
          "beanMetaDataManagers",
          "groupsPerOperation",
          "builtinConstraints",
          "multiValueConstraints",
          "_map",
          "serviceBindingMap",
          "postUpdates",
          "configurationValues",
          "entitiesByKey",
          "entitiesByUniqueKey",
          "parentsByChild",
          "entitySnapshotsByKey",
          "findEntityNameByEntityClass",
          "entityNameByInheritenceClassMap",
          // --------------> end of em.flush
          // --------------> begin of em.clear
          "entitiesByKey",
          "entitiesByUniqueKey",
          "proxiesByKey",
          "entitySnapshotsByKey",
          "arrayHolders",
          "collectionEntries",
          "collectionsByKey",
          "unownedCollections",
          "parentsByChild",
          "naturalIdResolutionCacheMap",
          // --------------> end of em.clear
          // --------------> begin of enhanced logic
          "attributes",
          "dbProperties",
          "aliasMap",
          "allBeanNamesByType",
          "mergedBeanDefinitions",
          "singletonBeanNamesByType",
          "proxyTypes",
          "singletonObjects",
          "singletonsCurrentlyInCreation",
          "stateMap",
          "source",
          "cacheStats",
          "transactionManagerCache",
          "entityExtensionManagers",
          "extractorMap",
          "idTypeIdMap",
          "transactionManagerCache",
          "entityMap",
          "registry",
          // --------------> end of enhanced logic
          // filter
          "ignoreAnnotationDefaults",
          "annotationIgnoresForClasses",
          "annotationIgnoredForMembers",
          "annotationIgnoresForReturnValues",
          "annotationIgnoresForCrossParameter",
          "annotationIgnoresForMethodParameter",
          "extractorMap");

  private static final Set<String> PRESERVED_SETS_NAME =
      ImmutableSet.of(
          // --> EhancedClasses
          "annotationParsers",
          // --> validators
          "typeContexts",
          "configuredTypes",
          "definedConstraints",
          "constraintContexts",
          "validatorDescriptors",
          // -------------> begin of em.find
          "enabledFetchProfileNames",
          "nullAssociations",
          // --------------> end of em.find
          // -------------> begin of em.remove
          "nullifiableEntityKeys",
          // --------------> end of em.remove
          // -------------> begin of em.refresh
          "nullifiableEntityKeys",
          // --------------> end of em.refresh
          // -------------> begin of em.flush
          "nullifiableEntityKeys",
          "constraintMappings",
          // --------------> end of em.flush
          // --------------> begin of em.getTransaction().commit()
          "unassociatedResultSets",
          "typeContexts",
          "constraintMappings",
          "configuredTypes",
          // --------------> end of em.getTransaction().commit(
          // --------------> begin of application related
          "propertySources",
          "encodedUrlBlacklist",
          "decodedUrlBlacklist",
          "applicationListeners",
          "applicationListenerBeans"
          // --------------> end of application related
          );

  static boolean shouldPreserveMapKV(String mapName, Object key, Object value) {
    if (mapName.equals("queryPlanCache")) {
      return false;
    } else if (mapName.equals("attributeCache")) {
      return false;
    } else if (mapName.equals("hib-metadata")) {
      // org.broadleafcommerce.common.util.dao.HibernateMappingProvider.metadataMap is too large, so
      // we preserve values on demand
      return key.equals("org.broadleafcommerce.core.catalog.domain.SkuImpl")
          || key.equals("org.broadleafcommerce.core.order.domain.FulfillmentOptionImpl")
          || key.equals("org.broadleafcommerce.profile.core.domain.CountryImpl")
          || key.equals("org.broadleafcommerce.core.catalog.domain.ProductAttributeImpl");
    }
    return true;
  }

  static boolean shouldPreserveSetKey(String setName, Object key, boolean enableWhiteList) {
    if (setName.equals("annotationParsers")) {
      return true;
    }
    if (!enableWhiteList) return true;
    return true;
  }

  static boolean shouldPreserveListVal(String setName, Object value) {
    // NOTE: these two classes should be filtered no matter whiteList is enabled or not.
    return !filterClass(
            "org.broadleafcommerce.profile.web.site.security.SessionFixationProtectionFilter",
            value) // we don't want to invalidate the session
        && !filterClass(
            "org.springframework.security.web.header.HeaderWriterFilter",
            value); // we don't need to write response header
  }

  static boolean filterClass(String className, Object instance) {
    try {
      return Class.forName(className).isInstance(instance);
    } catch (Exception ignored) {
    }
    return false;
  }

  static boolean shouldStopHandleInstanceType(String instanceClassName, int depth) {
    //    try {
    //      final Class<?> clz = Class.forName(instanceClassName);
    //      if (javax.servlet.ServletContext.class.isAssignableFrom(clz)) return true;
    //    } catch (ClassNotFoundException e) {
    //      return true;
    //    }

    if (instanceClassName.equals(
            "org.springframework.beans.factory.support.DefaultListableBeanFactory")
        && depth != 1) return true;

    if (instanceClassName.equals("java.io.File") || instanceClassName.startsWith("sun.net"))
      return false;

    return instanceClassName.contains("$$Lambda")
        || (ENABLE_DYNAMIC_GENERATED_CLASS_EXPANSION ? false : instanceClassName.contains("_$$_"))
        || instanceClassName.startsWith("java.io")
        || instanceClassName.equals("java.util.concurrent.ThreadPoolExecutor")
        || instanceClassName.startsWith("org.springframework.web.context.support")
        || instanceClassName.startsWith("sun.")
        || instanceClassName.startsWith("org.apache.catalina.core.ApplicationContext")
        || instanceClassName.startsWith("java.nio")
        || instanceClassName.startsWith("org.springframework.web.servlet.DispatcherServlet")
        || instanceClassName.startsWith("java.util.jar")
        || instanceClassName.startsWith("java.util.zip")
        || instanceClassName.startsWith("java.util.logging")
        || instanceClassName.startsWith("java.lang.Thread")
        || instanceClassName.startsWith("org.apache.tomcat.jdbc")
        || instanceClassName.contains("TestTypeExcludeFilter")
        || instanceClassName.equals(
            "org.broadleafcommerce.profile.web.site.security.SessionFixationProtectionFilter")
        || instanceClassName.equals("org.springframework.security.web.header.HeaderWriterFilter");
  }
}
