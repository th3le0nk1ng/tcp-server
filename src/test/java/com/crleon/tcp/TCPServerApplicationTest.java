package com.crleon.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TCPServerApplicationTest {

  private ExecutorService executorService;

  @Before
  public void setup() {
    executorService = Executors.newFixedThreadPool(250);
  }

  @Test
  public void simulateLoadTest() throws IOException {
    // Run test after starting TCPServerApplication class
    /*for (int i = 0; i < 250; i++) {
      executorService.submit(new DataWorker(new Socket("localhost", 4000)));
    }*/
  }

  @After
  public void teardown() {
    executorService.shutdown();
    try {
      executorService.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
  }
}
