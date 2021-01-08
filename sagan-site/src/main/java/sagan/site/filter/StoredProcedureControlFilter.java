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

import cn.edu.sjtu.ipads.wbridge.utils.BufferedServletRequestWrapper;
import cn.edu.sjtu.ipads.wbridge.utils.WBridgeSaganParamUtils;
import edu.sjtu.ipads.wbridge.storedprocedure.invocation.SPInvokeManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component("saganStoredProcedureControlFilter")
public class StoredProcedureControlFilter extends OncePerRequestFilter implements Ordered {

  public StoredProcedureControlFilter(
          // ClientRegistrationRepository clientRegistrationRepository,
          // OAuth2AuthorizedClientRepository authorizedClientRepository,
      @Value("${enable.sp.apilist}") String enableSpApiList,
      @Value("${spdef.dir}") String spDefDir) {
    // this.clientRegistrationRepository = clientRegistrationRepository;
    // this.authorizedClientRepository = authorizedClientRepository;
    // initialize enabled api
    if (!StringUtils.isEmpty(enableSpApiList)) {
      Arrays.stream(enableSpApiList.split(",")).forEach(WBridgeSaganParamUtils::addEnableSpApi);
    }
    // set the sp Def dir
    System.setProperty("spDefDir", spDefDir);
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }

  // private final ClientRegistrationRepository clientRegistrationRepository;
  // private final OAuth2AuthorizedClientRepository authorizedClientRepository;
  // private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
  //         new HttpSessionOAuth2AuthorizationRequestRepository();
  // private static final String AUTHORIZATION_REQUEST_NOT_FOUND_ERROR_CODE = "authorization_request_not_found";
  // private static final String CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE = "client_registration_not_found";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (!WBridgeSaganParamUtils.shouldUseSpThisTime(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // we need wrapper the request, to enable read request body input stream multiple times
      final BufferedServletRequestWrapper requestWrapper =
          WBridgeSaganParamUtils.bufferRequestBody(request);

      WBridgeSaganParamUtils.prepareSPParam(requestWrapper);
      final String apiName = WBridgeSaganParamUtils.getSPDefApi(request);
      SPInvokeManager.beginApi(apiName);
      filterChain.doFilter(requestWrapper, response);
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      SPInvokeManager.endApi();
    }
  }
}
