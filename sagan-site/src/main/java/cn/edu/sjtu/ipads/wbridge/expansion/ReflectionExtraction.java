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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static gov.nasa.jpf.symbc.Constants.*;

abstract class ReflectionExtraction {
  static ClassInfoContainer handleFieldType(Field instance, ClassInfoContainer classInfo) {
    final String fldName = instance.getName();
    final String clzName = instance.getDeclaringClass().getName();
    if (WhiteList.shouldStopHandleInstanceType(clzName, 1)) return null;

    classInfo.setAttribute(FIELD_DECLARING_CLZ_NAME_ATTR, clzName);
    classInfo.setAttribute(FIELD_NAME_ATTR, fldName);
    return classInfo;
  }

  static ClassInfoContainer handleConstructorType(
      Constructor<?> instance, ClassInfoContainer classInfo) {
    final String constructorClzName = instance.getName();
    if (WhiteList.shouldStopHandleInstanceType(constructorClzName, 1)) return null;

    classInfo.setAttribute(CONSTRUCTOR_CLASS_NAME_ATTR, constructorClzName);
    final List<String> paramTypes = new ArrayList<>();
    for (Class<?> parameterType : instance.getParameterTypes()) {
      paramTypes.add(parameterType.getName());
    }
    classInfo.setAttribute(CONSTRUCTOR_PARAMS_ATTR, paramTypes);
    return classInfo;
  }

  static ClassInfoContainer handleClassType(Class<?> instance, ClassInfoContainer classInfo) {
    final String clzName = instance.getName();
    classInfo.setAttribute(CLASS_NAME_ATTR, clzName);
    return classInfo;
  }

  static ClassInfoContainer handleMethodType(Method instance, ClassInfoContainer classInfo) {
    classInfo.setAttribute(METHOD_DECLARING_CLZ_NAME_ATTR, instance.getDeclaringClass().getName());
    classInfo.setAttribute(METHOD_SIMPLE_NAME_ATTR, instance.getName());
    classInfo.setAttribute(METHOD_STR_NAME_ATTR, instance.toGenericString());
    final List<String> paramTypes = new ArrayList<>();
    for (Class<?> parameterType : instance.getParameterTypes()) {
      paramTypes.add(parameterType.getName());
    }
    classInfo.setAttribute(METHOD_PARAMETERS_TYPE_ATTR, paramTypes);
    return classInfo;
  }
}
