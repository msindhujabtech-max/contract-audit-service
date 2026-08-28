# Java Advanced — Concurrency, JVM, Memory

## Multithreading & Concurrency

### Creating Threads (4 ways)

```java
// 1. Extend Thread
class MyThread extends Thread {
    public void run() { System.out.println("Running"); }
}
new MyThread().start();

// 2. Implement Runnable (preferred — allows extending other classes)
Runnable r = () -> System.out.println("Running");
new Thread(r).start();

// 3. Callable + ExecutorService (returns a result)
ExecutorService executor = Executors.newFixedThreadPool(4);
Future<Integer> future = executor.submit(() -> 42);
Integer result = future.get();

// 4. Virtual Threads (Java 21)
Thread.ofVirtual().start(() -> System.out.println("Virtual"));
```

### Thread Lifecycle States

```
NEW → RUNNABLE → RUNNING → (BLOCKED/WAITING/TIMED_WAITING) → TERMINATED
```

### synchronized keyword

Ensures only one thread executes a block/method at a time.
```java
class Counter {
    private int count = 0;
    public synchronized void increment() { count++; } // method-level lock

    public void increment2() {
        synchronized(this) { count++; }  // block-level lock
    }
}
```

### volatile keyword

Guarantees visibility — changes by one thread are immediately visible to others (no CPU caching). Does NOT guarantee atomicity.
```java
private volatile boolean running = true;
// Thread A sets running = false; Thread B sees it immediately
```

### synchronized vs volatile vs Atomic

| | synchronized | volatile | AtomicInteger |
|--|-------------|----------|---------------|
| Visibility | Yes | Yes | Yes |
| Atomicity | Yes | No | Yes |
| Locking | Yes (blocks) | No | No (CAS) |
| Use | Complex critical sections | Simple flags | Counters |

**In plain terms:** These three solve different slices of the same problem. `volatile` only fixes *visibility* — it guarantees every thread sees the latest value, but `count++` is still three steps (read, add, write) so two threads can clobber each other. `synchronized` fixes both visibility *and* atomicity by letting only one thread into the block at a time, but the waiting threads are blocked (slower). `AtomicInteger` gives you atomic updates without blocking by using a CPU-level Compare-And-Swap — ideal for a simple shared counter like "how many audits have we processed". Rule of thumb: `volatile` for an on/off flag, `Atomic*` for counters, `synchronized` when you must update several fields together as one unit.

```java
// volatile: safe as a flag, but NOT safe for count++ (lost updates under load)
private volatile boolean shutdownRequested = false;

// AtomicInteger: safe, lock-free counter — great for tallying processed audits
AtomicInteger processed = new AtomicInteger();
processed.incrementAndGet();               // atomic, no other thread can interleave

// synchronized: needed when two fields must change together atomically
synchronized (this) { total += amount; itemCount++; }
```

```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();  // atomic, no lock, uses CAS (Compare-And-Swap)
```

### ExecutorService & Thread Pools

```java
// Fixed pool — reuses N threads
ExecutorService pool = Executors.newFixedThreadPool(10);

// Cached pool — creates threads as needed, reuses idle ones
ExecutorService cached = Executors.newCachedThreadPool();

// Scheduled — for delayed/periodic tasks
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);

pool.submit(() -> doWork());
pool.shutdown();  // graceful shutdown
```

### CompletableFuture (async programming)

```java
CompletableFuture.supplyAsync(() -> fetchData())
    .thenApply(data -> process(data))
    .thenAccept(result -> save(result))
    .exceptionally(ex -> { log(ex); return null; });

// Combine two async calls
CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> b = CompletableFuture.supplyAsync(() -> "World");
a.thenCombine(b, (x, y) -> x + " " + y);  // "Hello World"
```

### Deadlock (know how to explain + prevent)

**Deadlock:** Two threads each hold a lock the other needs.
```java
// Thread 1: lock A, then B
// Thread 2: lock B, then A
// → both wait forever
```
**Prevention:** Always acquire locks in the same order, use timeouts (`tryLock`), avoid nested locks.

---

## JVM Architecture

```
┌─────────────────────────────────────────────┐
│              JVM                              │
│  ┌──────────────┐  ┌─────────────────────┐  │
│  │ Class Loader │  │   Runtime Data Areas │  │
│  │              │  │  ┌──────┐ ┌────────┐ │  │
│  │ - Loading    │  │  │ Heap │ │ Stack  │ │  │
│  │ - Linking    │  │  ├──────┤ ├────────┤ │  │
│  │ - Init       │  │  │ Meta │ │ PC Reg │ │  │
│  └──────────────┘  │  │ space│ │        │ │  │
│                    │  └──────┘ └────────┘ │  │
│  ┌──────────────────────────────────────┐  │
│  │  Execution Engine (JIT + GC)         │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

### Memory Areas

| Area | Stores | Shared? |
|------|--------|---------|
| **Heap** | Objects, instance variables | Yes (all threads) |
| **Stack** | Method calls, local variables | No (per thread) |
| **Metaspace** | Class metadata (Java 8+, replaced PermGen) | Yes |
| **PC Register** | Current instruction address | No (per thread) |

**In plain terms:** The key distinction interviewers probe is *heap vs stack*. The **heap** is one big shared warehouse where all objects live; any thread can reach an object if it has a reference to it. The **stack** is private scratch space per thread — it holds local variables and the trail of method calls, and it's wiped as each method returns. That's why local variables are inherently thread-safe (each thread has its own stack) but object fields on the heap are not (they're shared). A `StackOverflowError` means the stack ran out (usually runaway recursion); an `OutOfMemoryError: Java heap space` means the warehouse filled up.

```java
void process() {
    int localCount = 0;                 // 'localCount' lives on THIS thread's stack
    AuditRequest req = new AuditRequest(); // the object lives on the shared heap...
    // ...but 'req' (the reference/pointer to it) sits on the stack
}
// When process() returns, the stack frame (localCount, req) is gone.
// The AuditRequest object stays on the heap until no references point to it (then GC reclaims it).
```

### Heap structure (for Garbage Collection)

```
Heap
├── Young Generation
│   ├── Eden (new objects)
│   ├── Survivor S0
│   └── Survivor S1
└── Old Generation (long-lived objects)
```

- New objects → Eden
- Minor GC clears Eden, survivors move to S0/S1
- Objects surviving many GCs → promoted to Old Gen
- Major/Full GC cleans Old Gen (more expensive)

### Garbage Collectors

| GC | Best for |
|----|----------|
| Serial GC | Small apps, single-threaded |
| Parallel GC | Throughput-focused (Java 8 default) |
| G1 GC | Balanced, large heaps (Java 9+ default) |
| ZGC / Shenandoah | Low-latency, huge heaps (Java 15+) |

**In plain terms:** All GCs do the same job — reclaim objects nobody references anymore — they just make different trade-offs between *throughput* (total work done) and *pause time* (how long the app freezes during collection). Serial GC uses one thread and is fine for tiny tools. Parallel GC uses many threads to maximize throughput but has longer pauses. G1 (the modern default) splits the heap into regions and collects them incrementally to keep pauses short and predictable — the sweet spot for most Spring Boot microservices. ZGC/Shenandoah push pauses down to sub-millisecond for very large heaps where even G1's pauses hurt. You rarely pick a GC in interviews, but you should know *why* you'd switch: latency-sensitive service under load → G1 or ZGC.

```bash
# You select the GC with a JVM flag at startup — no code change needed
java -XX:+UseG1GC   -Xms512m -Xmx2g -jar contract-audit-service.jar   # balanced default
java -XX:+UseZGC    -Xmx16g  -jar contract-audit-service.jar          # low-latency, big heap
```

### Memory Leaks in Java (yes, they happen)

Even with GC, leaks occur from:
- Static collections holding references forever
- Unclosed resources (streams, connections)
- Listeners/callbacks not deregistered
- ThreadLocal not cleared

---

## Exception Handling

### Hierarchy

```
Throwable
├── Error (JVM problems — OutOfMemoryError, StackOverflowError) — don't catch
└── Exception
    ├── Checked (IOException, SQLException) — compile-time
    └── RuntimeException (unchecked)
        ├── NullPointerException
        ├── ArrayIndexOutOfBoundsException
        └── IllegalArgumentException
```

### try-with-resources (Java 7+)

Auto-closes resources implementing `AutoCloseable`.
```java
try (BufferedReader br = new BufferedReader(new FileReader("f.txt"))) {
    return br.readLine();
} // br.close() called automatically, even on exception
```

### Custom Exceptions

```java
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String msg) { super(msg); }
}
throw new InsufficientBalanceException("Balance too low");
```

### Best Practices
- Catch specific exceptions, not `Exception` broadly
- Don't swallow exceptions (empty catch blocks)
- Use custom exceptions for business errors
- Log with context, rethrow if you can't handle

---

## Generics

Type safety at compile time, avoiding casts.
```java
// Without generics — runtime errors possible
List list = new ArrayList();
list.add("hello");
Integer i = (Integer) list.get(0); // ClassCastException at runtime

// With generics — compile-time safety
List<String> list = new ArrayList<>();
list.add("hello");
String s = list.get(0); // no cast needed
```

### Bounded types & wildcards
```java
// Upper bound — T must be Number or subclass
public <T extends Number> double sum(List<T> list) { ... }

// Wildcards
List<? extends Number> nums;   // read-only, any Number subtype
List<? super Integer> ints;    // write Integers, any Integer supertype
```

### Type Erasure
Generics exist only at compile time. At runtime, `List<String>` and `List<Integer>` are both just `List`. The JVM erases type info.

---

## Java Memory Model & `equals()`/`hashCode()` Contract

```java
public class Person {
    private String name;
    private int age;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person p = (Person) o;
        return age == p.age && Objects.equals(name, p.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
```
**Contract rules:**
1. If `a.equals(b)` then `a.hashCode() == b.hashCode()`
2. Consistent — same object returns same hashCode
3. Reflexive, symmetric, transitive

---

## Common Advanced Questions

**Q: What is the difference between process and thread?**
> A process is an independent program with its own memory. A thread is a lightweight unit within a process, sharing the process's memory.

**Q: What is a race condition?**
> Multiple threads access shared data concurrently, and the result depends on timing. Fixed with synchronization or atomic operations.

**Q: What is CAS (Compare-And-Swap)?**
> A lock-free atomic operation. It checks if a value equals expected, and if so, updates it. Used by Atomic classes. Faster than locks because no blocking.

**Q: Fail-fast vs fail-safe iterators?**
> Fail-fast (ArrayList) throws ConcurrentModificationException if modified during iteration. Fail-safe (CopyOnWriteArrayList, ConcurrentHashMap) works on a copy, no exception.

**Q: What is the diamond problem and how does Java handle it?**
> When a class inherits the same default method from two interfaces. Java forces you to override and explicitly choose: `InterfaceA.super.method()`.
