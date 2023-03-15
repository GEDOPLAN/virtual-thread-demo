package de.gedoplan.showcase;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassicThreads {

  private static ExecutorService threadPool = Executors.newCachedThreadPool();
  private static AtomicInteger threadCount = new AtomicInteger();
  private static Phaser barrier = new Phaser(2);

  public static void main(String[] args) {
    while (true) {
      threadPool.execute(ClassicThreads::doSomething);
      barrier.arriveAndAwaitAdvance();
    }
  }

  private static void doSomething() {
    int count = threadCount.incrementAndGet();

    if ((count % 100) == 0) {
      System.out.printf(Locale.GERMANY, "Thread %,d%n", count);
    }

    barrier.arrive();

    sleepOneDay();
  }
  
  private static void sleepOneDay() {
    try {
      TimeUnit.DAYS.sleep(1);
    } catch (InterruptedException e) {
    }
  }
}
