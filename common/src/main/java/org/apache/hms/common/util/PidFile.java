/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hms.common.util;


import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.nio.channels.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PidFile extends Thread {

  String name;
  private static Log log = LogFactory.getLog(PidFile.class);
  private static FileLock lock = null;
  private static FileOutputStream pidFileOutput = null;
  private static ServerSocket serverSocket;

  public PidFile(String name, int port) {
    this.name = name;
    try {
      init(port);
    } catch (IOException ex) {
      clean();
      System.exit(-1);
    }
  }

  public void init(int port) throws IOException {
    String pidLong = ManagementFactory.getRuntimeMXBean().getName();
    String[] items = pidLong.split("@");
    String pid = items[0];
    String chukwaPath = System.getProperty("HMS_HOME");
    StringBuffer pidFilesb = new StringBuffer();
    String pidDir = System.getenv("HMS_PID_DIR");
    if (pidDir == null) {
      pidDir = chukwaPath + File.separator + "var" + File.separator + "run";
    }
    pidFilesb.append(pidDir).append(File.separator).append(name).append(".pid");
    try {
      serverSocket = new ServerSocket(port);
      
      File existsFile = new File(pidDir);
      if (!existsFile.exists()) {
        boolean success = (new File(pidDir)).mkdirs();
        if (!success) {
          throw (new IOException());
        }
      }
      File pidFile = new File(pidFilesb.toString());

      pidFileOutput = new FileOutputStream(pidFile);
      pidFileOutput.write(pid.getBytes());
      pidFileOutput.flush();
      log.debug("Initlization succeeded...");
    } catch (IOException ex) {
      System.out.println("Initialization failed: can not write pid file to " + pidFilesb);
      log.error("Initialization failed...");
      log.error(ex.getMessage());
      System.exit(-1);
      throw ex;
    }

  }

  public void clean() {
    String chukwaPath = System.getenv("HMS_HOME");
    StringBuffer pidFilesb = new StringBuffer();
    String pidDir = System.getenv("HMS_PID_DIR");
    if (pidDir == null) {
      pidDir = chukwaPath + File.separator + "var" + File.separator + "run";
    }
    pidFilesb.append(pidDir).append(File.separator).append(name).append(".pid");
    String pidFileName = pidFilesb.toString();

    File pidFile = new File(pidFileName);
    if (!pidFile.exists()) {
      log.error("Delete pid file, No such file or directory: " + pidFileName);
    } else {
      try {
        serverSocket.close();
      } catch (IOException e) {
        log.error("Unable to release file lock: " + pidFileName);
      }
    }

    boolean result = pidFile.delete();
    if (!result) {
      log.error("Delete pid file failed, " + pidFileName);
    }
  }

  public void run() {
    clean();
  }
}
