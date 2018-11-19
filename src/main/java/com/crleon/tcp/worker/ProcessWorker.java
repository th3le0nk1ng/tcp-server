package com.crleon.tcp.worker;

import static com.crleon.tcp.TCPServerApplication.executorService;
import static com.crleon.tcp.TCPServerApplication.scheduledExecutorService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessWorker implements Runnable {
  static final Logger LOGGER = LoggerFactory.getLogger(ProcessWorker.class);
  static AtomicInteger duplicateCounter = new AtomicInteger(0);
  static AtomicInteger uniqueCounter = new AtomicInteger(0);
  static Set<String> concurrentSet = ConcurrentHashMap.newKeySet();

  private Socket socket;

  public ProcessWorker(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      String input;
      while ((input = br.readLine()) != null) {
        LOGGER.debug("Received input: {}", input);
        if (isValidInput(input)) {
          if (concurrentSet.add(input)) {
            uniqueCounter.getAndIncrement();
            writeToLogFile(input);
          } else {
            duplicateCounter.getAndIncrement();
          }
        } else {
          break;
        }
      }

      handleInvalidInput(input);
    } catch (IOException ioe) {
    }
  }

  private boolean isValidInput(String input) {
    return input.matches("^\\d{9}$");
  }

  private void writeToLogFile(String input) {
    // TODO: Write input string to a log file
    // Strip leading zeroes when writing to log file to save disk space
    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("numbers.log", true)))) {
      LOGGER.debug("Writing input: {} to numbers.log", input);
      writer.println(Integer.parseInt(input));
    } catch (IOException ioe) { }
  }

  private void handleInvalidInput(String input) {
    if (StringUtils.equals(input, "terminate")) {
      terminateApplication();
    }
  }

  private void terminateApplication() {
    LOGGER.debug("Forcing graceful termination of program");
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }

    scheduledExecutorService.shutdown();
    try {
      if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
        scheduledExecutorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduledExecutorService.shutdownNow();
    }

    LOGGER.debug("All worker threads have completed");
    LOGGER.debug("Shutting down application...");
    System.exit(0);
  }
}