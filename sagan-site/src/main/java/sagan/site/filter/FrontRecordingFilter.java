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
package sagan.site.filter;

import cn.edu.sjtu.ipads.ClassInfoContainer;
import cn.edu.sjtu.ipads.wbridge.expansion.ExpansionConfig;
import cn.edu.sjtu.ipads.wbridge.expansion.ObjectExtraction;
import cn.edu.sjtu.ipads.wbridge.server.OfflineCompiler;
import cn.edu.sjtu.ipads.wbridge.utils.BufferedServletRequestWrapper;
import cn.edu.sjtu.ipads.wbridge.utils.WBridgeSaganParamUtils;
import com.mchange.v2.ser.SerializableUtils;
import edu.sjtu.ipads.wbridge.storedprocedure.invocation.SPInvokeManager;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.ApplicationFilterChain;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.tools.Pair;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.edu.sjtu.ipads.wbridge.utils.WBridgeSaganParamUtils.shouldCollectInputThisTime;

@Component("saganFrontRecordingFilter")
public class FrontRecordingFilter extends OncePerRequestFilter implements Ordered {
  public static boolean FILTER_COLLECTED = false;
  public static List<Pair<Object, String>> containerList = new ArrayList<>();

  private static final ThreadLocal<Integer> COUNTER = ThreadLocal.withInitial(() -> 1);
  private static ConcurrentHashMap<String, TraceInput> TRACE_FREQUENCE_MAP =
      new ConcurrentHashMap<>();

  private static class TraceInput {
    private static final String BASEDIR = "/home/huanwang/Documents/project/jpf-web/";
    private static final String NEW_TRACE_DIR = "api-input";
    AtomicInteger frequency;
    final int counter; // a threadLocal id
    final long threadId; // threadId
    final String apiName;
    byte[] request;
    byte[] response;
    byte[] resultSet;

    public TraceInput(int counter, long threadId, String apiName) {
      this.counter = counter;
      this.threadId = threadId;
      this.apiName = apiName;
      this.frequency = new AtomicInteger(0);
    }

    public String getRequestFileName() {
      return String.format("%s/%s/%d-%d-request.data", NEW_TRACE_DIR, apiName, counter, threadId);
    }

    public String getResponseFileName() {
      return String.format("%s/%s/%d-%d-response.data", NEW_TRACE_DIR, apiName, counter, threadId);
    }

    public String getResultsetFileName() {
      return String.format("%s/%s/%d-%d-resultset.data", NEW_TRACE_DIR, apiName, counter, threadId);
    }

    public void saveInputToFile() {
      final String reqFname = getRequestFileName();
      writeFileToBaseDir(request, reqFname);
      final String responseFname = getResponseFileName();
      writeFileToBaseDir(response, responseFname);
      final String resultFname = getResultsetFileName();
      writeFileToBaseDir(resultSet, resultFname);
    }

    private void writeFileToBaseDir(byte[] bytes, String filename) {
      File file = new File(BASEDIR + filename);
      file.getParentFile().mkdirs();
      try (FileOutputStream fos = new FileOutputStream(file)) {
        fos.write(bytes);
        fos.flush();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  private final OfflineCompiler offlineCompiler;
  private final String webHost;
  private final String webUser;
  private final String webPass;
  private final String dbUser;
  private final String dbPass;
  private final String dbHost;

  private final int traceMapSizeThreshold;
  private final int hotTracePercent;
  private final int sampleN;
  public final boolean enableCollectFilter;
  public final boolean enableCollectInput;

  public FrontRecordingFilter(
      @Value("${compiler.host}") String compilerHost,
      @Value("${webserver.host}") String webHost,
      @Value("${webserver.user}") String webUser,
      @Value("${webserver.password}") String webPass,
      @Value("${database.user}") String dbUser,
      @Value("${database.password}") String dbPass,
      @Value("${database.url}") String dbUrl,
      @Value("${recorder.threshold}") int traceMapSizeThreshold,
      @Value("${recorder.hotpercent}") int hotPercent,
      @Value("${recorder.sample}") int sample,
      @Value("${enable.collect.input}") boolean enableCollectInput,
      @Value("${enable.collect.filter}") boolean enableCollectFilter) {
    this.webHost = webHost;
    this.webUser = webUser;
    this.webPass = webPass;
    this.dbUser = dbUser;
    this.dbPass = dbPass;
    String dbH = dbUrl.substring(dbUrl.indexOf("//") + 2, dbUrl.lastIndexOf(":"));
    this.dbHost = dbH.equals("localhost") ? webHost : dbH;
    this.sampleN = sample;
    this.traceMapSizeThreshold = traceMapSizeThreshold;
    this.hotTracePercent = hotPercent;
    this.enableCollectFilter = enableCollectFilter;
    this.enableCollectInput = enableCollectInput;

    if (!StringUtils.isEmpty(compilerHost)) {
      try {
        offlineCompiler =
            (OfflineCompiler) Naming.lookup("//" + compilerHost + ":1099/offlineCompiler");
        System.out.println("Connect to offline compiler success!");
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    } else {
      offlineCompiler = null;
    }
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!WBridgeSaganParamUtils.isSpURI(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (enableCollectFilter) recordFilters(request, filterChain);

    boolean sampled = false;
    int counter = COUNTER.get();
    if (counter % sampleN == 1) sampled = true; // sample traces to control the congestion
    COUNTER.set(counter + 1);
    final boolean enableCollectInput =
        this.enableCollectInput && shouldCollectInputThisTime(request);

    if (!sampled || !enableCollectInput) {
      filterChain.doFilter(request, response);
      return;
    }

    String apiName = WBridgeSaganParamUtils.getSPDefApi(request);
    TraceInput inputWrapper = new TraceInput(counter, Thread.currentThread().getId(), apiName);

    request = recordRequestInput(request, response, inputWrapper);
    SPInvokeManager.startRecording();

    filterChain.doFilter(request, response);

    List<org.apache.commons.lang3.tuple.Pair<String, Object>> recordedResulset =
        SPInvokeManager.getRecordedSqlAndResultSets();

    if (recordedResulset == null || recordedResulset.isEmpty()) { // this path has been optimized
      SPInvokeManager.endRecording();
      return;
    }

    inputWrapper.resultSet = SerializableUtils.toByteArray(recordedResulset);
    StringBuilder keyBuilder = new StringBuilder(); // use sql template as key
    recordedResulset.forEach(p -> keyBuilder.append(p.getLeft()).append("\n"));
    SPInvokeManager.endRecording();

    updateFrequencyMap(keyBuilder.toString(), inputWrapper);
    inputWrapper.saveInputToFile();
  }

  private void updateFrequencyMap(String key, TraceInput inputWrapper) {
    TRACE_FREQUENCE_MAP.putIfAbsent(key, inputWrapper);
    inputWrapper.frequency.incrementAndGet(); // update frequency

    if (TRACE_FREQUENCE_MAP.size() < traceMapSizeThreshold) return;

    ConcurrentHashMap<String, TraceInput> frequenceMap = TRACE_FREQUENCE_MAP;
    boolean needSaveFile = false;
    synchronized (this) {
      if (TRACE_FREQUENCE_MAP.size() > traceMapSizeThreshold) {
        TRACE_FREQUENCE_MAP = new ConcurrentHashMap<>();
        needSaveFile = true;
      }
    }

    if (!needSaveFile) return; // some other thread is doing this work

    // save hot path to files
    final int outputSize = frequenceMap.size() * hotTracePercent / 100;
    PriorityQueue<TraceInput> minHeap =
        new PriorityQueue<>(Comparator.comparingInt(o -> o.frequency.get()));
    frequenceMap
        .values()
        .forEach(
            input -> {
              if (minHeap.size() < outputSize) {
                minHeap.add(input);
              } else {
                assert minHeap.peek() != null;
                if (minHeap.peek().frequency.get() < input.frequency.get()) {
                  minHeap.poll();
                  minHeap.add(input);
                }
              }
            });

    Map<String, List<OfflineCompiler.Trace>> tracesToCompile = new HashMap<>();
    minHeap.forEach(
        iw -> {
          tracesToCompile.putIfAbsent(iw.apiName, new ArrayList<>());
          tracesToCompile
              .get(iw.apiName)
              .add(
                  new OfflineCompiler.Trace(
                      key, iw.apiName, iw.request, iw.response, iw.resultSet));
        });

    // call webridge to perform offline compile
    if (offlineCompiler != null) {
      try {
        String spDefDir = System.getProperty("spDefDir");
        assert !StringUtils.isEmpty(spDefDir);
        OfflineCompiler.ServerMeta serverMeta =
            new OfflineCompiler.ServerMeta(
                webHost, webUser, webPass, spDefDir, dbHost, dbUser, dbPass);

        offlineCompiler.startCompile(serverMeta, tracesToCompile);
      } catch (RemoteException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

  // we return a BufferedServletRequestWrapper to allow the request body being read multiple times.
  private HttpServletRequest recordRequestInput(
      HttpServletRequest request, HttpServletResponse response, TraceInput inputWrapper) {
    if (!WBridgeSaganParamUtils.isSpURI(request)) return request;
    // some fields are lazy initialized
    request.getRemoteAddr();
    request.getRemoteHost();
    request.getRemotePort();
    try {
      ExpansionConfig.set(9, false, false);
      ExpansionConfig.setCustomFilter(o -> o instanceof BeanFactory);

      BufferedServletRequestWrapper requestWrapper = WBridgeSaganParamUtils.bufferRequestBody(request);
      ClassInfoContainer reqInfo =
          ObjectExtraction.expandInstanceValues(requestWrapper, "request", new HashMap<>());
      inputWrapper.request = SerializableUtils.toByteArray(reqInfo);

      ClassInfoContainer resInfo =
          ObjectExtraction.expandInstanceValues(
              WBridgeSaganParamUtils.unwrap(response), "response", new HashMap<>());
      inputWrapper.response = SerializableUtils.toByteArray(resInfo);
      return requestWrapper;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      ExpansionConfig.setCustomFilter(null);
    }
  }

  // NOTE: this function should only be used while testing
  private void recordFilters(HttpServletRequest request, FilterChain filterChain) {
    if (!WBridgeSaganParamUtils.isSpURI(request) || FILTER_COLLECTED) return;
    try {
      ExpansionConfig.setCustomFilter(o -> o instanceof BeanFactory);
      ExpansionConfig.set(15, false, false);
      final Class<? extends ApplicationFilterChain> filterChainClass =
          (Class<? extends ApplicationFilterChain>) filterChain.getClass();
      final Field servletFld = filterChainClass.getDeclaredField("servlet");
      servletFld.setAccessible(true);
      final Object oldServlet = servletFld.get(filterChain);
      servletFld.set(filterChain, null); // ignore the servlet
      final ClassInfoContainer filterChainRes =
          ObjectExtraction.expandInstanceValues(filterChain, "filterChain");
      containerList.add(
          Pair.make(
              filterChainRes,
              String.format("/tmp/filterChain-%s.data", ObjectExtraction.MAX_DEPTH)));

      servletFld.set(filterChain, oldServlet);

      ExpansionConfig.set(15, false, true);
      ExpansionConfig.setCustomFilter(null);
      final Field filtersFld = filterChainClass.getDeclaredField("filters");
      filtersFld.setAccessible(true);
      final ApplicationFilterConfig[] filters =
          (ApplicationFilterConfig[]) filtersFld.get(filterChain);
      final ApplicationContextFacade facade =
          (ApplicationContextFacade) filters[0].getServletContext();
      final Field appCtxFld = facade.getClass().getDeclaredField("context");
      appCtxFld.setAccessible(true);
      final ApplicationContext context = (ApplicationContext) appCtxFld.get(facade);
      final Field attrFld = context.getClass().getDeclaredField("attributes");
      attrFld.setAccessible(true);
      final Map<?, ?> attr = (ConcurrentHashMap<?, ?>) attrFld.get(context);
      final ClassInfoContainer attrRes = ObjectExtraction.expandInstanceValues(attr, "appCtxAttr");
      containerList.add(
          Pair.make(attrRes, String.format("/tmp/appCtxAttr-%s.data", ObjectExtraction.MAX_DEPTH)));
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      FILTER_COLLECTED = true;
      ExpansionConfig.setCustomFilter(null);
    }
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE + 1;
  }
}
