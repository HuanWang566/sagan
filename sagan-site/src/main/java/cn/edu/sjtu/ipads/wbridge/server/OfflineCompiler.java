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
package cn.edu.sjtu.ipads.wbridge.server;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface OfflineCompiler extends Remote {

  class ServerMeta implements Serializable {
    String webHost;
    String webUser;
    String webPass;
    String webSpDefPath;
    String dbHost;
    String dbUser;
    String dbPass;

    public ServerMeta(
        String webHost,
        String webUser,
        String webPass,
        String webSpDefPath,
        String dbHost,
        String dbUser,
        String dbPass) {
      this.webHost = webHost;
      this.webUser = webUser;
      this.webPass = webPass;
      this.webSpDefPath = webSpDefPath;
      this.dbHost = dbHost;
      this.dbUser = dbUser;
      this.dbPass = dbPass;
    }
  }

  class Trace implements Serializable {
    String uniqueKey;
    String apiName;
    byte[] requestData;
    byte[] responseData;
    byte[] resultSetData;

    public Trace(
        String uniqueKey,
        String apiName,
        byte[] requestData,
        byte[] responseData,
        byte[] resultSetData) {
      this.uniqueKey = uniqueKey;
      this.apiName = apiName;
      this.requestData = requestData;
      this.responseData = responseData;
      this.resultSetData = resultSetData;
    }
  }

  void startCompile(ServerMeta serverMeta, Map<String, List<Trace>> traceMap)
      throws RemoteException;
}
