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
package org.apache.catalina.core;

import org.apache.catalina.Globals;
import org.apache.catalina.security.SecurityUtil;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;

/**
 * Implementation of <code>javax.servlet.FilterChain</code> used to manage
 * the execution of a set of filters for a particular request.  When the
 * set of defined filters has all been executed, the next call to
 * <code>doFilter()</code> will execute the servlet's <code>service()</code>
 * method itself.
 *
 * @author Craig R. McClanahan
 */
public final class JPFFilterChain implements FilterChain {

  // -------------------------------------------------------------- Constants


  public static final int INCREMENT = 10;


  // ----------------------------------------------------- Instance Variables

  /**
   * Filters.
   */
  private ApplicationFilterConfig[] filters = new ApplicationFilterConfig[0];


  /**
   * The int which is used to maintain the current position
   * in the filter chain.
   */
  private int pos = 0;


  /**
   * The int which gives the current number of filters in the chain.
   */
  private int n = 0;


  /**
   * The servlet instance to be executed by this chain.
   */
  private Servlet servlet = null;


  /**
   * Does the associated servlet instance support async processing?
   */
  private boolean servletSupportsAsync = false;

  /**
   * The string manager for our package.
   */
  private static final StringManager sm =
      StringManager.getManager(Constants.Package);


  /**
   * Static class array used when the SecurityManager is turned on and
   * <code>doFilter</code> is invoked.
   */
  private static final Class<?>[] classType = new Class[]{
      ServletRequest.class, ServletResponse.class, FilterChain.class};

  /**
   * Static class array used when the SecurityManager is turned on and
   * <code>service</code> is invoked.
   */
  private static final Class<?>[] classTypeUsedInService = new Class[]{
      ServletRequest.class, ServletResponse.class};


  // ---------------------------------------------------- FilterChain Methods

  /**
   * Invoke the next filter in this chain, passing the specified request
   * and response.  If there are no more filters in this chain, invoke
   * the <code>service()</code> method of the servlet itself.
   *
   * @param request The servlet request we are processing
   * @param response The servlet response we are creating
   *
   * @exception IOException if an input/output error occurs
   * @exception ServletException if a servlet exception occurs
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response)
      throws IOException, ServletException {

    if( Globals.IS_SECURITY_ENABLED ) {
      final ServletRequest req = request;
      final ServletResponse res = response;
      try {
        java.security.AccessController.doPrivileged(
            (PrivilegedExceptionAction<Void>) () -> {
              internalDoFilter(req,res);
              return null;
            }
        );
      } catch( PrivilegedActionException pe) {
        Exception e = pe.getException();
        if (e instanceof ServletException)
          throw (ServletException) e;
        else if (e instanceof IOException)
          throw (IOException) e;
        else if (e instanceof RuntimeException)
          throw (RuntimeException) e;
        else
          throw new ServletException(e.getMessage(), e);
      }
    } else {
      internalDoFilter(request,response);
    }
  }

  private void internalDoFilter(ServletRequest request,
                                ServletResponse response)
      throws IOException, ServletException {

    // Call the next filter if there is one
    if (pos < n) {
      ApplicationFilterConfig filterConfig = filters[pos++];
      try {
        Filter filter = filterConfig.getFilter();
        System.out.println("Prepare filter at " + (pos - 1));
        if (request.isAsyncSupported() && "false".equalsIgnoreCase(
            filterConfig.getFilterDef().getAsyncSupported())) {
          request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR, Boolean.FALSE);
        }
        if( Globals.IS_SECURITY_ENABLED ) {
          final ServletRequest req = request;
          final ServletResponse res = response;
          Principal principal =
              ((HttpServletRequest) req).getUserPrincipal();

          Object[] args = new Object[]{req, res, this};
          SecurityUtil.doAsPrivilege ("doFilter", filter, classType, args, principal);
        } else {
          System.out.println("Do filter:" + filter.getClass().getName());
          filter.doFilter(request, response, this);
        }
      } catch (IOException | ServletException | RuntimeException e) {
        throw e;
      } catch (Throwable e) {
        e = ExceptionUtils.unwrapInvocationTargetException(e);
        ExceptionUtils.handleThrowable(e);
        throw new ServletException(sm.getString("filterChain.filter"), e);
      }
      return;
    }

    // We fell off the end of the chain -- call the servlet instance
    try {
      if (request.isAsyncSupported() && !servletSupportsAsync) {
        request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR,
            Boolean.FALSE);
      }
      // Use potentially wrapped request from this point
      if ((request instanceof HttpServletRequest) &&
          (response instanceof HttpServletResponse) &&
          Globals.IS_SECURITY_ENABLED ) {
        final ServletRequest req = request;
        final ServletResponse res = response;
        Principal principal =
            ((HttpServletRequest) req).getUserPrincipal();
        Object[] args = new Object[]{req, res};
        SecurityUtil.doAsPrivilege("service",
            servlet,
            classTypeUsedInService,
            args,
            principal);
      } else {
        servlet.service(request, response);
      }
    } catch (IOException | ServletException | RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      e = ExceptionUtils.unwrapInvocationTargetException(e);
      ExceptionUtils.handleThrowable(e);
      throw new ServletException(sm.getString("filterChain.servlet"), e);
    } finally {
    }
  }



  // -------------------------------------------------------- Package Methods

  /**
   * Add a filter to the set of filters that will be executed in this chain.
   *
   * @param filterConfig The FilterConfig for the servlet to be executed
   */
  public void addFilter(ApplicationFilterConfig filterConfig) {

    // Prevent the same filter being added multiple times
    for(ApplicationFilterConfig filter:filters)
      if(filter==filterConfig)
        return;

    if (n == filters.length) {
      ApplicationFilterConfig[] newFilters =
          new ApplicationFilterConfig[n + INCREMENT];
      System.arraycopy(filters, 0, newFilters, 0, n);
      filters = newFilters;
    }
    filters[n++] = filterConfig;

  }


  /**
   * Release references to the filters and wrapper executed by this chain.
   */
  void release() {
    for (int i = 0; i < n; i++) {
      filters[i] = null;
    }
    n = 0;
    pos = 0;
    servlet = null;
    servletSupportsAsync = false;
  }


  /**
   * Prepare for reuse of the filters and wrapper executed by this chain.
   */
  void reuse() {
    pos = 0;
  }


  /**
   * Set the servlet that will be executed at the end of this chain.
   *
   * @param servlet The Wrapper for the servlet to be executed
   */
  void setServlet(Servlet servlet) {
    this.servlet = servlet;
  }


  void setServletSupportsAsync(boolean servletSupportsAsync) {
    this.servletSupportsAsync = servletSupportsAsync;
  }


  /**
   * Identifies the Filters, if any, in this FilterChain that do not support
   * async.
   *
   * @param result The Set to which the fully qualified class names of each
   *               Filter in this FilterChain that does not support async will
   *               be added
   */
  public void findNonAsyncFilters(Set<String> result) {
    for (int i = 0; i < n ; i++) {
      ApplicationFilterConfig filter = filters[i];
      if ("false".equalsIgnoreCase(filter.getFilterDef().getAsyncSupported())) {
        result.add(filter.getFilterClass());
      }
    }
  }
}
