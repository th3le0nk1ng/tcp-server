package com.crleon.tcp.worker;

import java.time.LocalDateTime;

public class MetricsWorker implements Runnable {

  @Override
  public void run() {
    String metrics = String.format("Received %s unique numbers, %s duplicates. Unique total: %s",
        ProcessWorker.uniqueCounter.getAndSet(0), ProcessWorker.duplicateCounter.getAndSet(0),
        ProcessWorker.concurrentSet.size());
    System.out.println("[" + LocalDateTime.now().toString() + "] " + metrics);
  }
}