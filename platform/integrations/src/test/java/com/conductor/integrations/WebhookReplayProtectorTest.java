package com.conductor.integrations;

import static org.junit.jupiter.api.Assertions.*;

import com.conductor.integrations.webhooks.WebhookReplayProtector;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class WebhookReplayProtectorTest {

  private WebhookReplayProtector protector() {
    // 300s window, large cache
    return new WebhookReplayProtector(300, 10000);
  }

  @Test
  public void testReplayProtection() {
    WebhookReplayProtector protector = protector();
    String eventId = "evt_99999999";

    assertFalse(protector.isDuplicate(eventId));
    assertTrue(protector.isDuplicate(eventId));
  }

  @Test
  void firstOccurrence_notDuplicate() {
    assertFalse(protector().isDuplicate("evt_001"));
  }

  @Test
  void secondOccurrence_isDuplicate() {
    WebhookReplayProtector p = protector();
    p.isDuplicate("evt_002");
    assertTrue(p.isDuplicate("evt_002"));
  }

  @Test
  void nullEventId_neverDuplicate() {
    WebhookReplayProtector p = protector();
    assertFalse(p.isDuplicate(null));
    assertFalse(p.isDuplicate(null));
  }

  @Test
  void distinctIds_neverConflict() {
    WebhookReplayProtector p = protector();
    assertFalse(p.isDuplicate("evt_A"));
    assertFalse(p.isDuplicate("evt_B"));
    // second calls
    assertTrue(p.isDuplicate("evt_A"));
    assertTrue(p.isDuplicate("evt_B"));
  }

  /**
   * Regression: the old check-then-put pattern had a race condition where two concurrent threads
   * processing the same event ID could both see "not duplicate" and proceed. With putIfAbsent
   * exactly one thread should win; all others return duplicate=true.
   */
  @Test
  void concurrentDuplicateCheck_exactlyOneThreadWins() throws Exception {
    WebhookReplayProtector p = protector();
    String eventId = UUID.randomUUID().toString();
    int threads = 20;
    CountDownLatch ready = new CountDownLatch(threads);
    CountDownLatch go = new CountDownLatch(1);
    AtomicInteger notDuplicateCount = new AtomicInteger(0);

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    List<Future<?>> futures = new ArrayList<>();

    for (int i = 0; i < threads; i++) {
      futures.add(
          executor.submit(
              () -> {
                ready.countDown();
                try {
                  go.await();
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
                if (!p.isDuplicate(eventId)) {
                  notDuplicateCount.incrementAndGet();
                }
              }));
    }

    ready.await();
    go.countDown();
    for (Future<?> f : futures) f.get();
    executor.shutdown();

    assertEquals(
        1,
        notDuplicateCount.get(),
        "Exactly one thread should win the race; all others must see duplicate=true");
  }

  @Test
  void cacheDoesNotGrowUnbounded() {
    // Small cache to trigger pruning
    WebhookReplayProtector p = new WebhookReplayProtector(300, 50);
    for (int i = 0; i < 100; i++) {
      p.isDuplicate("evt_" + i);
    }
    // After pruning kicks in the cache should not exceed 2x maxCacheSize
    assertTrue(p.cacheSize() <= 100, "Cache should be bounded, got " + p.cacheSize());
  }
}
