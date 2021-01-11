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

import cn.edu.sjtu.ipads.ClassInfoContainer;
import edu.sjtu.ipads.wbridge.hack.classes.EntityNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LazyInitializationException;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.resource.jdbc.spi.EmptyStatementInspector;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.aop.framework.Advised;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.util.ReflectionUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static gov.nasa.jpf.symbc.Constants.*;

public abstract class ObjectExtraction {
  public static int MAX_DEPTH = 20;
  private static final Log logger = LogFactory.getLog(ObjectExtraction.class);

  private static boolean ENABLE_EXPAND_CGLIB_CLZ = false;
  public static Map<String, EntityNode> entities = new HashMap<>();

  public static int getMaxDepth() {
    return MAX_DEPTH;
  }

  public static void setEnableExpandCglibClz(boolean val) {
    ENABLE_EXPAND_CGLIB_CLZ = val;
  }

  public static boolean isEnableExpandCglibClz() {
    return ENABLE_EXPAND_CGLIB_CLZ;
  }

  public static void setMaxDepth(int maxDepth) {
    MAX_DEPTH = maxDepth;
  }

  public static class ExtractMeta {
    ClassInfoContainer container = null;
    int depth = 0;
    int endDepth = 0;
    int maxDepth = 0;
    boolean whiteList = false;
    boolean hasEndDepth = false;
  }

  private static final Map<String, ExtractMeta> resolvedClasses = new HashMap<>();

  public static ClassInfoContainer expandInstanceValues(
      Object instance, String name, Map<String, ExtractMeta> resolvedClassInfo) {
    return expandInstanceValues(instance, name, resolvedClassInfo, 1, WhiteList.isEnabled());
  }

  public static ClassInfoContainer expandInstanceValues(Object instance, String name) {
    return expandInstanceValues(instance, name, resolvedClasses, 1, WhiteList.isEnabled());
  }

  public static ClassInfoContainer expandInstanceValues(
      Object instance,
      String name,
      Map<String, ExtractMeta> resolvedClassInfo,
      int depth,
      boolean enableWhiteList) {
    if (instance == null) return null;
    if (instance instanceof Advised && ENABLE_EXPAND_CGLIB_CLZ) {
      try {
        final Object advisedInstance;
        advisedInstance = ((Advised) instance).getTargetSource().getTarget();
        // instance is declared final, so we do not modify this
        return expandInstanceValues(
            advisedInstance, name, resolvedClassInfo, depth, enableWhiteList);
      } catch (Exception e) {
        System.out.println("[ Warning ] Error expanding proxy clz:" + e.getMessage());
      }
    }
    final String instanceClassName;

    // handle special case for StatementInspector, inject a default(empty) implementation
    if (name.equals("statementInspector") && instance instanceof StatementInspector) {
      instanceClassName = EmptyStatementInspector.class.getCanonicalName();
    } else instanceClassName = instance.getClass().getName();

    // we use className + instance identity hash code to distinguish java objects. Do not use
    // obj.hashCode as key:
    // 1. use obj.hashCode() for identity key is not appropriate. For the same class(e.g. HashMap),
    // two different instances might have the same hashCode, the following logic will reuse the
    // older object and regard them as the same.
    // 2. Even for two different instances of different classes might have the same hashCode(e.g.
    // empty HashMap & ConcurrentReferenceHashMap)
    // 3. identityHashCode might be the same across different classes of instances, need to add
    // class name to distinguish with others
    final String instanceId =
        instanceClassName + "_MAGIC_SPLIT_" + System.identityHashCode(instance);

    final boolean continueLiftedExpanding;
    final boolean nextEnableWhiteList;
    if (resolvedClassInfo.containsKey(instanceId)) {
      final ExtractMeta meta = resolvedClassInfo.get(instanceId);
      final ClassInfoContainer container = meta.container;
      // Otherwise, this mismatch is caused by hack: change UnmodifiableMap to HashMap
      assert instanceClassName.equals(container.getClassName())
          || "java.util.Collections$SetFromMap".equals(instanceClassName)
          || "java.util.Collections$UnmodifiableMap".equals(instanceClassName);

      nextEnableWhiteList = (meta.whiteList == false) ? false : enableWhiteList;

      // if the resolved container is expanded from a shallower depth than current depth,(startPoint
      // < depth)
      // the container will include more expanded information than starting the expansion
      // procedure from current depth. Thus, return the container which has deeper object info.
      if (meta.hasEndDepth == false) {
        if (meta.maxDepth - meta.depth > getMaxDepth() - depth) return container;
        // if (startPoint < depth) return container;
        // otherwise continue expansion will resolve more information to the instance
      } else {
        if (meta.endDepth - meta.depth > getMaxDepth() - depth) return container;
      }
      continueLiftedExpanding = true;
    } else {
      continueLiftedExpanding = false;
      nextEnableWhiteList = enableWhiteList;
    }

    final Class<?> instanceClass = instance.getClass();
    ExtractMeta meta = null;
    ClassInfoContainer classInfo = null;
    if (continueLiftedExpanding) {
      meta = resolvedClassInfo.get(instanceId);
      classInfo = meta.container;
    }

    if (WhiteList.shouldStopHandleInstanceType(instanceClassName, depth)) return classInfo;
    // filter custom instance
    if (ExpansionConfig.getCustomFilter() != null
        && ExpansionConfig.getCustomFilter().test(instance)) {
      // logger.warn("Filtered instance of type " + instanceClassName + " by custom filter");
      return classInfo;
    }

    if (classInfo == null) {
      if (instanceClassName.equals("java.util.Collections$UnmodifiableMap")) {
        // Hack for UnmodifiableMap, because we cannot initialize and put to UnmodifiableMap,
        // just change it into an HashMap
        classInfo = new ClassInfoContainer("java.util.HashMap");
      } else if (instanceClassName.equals("java.util.Collections$SetFromMap")) {
        classInfo = new ClassInfoContainer("java.util.HashSet");
      } else {
        classInfo = new ClassInfoContainer(instanceClassName);
      }
      meta = new ExtractMeta();
      meta.container = classInfo;
    }

    resolvedClassInfo.put(instanceId, meta);
    classInfo.setAttribute(INSTANCE_IDENTIFIER, instanceId);
    meta.depth = depth;
    meta.maxDepth = getMaxDepth();
    meta.whiteList = nextEnableWhiteList;
    if (instance instanceof JoinedSubclassEntityPersister) {
      computeEntityHierarchy(instance);
    }
    if (depth > getMaxDepth()) {
      meta.hasEndDepth = true;
      meta.endDepth = getMaxDepth();
      return classInfo;
    }

    if (instanceClass.isPrimitive()
        || instance instanceof Number
        || instance instanceof Boolean
        || instance instanceof Character
        || ((instance instanceof CharSequence) && ! instance.getClass().toString().contains("OriginTrackedCharSequence"))) {
      classInfo.setAttribute(CONCRETE_VALUE_ATTR, instance);
      return classInfo;
    } else if (instanceClass.isArray()) {
      return ArrayExtraction.handleArrayType(
          instance, name, resolvedClassInfo, depth, nextEnableWhiteList, classInfo);
    } else if (instance instanceof Map) {
      return CollectionExtraction.handleMapType(
          name, resolvedClassInfo, depth, nextEnableWhiteList, classInfo, (Map<?, ?>) instance);
    } else if (instance instanceof List) {
      return CollectionExtraction.handleListType(
          name, resolvedClassInfo, depth, nextEnableWhiteList, classInfo, (List<?>) instance);
    } else if (instance instanceof Set) {
      return CollectionExtraction.handleSetType(
          name, resolvedClassInfo, depth, nextEnableWhiteList, classInfo, (Set<?>) instance);
    } else if (instance instanceof Class) {
      return ReflectionExtraction.handleClassType((Class<?>) instance, classInfo);
    } else if (instance instanceof Enum) {
      return handleEnumType((Enum) instance, classInfo);
    } else if (instance instanceof Constructor) {
      return ReflectionExtraction.handleConstructorType((Constructor<?>) instance, classInfo);
    } else if (instance instanceof Field) {
      return ReflectionExtraction.handleFieldType((Field) instance, classInfo);
    } else if (instance instanceof Method) {
      return ReflectionExtraction.handleMethodType((Method) instance, classInfo);
    }

    final ClassInfoContainer finalClassInfo = classInfo;
    ReflectionUtils.doWithFields(
        instanceClass,
        field -> {
          if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) return;
          field.setAccessible(true);
          final Object fldVal = ReflectionUtils.getField(field, instance);

          if (WhiteList.filterClass("org.apache.commons.logging.Log", fldVal)
              || WhiteList.filterClass("org.slf4j.Logger", fldVal)
              || WhiteList.filterClass("org.jboss.logging.BasicLogger", fldVal)) {
            return;
          }

          final ClassInfoContainer container;
          try {
            container =
                expandInstanceValues(
                    fldVal, field.getName(), resolvedClassInfo, depth + 1, nextEnableWhiteList);
          } catch (LazyInitializationException e) {
            // lazy fields of entity can't be initialized at FrontRecordingFilter because hibernate
            // session doesn't open yet.
            System.err.println("[ Warning ] Lazy field " + name + "." + field.getName() + " is skipped.");
            return;
          }
          finalClassInfo.addFieldInfo(field.getName(), container);
        });

    return classInfo;
  }

  static ClassInfoContainer handleEnumType(Enum instance, ClassInfoContainer classInfo) {
    classInfo.setAttribute(ENUM_VALUE_ATTR, instance.name());
    return classInfo;
  }

  static void computeEntityHierarchy(Object instance) {
    final JoinedSubclassEntityPersister persister = (JoinedSubclassEntityPersister) instance;
    final String tableName = persister.getTableName();
    final int span = persister.getSubclassTableSpan();
    final Method isClassOrSuperclassTable =
        ReflectionUtils.findMethod(persister.getClass(), "isClassOrSuperclassTable", null);
    assert isClassOrSuperclassTable != null;
    final EntityNode curEntityNode = entities.computeIfAbsent(tableName, EntityNode::new);

    for (int i = 0; i < span; i++) {
      final String subclassTableName = persister.getSubclassTableName(i);
      if (subclassTableName.equals(tableName)) continue;
      try {
        isClassOrSuperclassTable.setAccessible(true);
        final boolean superClass = (boolean) isClassOrSuperclassTable.invoke(instance, i);
        final EntityNode subClzNode = entities.computeIfAbsent(subclassTableName, EntityNode::new);
        if (superClass) {
          curEntityNode.addParent(subClzNode);
          subClzNode.addChild(curEntityNode);
        } else {
          curEntityNode.addChild(subClzNode);
          subClzNode.addParent(curEntityNode);
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void saveToFile(Object o, String path) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
      oos.writeObject(o);
      oos.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
