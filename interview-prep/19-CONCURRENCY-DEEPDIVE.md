# Java Concurrency — Deep Dive

Builds on 02-JAVA-ADVANCED. Senior Java rounds probe these concurrency utilities and the memory model.

## java.util.concurrent Utilities

### CountDownLatch
Wait for N tasks to finish before proceeding. One-time use.
```java
CountDownLatch latch = new CountDownLatch(3);
for (int i = 0; i < 3; i++) {
    executor.submit(() -> {
        doWork();
        latch.countDown();   // decrement
    });
}
latch.await();   // blocks until count reaches 0
System.out.println("All 3 tasks done");
```
Use case: wait for several services to initialize before starting.

### CyclicBarrier
Wait for N threads to reach a common point, then all proceed. Reusable (unlike latch).
```java
CyclicBarrier barrier = new CyclicBarrier(3, () -> System.out.println("All arrived"));
// each thread:
barrier.await();  // waits until 3 threads call await()
```
Use case: parallel computation phases where all must finish a phase before the next.

### Semaphore
Limits how many threads access a resource concurrently.
```java
Semaphore semaphore = new Semaphore(5);  // max 5 permits
semaphore.acquire();   // take a permit (blocks if none)
try { useResource(); }
finally { semaphore.release(); }  // return permit
```
Use case: limit concurrent DB connections or API calls (like a bulkhead).

### BlockingQueue (producer-consumer)
Thread-safe queue that blocks when empty (take) or full (put).
```java
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);

// Producer
queue.put(task);        // blocks if full

// Consumer
Task task = queue.take();  // blocks if empty
```
Use case: classic producer-consumer without manual wait/notify.

### ConcurrentHashMap
Thread-safe map with bucket-level locking (see 13-DIFFERENCES-JAVA). Atomic operations:
```java
map.putIfAbsent(key, value);
map.computeIfAbsent(key, k -> expensive(k));  // computed once even under contention
map.merge(key, 1, Integer::sum);              // atomic counter per key
```

## ExecutorService Deep Dive

### Thread pool types
```java
Executors.newFixedThreadPool(n)      // fixed n threads
Executors.newCachedThreadPool()      // grows/shrinks as needed
Executors.newSingleThreadExecutor()  // one thread, sequential
Executors.newScheduledThreadPool(n)  // delayed/periodic
Executors.newVirtualThreadPerTaskExecutor()  // Java 21 virtual threads
```

### ThreadPoolExecutor tuning (senior topic)
```java
new ThreadPoolExecutor(
    corePoolSize,     // threads kept alive
    maxPoolSize,      // max under load
    keepAliveTime,    // idle timeout for extra threads
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(capacity),  // work queue
    new ThreadPoolExecutor.CallerRunsPolicy()  // rejection policy
);
```
- **Rejection policies:** AbortPolicy (throws), CallerRunsPolicy (caller runs it), DiscardPolicy, DiscardOldestPolicy.
- Sizing: CPU-bound ≈ #cores; I/O-bound can be higher (threads mostly wait).

## ForkJoinPool
Splits a task into subtasks (divide-and-conquer), executes in parallel, joins results. Uses work-stealing (idle threads steal tasks from busy ones). Backs parallel streams.
```java
list.parallelStream().filter(...).collect(...);  // uses common ForkJoinPool
```

## ThreadLocal
Each thread gets its own isolated copy of a variable.
```java
ThreadLocal<SimpleDateFormat> formatter =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
formatter.get().format(new Date());  // each thread has its own formatter
```
Use case: per-thread context (user, transaction ID). **Warning:** clear it (`remove()`) in thread pools to avoid leaks and stale data.

## Java Memory Model (JMM) & happens-before

The JMM defines when one thread's writes become visible to another. The key concept is **happens-before**: if action A happens-before B, then A's effects are visible to B.

Happens-before is established by:
- Releasing a lock happens-before acquiring it (synchronized)
- Writing a volatile happens-before reading it
- Thread.start() happens-before the thread's actions
- A thread's actions happen-before another thread's join() returning

```java
// Without volatile/synchronized, thread B may never see the update
volatile boolean ready = false;
int data = 0;

// Thread A
data = 42;
ready = true;    // volatile write — happens-before B's read

// Thread B
if (ready) {     // volatile read
    use(data);   // guaranteed to see 42
}
```

## Deadlock, Livelock, Starvation

- **Deadlock** — threads each hold a lock the other needs; both wait forever.
- **Livelock** — threads keep responding to each other and make no progress (like two people stepping aside repeatedly).
- **Starvation** — a thread never gets CPU/resources because others monopolize them.

**Deadlock prevention:** acquire locks in a consistent global order, use `tryLock` with timeout, minimize lock scope.

## Locks (beyond synchronized)

### ReentrantLock
More flexible than synchronized — tryLock, timed lock, interruptible, fairness.
```java
ReentrantLock lock = new ReentrantLock();
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try { critical(); }
    finally { lock.unlock(); }  // always unlock in finally
}
```

### ReadWriteLock
Multiple readers OR one writer — great for read-heavy data.
```java
ReadWriteLock rw = new ReentrantReadWriteLock();
rw.readLock().lock();   // many threads can read together
rw.writeLock().lock();  // exclusive for writing
```

## Common Concurrency Interview Questions

**Q: CountDownLatch vs CyclicBarrier?**
> CountDownLatch is one-time — threads wait for a count to reach zero (wait for N tasks). CyclicBarrier is reusable — N threads wait for each other at a barrier, then all proceed, and it resets.

**Q: How do you implement producer-consumer?**
> Use a BlockingQueue — producers `put()` (blocks if full), consumers `take()` (blocks if empty). It handles all synchronization internally, no manual wait/notify.

**Q: What is happens-before?**
> A guarantee in the Java Memory Model that one action's effects are visible to another. Established by synchronized, volatile, thread start/join. Without it, threads may see stale values.

**Q: How do you size a thread pool?**
> CPU-bound tasks ≈ number of cores. I/O-bound tasks can use more since threads spend time waiting. Measure and tune. With Java 21 virtual threads, you can use a virtual-thread-per-task executor for blocking I/O.

**Q: What is a ThreadLocal memory leak?**
> In a thread pool, threads are reused, so a ThreadLocal value persists across tasks and never gets garbage-collected. Always call `remove()` when done to avoid stale data and leaks.

**Q: ReentrantLock vs synchronized?**
> Both provide mutual exclusion. ReentrantLock adds tryLock (non-blocking), timed/interruptible locking, and fairness options — more flexible but you must manually unlock in a finally block. synchronized is simpler and auto-releases.
