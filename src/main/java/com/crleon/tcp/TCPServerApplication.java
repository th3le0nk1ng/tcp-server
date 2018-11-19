package com.crleon.tcp;

import com.crleon.tcp.worker.MetricsWorker;
import com.crleon.tcp.worker.ProcessWorker;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPServerApplication {

  static final Logger LOGGER = LoggerFactory.getLogger(TCPServerApplication.class);
  public static ExecutorService executorService = Executors.newFixedThreadPool(5);
  public static ScheduledExecutorService scheduledExecutorService = Executors
      .newSingleThreadScheduledExecutor();

  private int port;

  public TCPServerApplication(int port) {
    this.port = port;
  }

  /**
   * Starts the TCP server on given port and creates a thread for each incoming socket connection
   */
  public void start() throws IOException {
    LOGGER.info("Deleting file: [{}]", "numbers.log");
    Files.deleteIfExists(new File("numbers.log").toPath());

    LOGGER.info("Starting TCP server on port {}...", port);

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      LOGGER.info("Started TCP server on port {}", port);
      scheduledExecutorService.scheduleAtFixedRate(new MetricsWorker(), 10, 10, TimeUnit.SECONDS);

      while (true) {
        executorService.submit(new ProcessWorker(serverSocket.accept()));
      }
    }
  }

  public static void main(String[] args) throws IOException {
    TCPServerApplication tcpServerApplication = new TCPServerApplication(4000);
    tcpServerApplication.start();
  }
}