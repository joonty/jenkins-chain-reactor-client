/*
 * The MIT License
 *
 * Copyright 2013 Jon Cairns <jon@joncairns.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.chainreactorclient;

import java.util.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ChainReactorInvalidServerException extends Exception {
  ChainReactorInvalidServerException(String message) {
    super(message);
  }
}

public class ChainReactorConnector {

  private AbstractBuild build;
  private PrintStream logger;
  private String json;
  private int acceptableMajorVersion = 0;

  ChainReactorConnector(AbstractBuild build, PrintStream logger) {
    this.build = build;
    this.logger = logger;
    JSONObject jsonobj = new JSONObject();
    jsonobj.put("project",this.build.getProject().getName());
    jsonobj.put("build_no",this.build.getNumber());
    jsonobj.put("build",this.build.getFullDisplayName());
    jsonobj.put("url",this.build.getAbsoluteUrl());
    Result res = build.getResult();
    jsonobj.put("result",res.toString());
    this.json = jsonobj.toJSONString();
  }

  public boolean connect(ChainReactorServer server) {
    try {
      InetSocketAddress sockaddr = server.getSocketAddress();
      Socket sock = new Socket();
      sock.setSoTimeout(2000);
      sock.connect(sockaddr,2000);
      BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
      ensureValidServer(rd);
      logger.println("Connected to chain reactor server");
      sendBuildInfo(wr);
      rd.close();
      wr.close();
      sock.close();
    } catch (UnknownHostException uhe) {
      logError("Failed to resolve host: "+server.getUrl());
    } catch (SocketTimeoutException ste) {
      logError("Time out while trying to connect to "+server.toString());
    } catch (IOException ioe) {
      logError(ioe.getMessage());
    } catch (ChainReactorInvalidServerException crise) {
      logError(crise.getMessage());
    }

    return true;
  }

  protected void logError(String error) {
    logger.println(error);
    System.err.println("Chain reactor client error: "+error);
  }

  /* Check that the server is actually chain reactor. 
   *
   * This is done by checking the welcome text from the server.
   */
  protected void ensureValidServer(BufferedReader rd) 
    throws ChainReactorInvalidServerException, IOException {
    String str = rd.readLine();
    if (str != null) {
      Pattern pattern = Pattern.compile("^ChainReactor v([0-9]+)(.[0-9]+.[0-9])");
      Matcher matcher = pattern.matcher(str);
      if (matcher.find()) {
        int majorVersion = Integer.parseInt(matcher.group(1));
        String fullVersion = majorVersion + matcher.group(2);
        if (majorVersion != acceptableMajorVersion) {
          throw new ChainReactorInvalidServerException("Incompatible version of chain reactor server, " + fullVersion + " (this client is compatible with " + acceptableMajorVersion + ".x.x");
        }
        return;
      } else {
        throw new ChainReactorInvalidServerException("Invalid welcome message from server socket - not a chain reactor server!");
      }
    } else {
      throw new ChainReactorInvalidServerException("No text response from server socket - not a chain reactor server!");
    }
  }

  protected void sendBuildInfo(BufferedWriter writer) throws IOException {
    writer.write(this.json);
    writer.flush();
  }
}
