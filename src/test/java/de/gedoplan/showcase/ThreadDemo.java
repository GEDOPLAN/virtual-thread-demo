package de.gedoplan.showcase;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadDemo {

  private long start = System.currentTimeMillis();
  private AtomicInteger threadCount = new AtomicInteger();
  private Phaser barrier = new Phaser(2);
  private Logger logger = Logger.getLogger(ThreadDemo.class);

  /**
   * Start threads by constructing them.
   * This is not recommended - use a thread pool instead.
   */
  @Test
  void thread() {
    // while (true) {
    for (int i = 0; i < 10_000; ++i) {
      // Start doSomething in new (platform) thread
      new Thread(this::doSomething).start();

      // Wait for new thread to be alive before continuing with the next one
      barrier.arriveAndAwaitAdvance();
    }
  }

  /**
   * Start threads from pool.
   * In this special case, no thread is reused, because they run indefinitely.
   */
  @Test
  void pool() {
    ExecutorService executor = Executors.newCachedThreadPool();
    // while (true) {
    for (int i = 0; i < 10_000; ++i) {
      // Start doSomething in new (platform) thread
      executor.execute(this::doSomething);

      // Wait for new thread to be alive before continuing with the next one
      barrier.arriveAndAwaitAdvance();
    }
  }

  /**
   * Start threads from virtual thread executor.
   * No thread is reused, because they are so cheap.
   */
  @Test
  void virtual() {

    ThreadFactory factory = Thread.ofVirtual().name("virtual-", 1).factory();
    ExecutorService executor = Executors.newThreadPerTaskExecutor(factory);
    // while (true) {
    for (int i = 0; i < 1_000_000; ++i) {
      // Start doSomething in new virtual thread
      executor.submit(this::doSomething);

      // Wait for new thread to be alive before continuing with the next one
      barrier.arriveAndAwaitAdvance();
    }
  }

  private void doSomething() {

    // Increment and print thread count (every 1000)
    int count = threadCount.incrementAndGet();
    if ((count % 1000) == 0) {
      long elapsedMillis = System.currentTimeMillis() - start;

      logger.infof("%,9d (%d ns/thread) %s", count, elapsedMillis*1000/count, Thread.currentThread());
    }

    // Tell main, I'm alive
    barrier.arrive();

    // Take a rest
    try {
      TimeUnit.DAYS.sleep(1);
    } catch (InterruptedException e) {
      // ignore
    }
  }

}
