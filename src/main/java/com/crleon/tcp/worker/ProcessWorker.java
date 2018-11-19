package com.crleon.tcp.worker;

import static com.crleon.tcp.TCPServerApplication.executorService;
import static com.crleon.tcp.TCPServerApplication.scheduledExecutorService;
import static com.crleon.tcp.constant.ApplicationConstants.NUMBERS_LOG;
import static com.crleon.tcp.constant.ApplicationConstants.TIMEOUT;

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

  private static final String TERMINATE = "terminate";
  private Socket socket;

  public ProcessWorker(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      String input;
      while ((input = br.readLine()) != null) {
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
      LOGGER.error("Failed to read input from client socket");
    }
  }

  /**
   * Validates that the input received is a 9 digit number
   * @param input string received from client socket
   * @return true if input passes validation, else false
   */
  private boolean isValidInput(String input) {
    return input.matches("^\\d{9}$");
  }

  /**
   * Writes 9 digit number to log file
   * @param input string received from client socket
   */
  private void writeToLogFile(String input) {
    // Strip leading zeroes when writing to log file to save disk space
    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(NUMBERS_LOG, true)))) {
      writer.println(Integer.parseInt(input));
    } catch (IOException ioe) {
      LOGGER.error("Failed to write input: {} to {}", input, NUMBERS_LOG);
    }
  }

  /**
   * Determines if the application should shutdown based on client input
   * @param input string received from client socket
   */
  private void handleInvalidInput(String input) {
    if (StringUtils.equals(input, TERMINATE)) {
      terminateApplication();
    }
  }

  /**
   * Gracefully shutdown application within a set timeout
   */
  private void terminateApplication() {
    LOGGER.debug("Starting graceful termination of application...");

    executorService.shutdown();
    try {
      LOGGER.info("Waiting up to {} seconds for ExecutorService to terminate", TIMEOUT);
      if (!executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      LOGGER.error("ExecutorService graceful shutdown was interrupted. Forcing immediate shutdown now.");
      executorService.shutdownNow();
    }

    scheduledExecutorService.shutdown();
    try {
      LOGGER.info("Waiting up to {} seconds for ScheduledExecutorService to terminate", TIMEOUT);
      if (!scheduledExecutorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
        scheduledExecutorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      LOGGER.error("ScheduledExecutorService graceful shutdown was interrupted. Forcing immediate shutdown now.");
      scheduledExecutorService.shutdownNow();
    }

    LOGGER.debug("All worker threads have shutdown");
    LOGGER.debug("Shutting down application...");
    System.exit(0);
  }
}