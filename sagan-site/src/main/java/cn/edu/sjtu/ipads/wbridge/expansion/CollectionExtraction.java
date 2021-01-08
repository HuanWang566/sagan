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
import com.zaxxer.hikari.util.FastList;

import java.util.*;

import static gov.nasa.jpf.symbc.Constants.*;

abstract class CollectionExtraction {
  static ClassInfoContainer handleMapType(
      String name,
      Map<String, ObjectExtraction.ExtractMeta> resolvedClassInfo,
      int depth,
      boolean enableWhitelist,
      ClassInfoContainer classInfo,
      Map<?, ?> insMap) {
    final Map<ClassInfoContainer, ClassInfoContainer> valsMap = new HashMap<>();
    if (!WhiteList.isPreservedMapsName(name, enableWhitelist)) return null;
    insMap.forEach(
        (k, v) -> {
          if (!WhiteList.shouldPreserveMapKV(name, k, v)) return;
          valsMap.put(
              ObjectExtraction.expandInstanceValues(
                  k, MAP_KEY_ATTR, resolvedClassInfo, depth + 1, enableWhitelist),
              ObjectExtraction.expandInstanceValues(
                  v, MAP_VAL_ATTR, resolvedClassInfo, depth + 1, enableWhitelist));
        });
    classInfo.setAttribute(MAP_KVS_ATTR, valsMap);
    if (insMap instanceof TreeMap
        && ((TreeMap<?, ?>) insMap).comparator() == String.CASE_INSENSITIVE_ORDER) {
      classInfo.setAttribute(MAP_INSENSITIVE_ATTR, true);
    }
    return classInfo;
  }

  static ClassInfoContainer handleSetType(
      String name,
      Map<String, ObjectExtraction.ExtractMeta> resolvedClassInfo,
      int depth,
      boolean enableWhiteList,
      ClassInfoContainer classInfo,
      Set<?> insSet) {
    final List<ClassInfoContainer> valsSet = new ArrayList<>();
    if (!WhiteList.isPreservedSetsName(name, enableWhiteList)) return null;
    insSet.forEach(
        k -> {
          if (!WhiteList.shouldPreserveSetKey(name, k, enableWhiteList)) return;
          valsSet.add(
              ObjectExtraction.expandInstanceValues(
                  k, SET_VAL_ATTR, resolvedClassInfo, depth + 1, enableWhiteList));
        });
    classInfo.setAttribute(SET_VALS_ATTR, valsSet);
    return classInfo;
  }

  static ClassInfoContainer handleListType(
      String name,
      Map<String, ObjectExtraction.ExtractMeta> resolvedClassInfo,
      int depth,
      boolean enableWhitelist,
      ClassInfoContainer classInfo,
      List<?> listeners) {

    if (listeners instanceof FastList) {
      List<Object> tmpArr = new ArrayList<>();
      for (int i = 0; i < listeners.size(); i++) {
        tmpArr.add(listeners.get(i));
      }
      listeners = tmpArr;
    }
    final List<ClassInfoContainer> lstContainer = new ArrayList<>();
    // copy to avoid concurrent modification error
    if (listeners instanceof Vector) listeners = new ArrayList<>(listeners);
    listeners.forEach(
        o -> {
          if (!WhiteList.shouldPreserveListVal(name, o)) return;
          lstContainer.add(
              ObjectExtraction.expandInstanceValues(
                  o, LIST_CONTENT_ATTR, resolvedClassInfo, depth + 1, enableWhitelist));
        });
    classInfo.setAttribute(LIST_CONTENTS_ATTR, lstContainer);
    return classInfo;
  }
}
