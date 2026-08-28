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
