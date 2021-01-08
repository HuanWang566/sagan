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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gov.nasa.jpf.symbc.Constants.ARRAY_CONTENTS_ATTR;
import static gov.nasa.jpf.symbc.Constants.ARRAY_CONTENT_ATTR;

abstract class ArrayExtraction {
  static ClassInfoContainer handleArrayType(
      Object instance,
      String name,
      Map<String, ObjectExtraction.ExtractMeta> resolvedClassInfo,
      int depth,
      boolean enableWhiteList,
      ClassInfoContainer classInfo) {
    assert instance.getClass().isArray();
    final List<ClassInfoContainer> lstContainer = new ArrayList<>();
    int length = Array.getLength(instance);
    for (int i = 0; i < length; i++) {
      lstContainer.add(
          ObjectExtraction.expandInstanceValues(
              Array.get(instance, i), ARRAY_CONTENT_ATTR, resolvedClassInfo, depth + 1, enableWhiteList));
    }
    classInfo.setAttribute(ARRAY_CONTENTS_ATTR, lstContainer);
    return classInfo;
  }
}
