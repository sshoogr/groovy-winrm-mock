/*
 * Copyright (C) 2011-2015 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aestasit.infrastructure.winrm.mock.server

import org.apache.commons.io.IOUtils
import org.mortbay.jetty.Handler
import org.mortbay.jetty.HttpConnection
import org.mortbay.jetty.Request
import org.mortbay.jetty.Server
import org.mortbay.jetty.handler.AbstractHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static javax.servlet.http.HttpServletResponse.SC_OK
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import static org.apache.commons.io.IOUtils.write

/**
 * HTTP server implementation for use during WinRM tests.
 *
 * @author Sergey Korenko
 */
public class WinRMTestServer {

  static final int HTTP_PORT = 5985
  static final int HTTPS_PORT = 5986

  Server server
  Map requestResponseMock = [:]

  void start(useHTTP) throws Exception {
    server = new Server(useHTTP ? HTTP_PORT : HTTPS_PORT)
    server.handler = getMockHandler()
    server.start()
  }

  Handler getMockHandler() {
    new AbstractHandler() {
      public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        Request baseRequest = request instanceof Request ? request : HttpConnection.currentConnection.request
        def requestBody = IOUtils.toString(baseRequest.inputStream)
        def responseBody = getResponseMockByRequest(requestBody)
        response.status = responseBody ? SC_OK : SC_NOT_FOUND
        response.contentType = "text/xml;charset=utf-8"
        write responseBody, response.outputStream

        baseRequest.handled = true
      }
    }
  }

  void stop() throws Exception {
    server.stop()
  }

  String getResponseMockByRequest(def requestBody){
    requestResponseMock.find { requestBody.contains(it.key)}?.value
  }
}