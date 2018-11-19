package com.crleon.tcp;

import com.crleon.tcp.worker.MetricsWorker;
import com.crleon.tcp.worker.ProcessWorker;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPServerApplication {
  static final Logger LOGGER = LoggerFactory.getLogger(TCPServerApplication.class);
  public static ExecutorService executorService = Executors.newFixedThreadPool(5) ;
  public static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

  private int port;

  public TCPServerApplication(int port) {
    this.port = port;
  }

  public void start() throws IOException {
    LOGGER.info("Starting TCP server on port {}", port);
    ServerSocket serverSocket = new ServerSocket(port);

    // Schedule task to print metrics to SYSOUT every 10 seconds
    scheduledExecutorService.scheduleAtFixedRate(new MetricsWorker(), 10, 10, TimeUnit.SECONDS);

    while (true) {
      executorService.submit(new ProcessWorker(serverSocket.accept()));
    }
  }

  public static void main(String[] args) throws IOException {
    TCPServerApplication tcpServerApplication = new TCPServerApplication(4000);
    tcpServerApplication.start();
  }
}