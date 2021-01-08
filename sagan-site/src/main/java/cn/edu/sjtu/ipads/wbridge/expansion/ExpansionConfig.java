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

import java.util.function.Predicate;

public abstract class ExpansionConfig {

  /**
   * @param maxDepth max expansion depth
   * @param enableCglibExpansion whether to expand the original instance or enhanced instance. If
   *     true, expand the original instance, if false, expand the enhanced instance
   * @param enableWhiteList if the whitelist is disabled, nothing will be filtered out; if enabled,
   *     only registered ones will be preserved
   */
  public static void set(int maxDepth, boolean enableCglibExpansion, boolean enableWhiteList) {
    ObjectExtraction.setMaxDepth(maxDepth);
    ObjectExtraction.setEnableExpandCglibClz(enableCglibExpansion);
    WhiteList.setIsEnabled(enableWhiteList);
  }

  private static Predicate<Object> CUSTOM_FILTER = null;

  public static void setCustomFilter(Predicate<Object> customFilter) {
    CUSTOM_FILTER = customFilter;
  }

  public static Predicate<Object> getCustomFilter() {
    return CUSTOM_FILTER;
  }
}
