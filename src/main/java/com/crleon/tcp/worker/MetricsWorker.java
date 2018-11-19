package com.crleon.tcp.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsWorker implements Runnable {
  static final Logger LOGGER = LoggerFactory.getLogger(MetricsWorker.class);

  @Override
  public void run() {
    LOGGER.info("Received {} unique numbers, {} duplicates. Unique total: {}",
        ProcessWorker.uniqueCounter.getAndSet(0), ProcessWorker.duplicateCounter.getAndSet(0),
        ProcessWorker.concurrentSet.size());
  }
}