package com.crleon.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import org.apache.commons.lang3.RandomStringUtils;

public class DataWorker implements Runnable {
  private Socket socket;

  public DataWorker(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try(PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
      for (int i = 0; i < 999999999; i++) {
        out.println(RandomStringUtils.random(9, "0123456789"));
      }
    } catch (IOException ioe) {
    }
  }
}
