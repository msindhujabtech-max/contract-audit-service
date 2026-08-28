# Differences (X vs Y) — Java

Every common "what is the difference between X and Y" for Java. Each has a quick table for revision, followed by a plain-English explanation you can read if the table alone isn't enough.

## JDK vs JRE vs JVM
| JDK | JRE | JVM |
|-----|-----|-----|
| Development Kit (compiler + tools + JRE) | Runtime Environment (JVM + libraries) | Virtual Machine that runs bytecode |
| To develop + run | To run only | To execute bytecode |
| Includes javac | No compiler | Interprets/JITs bytecode |

**Explanation:** Think of cooking. The **JVM** is the oven that actually runs your bytecode. The **JRE** is the oven plus basic ingredients and utensils (JVM + core libraries) — enough to RUN a Java app. The **JDK** is the whole kitchen — oven, ingredients, AND recipe-creation tools like the `javac` compiler — needed to DEVELOP apps. On a production server you only need the JRE; on a developer machine you need the full JDK. The JVM is platform-specific but runs the same bytecode — that's "write once, run anywhere".

## == vs equals()
| == | equals() |
|----|----------|
| Compares references (memory address) | Compares content/value |
| Primitive: compares value | Object: default is ==, override for content |
| Cannot override | Can override |

**Explanation:** `==` asks "are these the exact same object in memory?". `.equals()` asks "do these have the same content?".
```java
String a = new String("hello");
String b = new String("hello");
a == b;       // false — two different objects in memory
a.equals(b);  // true  — same content
```
Common bug: comparing strings with `==`. It sometimes works because Java caches string literals in a pool, but fails for objects made with `new`. Always use `.equals()` for content. For primitives (`int`), `==` compares the value directly.

## String vs StringBuilder vs StringBuffer
| String | StringBuilder | StringBuffer |
|--------|---------------|--------------|
| Immutable | Mutable | Mutable |
| Thread-safe (immutable) | Not thread-safe | Thread-safe (synchronized) |
| Slow for concatenation | Fast, single-thread | Slower than Builder |

**Explanation:** A `String` is **immutable** — every "change" creates a new object. In a loop this makes thousands of throwaway objects (slow). `StringBuilder` is a **mutable buffer** you keep appending to (fast). `StringBuffer` is the same but thread-safe via synchronization (slower).
```java
// BAD — ~1000 String objects created
String r = "";
for (int i = 0; i < 1000; i++) r += i;

// GOOD — one buffer
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) sb.append(i);
```
Use String for fixed values, StringBuilder for single-thread building (99% of cases), StringBuffer only when multiple threads share one buffer.

## Abstract Class vs Interface
| Abstract Class | Interface |
|----------------|-----------|
| Constructors allowed | No constructors |
| Instance fields (state) | Only constants |
| Single inheritance | Multiple inheritance |
| Concrete + abstract methods | Abstract + default/static (Java 8+) |
| "IS-A", shared code | Contract/capability |

**Explanation:** An **interface** is a pure contract ("any class implementing me must provide these methods"). An **abstract class** is a partial blueprint — it can provide some working methods AND leave others abstract. Interface = a job description (defines capability, "can-do"). Abstract class = a partially built house with some finished rooms (shared code + identity, "is-a"). A class implements MANY interfaces but extends only ONE abstract class. Use interface for capabilities, abstract class for shared code + identity.

## Overloading vs Overriding
| Overloading | Overriding |
|-------------|------------|
| Same name, different parameters | Same signature in subclass |
| Compile-time (static) polymorphism | Runtime (dynamic) polymorphism |
| Same class or subclass | Parent-child classes |
| Return type can differ | Return type same/covariant |

**Explanation:** **Overloading** = same method name, DIFFERENT parameters, same class. Chosen at COMPILE time. **Overriding** = subclass REPLACES parent's method (same name + params). Chosen at RUNTIME by the actual object type — this is polymorphism.
```java
// Overloading — same name, different params
int add(int a, int b) {...}
double add(double a, double b) {...}

// Overriding — subclass replaces parent's method
class Animal { String sound() { return "..."; } }
class Dog extends Animal { String sound() { return "Woof"; } }
Animal a = new Dog();
a.sound(); // "Woof" — runtime picks Dog's version
```

## Checked vs Unchecked Exceptions
| Checked | Unchecked |
|---------|-----------|
| Compile-time enforced | Runtime |
| Must handle or declare | Optional handling |
| IOException, SQLException | NullPointerException, RuntimeException |
| Recoverable conditions | Programming errors |

**Explanation:** **Checked** exceptions are ones the compiler FORCES you to handle (try-catch or `throws`). They represent recoverable external failures (file missing, network down). **Unchecked** (RuntimeException) are NOT enforced — usually programming bugs (null access, bad index).
```java
try { new FileReader("x.txt"); }   // checked — must handle IOException
catch (IOException e) {}

String s = null; s.length();       // unchecked — NPE, a code bug
```
Checked = "plan for expected external failures." Unchecked = "fix your code."

## Error vs Exception
| Error | Exception |
|-------|-----------|
| Serious JVM problems | Application-level issues |
| Not meant to be caught | Can be handled |
| OutOfMemoryError, StackOverflowError | IOException, NPE |

**Explanation:** An **Error** is a severe problem in the JVM itself (out of memory, stack overflow from infinite recursion) — your code usually can't recover, so don't catch them. An **Exception** is an application-level problem you can anticipate and handle (bad input, missing file). Both extend `Throwable`, but you only handle Exceptions.

## final vs finally vs finalize
| final | finally | finalize |
|-------|---------|----------|
| Keyword — constant/no-override | Block after try-catch | Method before GC (deprecated) |
| Compile-time | Always executes | Called by GC |

**Explanation:** Three unrelated things with similar names. **final** is a keyword: a `final` variable is a constant, a `final` method can't be overridden, a `final` class can't be extended. **finally** is a block after try-catch that ALWAYS runs (used to close resources). **finalize()** was a method the garbage collector called before destroying an object — now deprecated and unreliable, don't use it.

## ArrayList vs LinkedList
| ArrayList | LinkedList |
|-----------|------------|
| Dynamic array | Doubly-linked list |
| O(1) random access | O(n) random access |
| O(n) insert/delete middle | O(1) insert/delete (node known) |
| Less memory | More (node pointers) |

**Explanation:** **ArrayList** = numbered lockers in a row. Jumping to locker #500 is instant (O(1)), but inserting in the middle shifts everything after it (O(n)). **LinkedList** = a chain where each node points to the next. Finding the 500th item means following 500 links (O(n)), but inserting between two known nodes just rewires pointers (O(1)). Use ArrayList for read/index-heavy work (most cases); LinkedList for frequent insert/delete at the ends.

## ArrayList vs Vector
| ArrayList | Vector |
|-----------|--------|
| Not synchronized | Synchronized |
| Faster | Slower |
| Grows 50% | Grows 100% |
| Modern | Legacy |

**Explanation:** Both are resizable arrays. **Vector** is the old (Java 1.0) version where every method is synchronized (thread-safe but slow). **ArrayList** (Java 1.2) is not synchronized, so it's faster for single-threaded use. When Vector runs out of space it doubles (100%); ArrayList grows by 50%. Today use ArrayList; if you need thread-safety, wrap it or use `CopyOnWriteArrayList`.

## HashMap vs HashSet
| HashMap | HashSet |
|---------|---------|
| Key-value pairs | Unique values only |
| implements Map | implements Set |
| Two objects per entry | One object (backed by HashMap) |

**Explanation:** A **HashMap** stores key→value pairs (like a dictionary: word → definition). A **HashSet** stores just unique values (like a guest list — no duplicates). Interestingly, HashSet is internally backed by a HashMap where your values become the keys and a dummy constant is the value. Use HashMap when you need to look up a value by a key; HashSet when you only care about membership/uniqueness.

## HashMap vs ConcurrentHashMap vs Hashtable
| HashMap | ConcurrentHashMap | Hashtable |
|---------|-------------------|-----------|
| Not thread-safe | Thread-safe (bucket-level lock) | Thread-safe (whole map lock) |
| 1 null key allowed | No null | No null |
| Fastest | Fast concurrent | Slow (legacy) |

**Explanation:** All store key-value pairs; they differ in thread-safety. **HashMap** — not thread-safe, fastest, single-thread only. **Hashtable** — locks the ENTIRE map per operation (like one key for a whole building, one person at a time) — slow, legacy. **ConcurrentHashMap** — locks only a small bucket (like separate keys per room, many people work at once) — fast and modern. This is why shared mutable state across concurrent requests needs ConcurrentHashMap or CopyOnWriteArrayList (as you used in the audit service).

## HashMap vs TreeMap vs LinkedHashMap
| HashMap | TreeMap | LinkedHashMap |
|---------|---------|---------------|
| Unordered | Sorted by key | Insertion order |
| O(1) | O(log n) | O(1) |
| No ordering guarantee | Red-black tree | Doubly-linked list |

**Explanation:** All are maps; they differ in ORDER. **HashMap** — no guaranteed order, fastest. **TreeMap** — keeps keys sorted (uses a red-black tree), so iteration is in sorted order, but slower (O(log n)). **LinkedHashMap** — remembers the order you inserted entries, fast like HashMap but uses extra memory for a linked list. Use HashMap by default, TreeMap when you need sorted keys, LinkedHashMap when insertion order matters (e.g., an LRU cache).

## HashSet vs TreeSet vs LinkedHashSet
| HashSet | TreeSet | LinkedHashSet |
|---------|---------|---------------|
| Unordered, O(1) | Sorted, O(log n) | Insertion order |

**Explanation:** Same idea as the maps above, but for sets of unique values. **HashSet** — unordered, fastest. **TreeSet** — automatically sorted. **LinkedHashSet** — preserves insertion order. Pick based on whether you need speed (HashSet), sorting (TreeSet), or insertion order (LinkedHashSet).

## Comparable vs Comparator
| Comparable | Comparator |
|------------|------------|
| `compareTo()` | `compare()` |
| Natural ordering, one way | Multiple custom orderings |
| Implemented by the class | Separate class/lambda |
| `Collections.sort(list)` | `Collections.sort(list, comparator)` |

**Explanation:** **Comparable** defines the ONE natural order of a class, written INSIDE the class via `compareTo()` (e.g., numbers sort ascending). **Comparator** defines ANY custom order, written OUTSIDE the class — you can make many (by name, age, salary).
```java
class Employee implements Comparable<Employee> {
    public int compareTo(Employee o) { return this.salary - o.salary; } // natural order
}
Comparator<Employee> byName = Comparator.comparing(e -> e.name);        // custom order
employees.sort(byName);
```
One natural order → Comparable. Multiple/flexible orders → Comparator.

## Iterator vs ListIterator
| Iterator | ListIterator |
|----------|--------------|
| Forward only | Both directions |
| All collections | List only |
| remove() | add(), set(), remove() |

**Explanation:** An **Iterator** walks a collection in one direction (forward) and can only remove elements. A **ListIterator** is a more powerful version for Lists only — it can go both forward and backward, and can add/replace elements during iteration. Use Iterator for simple looping, ListIterator when you need bidirectional traversal or in-place modification of a List.

## Fail-fast vs Fail-safe iterators
| Fail-fast | Fail-safe |
|-----------|-----------|
| Throws ConcurrentModificationException | No exception |
| Works on original | Works on a copy |
| ArrayList, HashMap | CopyOnWriteArrayList, ConcurrentHashMap |

**Explanation:** **Fail-fast** iterators (ArrayList, HashMap) throw a `ConcurrentModificationException` immediately if the collection changes while you loop — they "fail fast" to warn you of a likely bug. **Fail-safe** iterators (CopyOnWriteArrayList, ConcurrentHashMap) iterate over a COPY, so changes during iteration don't error. This is exactly why you used `CopyOnWriteArrayList` — concurrent reads while writing don't blow up.

## synchronized vs volatile vs Atomic
| synchronized | volatile | Atomic |
|--------------|----------|--------|
| Visibility + atomicity | Visibility only | Visibility + atomicity |
| Blocks (lock) | No lock | Lock-free (CAS) |
| Critical sections | Flags | Counters |

**Explanation:** They solve different concurrency problems. **volatile** — VISIBILITY only. It forces reads/writes to go to main memory so all threads see the latest value, but it does NOT make multi-step operations atomic. Good for simple flags. **synchronized** — visibility + atomicity via locking; only one thread runs the block at a time (safe but slower). **Atomic** classes (AtomicInteger) — visibility + atomicity WITHOUT locks, using CAS (Compare-And-Swap hardware instruction); fast for counters.
```java
count++;  // NOT atomic (read, add, write) — two threads can lose an update
// Fix: synchronized method, OR AtomicInteger.incrementAndGet()
private volatile boolean running = true;  // flag — volatile is enough
```

## process vs thread
| Process | Thread |
|---------|--------|
| Independent program | Unit within a process |
| Own memory space | Shares process memory |
| Heavy | Lightweight |
| IPC to communicate | Shared variables |

**Explanation:** A **process** is an independent running program with its own isolated memory (e.g., Chrome and Word are separate processes). A **thread** is a lightweight unit of execution INSIDE a process, and all threads in a process SHARE its memory. Because they share memory, threads communicate easily (shared variables) but risk race conditions; processes are isolated and must use IPC (inter-process communication) to talk.

## Thread vs Runnable
| Thread (extend) | Runnable (implement) |
|-----------------|----------------------|
| Uses single inheritance slot | Can extend another class |
| Tightly coupled | Preferred, flexible |

**Explanation:** Two ways to define a task for a thread. Extending **Thread** uses up your one allowed superclass (Java has single inheritance), so you can't extend anything else. Implementing **Runnable** leaves you free to extend another class and is the preferred, more flexible approach — you pass the Runnable to a Thread or ExecutorService.

## Platform Thread vs Virtual Thread (Java 21)
| Platform Thread | Virtual Thread |
|-----------------|----------------|
| 1:1 with OS thread | Many:1, JVM-managed |
| Expensive (~1MB) | Cheap (~KB) |
| Thousands max | Millions possible |
| Blocking wastes OS thread | Blocking doesn't waste OS thread |

**Explanation:** A **platform thread** maps 1:1 to an OS thread (~1MB each), so you can only have a few thousand. When it blocks on I/O (DB, network), that expensive OS thread sits idle — wasted. A **virtual thread** (Java 21) is managed by the JVM, extremely cheap (~KB), so you can have millions. When it blocks, the JVM parks it and runs another virtual thread on the same OS thread — nothing is wasted.
```java
try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 1_000_000).forEach(i ->
        ex.submit(() -> { Thread.sleep(Duration.ofSeconds(1)); return i; }));
}
```
Big deal: you write simple blocking code that scales like reactive code — great for high-throughput microservices.

## wait() vs sleep()
| wait() | sleep() |
|--------|---------|
| Object method | Thread method |
| Releases lock | Holds lock |
| Needs synchronized block | Anywhere |
| Woken by notify() | Woken by timeout |

**Explanation:** Both pause a thread. **sleep()** pauses for a fixed time and KEEPS any lock it holds (like napping while still holding the office keys — nobody else can enter). **wait()** RELEASES the lock and waits until another thread calls `notify()` (like stepping out and handing over the keys, waiting to be called back). `wait()` must be inside a synchronized block; `sleep()` can go anywhere. Use wait/notify for thread coordination (producer-consumer); sleep for a timed pause.

## Callable vs Runnable
| Runnable | Callable |
|----------|----------|
| run(), returns void | call(), returns value |
| Can't throw checked exception | Can throw |
| No result | Returns Future |

**Explanation:** Both represent a task for a thread. **Runnable**'s `run()` returns nothing and can't throw checked exceptions — fire-and-forget. **Callable**'s `call()` RETURNS a result and can throw checked exceptions; you get the result via a `Future`.
```java
Future<Integer> f = executor.submit(() -> 42);  // Callable
Integer result = f.get();  // 42 (blocks until done)
```
Use Runnable when you don't need a result, Callable when you do.

## Stream vs Collection
| Collection | Stream |
|------------|--------|
| Stores data | Processes data |
| Eager | Lazy |
| Can modify | Read-only (functional) |
| Reusable | Consumed once |

**Explanation:** A **Collection** (List, Set) is a data structure that STORES elements in memory. A **Stream** is a pipeline that PROCESSES elements (filter, map, reduce) — it doesn't store anything. Streams are lazy (nothing runs until a terminal operation like `collect`) and can only be consumed once (like a one-way conveyor belt). Collections hold data; streams flow data through operations.

## map() vs flatMap()
| map() | flatMap() |
|-------|-----------|
| 1-to-1 transform | 1-to-many, flattens |
| `Stream<Stream<T>>` possible | Flattens to `Stream<T>` |
| `list.stream().map(x -> x*2)` | `list.stream().flatMap(List::stream)` |

**Explanation:** **map()** transforms each element 1-to-1 (3 items in → 3 items out). **flatMap()** transforms each element into a stream, then FLATTENS all of them into one — use it when each element expands into many, or when data is nested.
```java
// map: open each box, transform contents, put back in a box
words.stream().map(String::length);   // ["hi","bye"] → [2,3]

// flatMap: open each box, pour all contents into one pile
List.of(List.of(1,2), List.of(3,4)).stream()
    .flatMap(List::stream);   // → [1,2,3,4]
```

## Optional.of() vs ofNullable() vs empty()
| of() | ofNullable() | empty() |
|------|--------------|---------|
| Non-null value | Nullable value | Empty |
| NPE if null | No NPE | Always empty |

**Explanation:** `Optional` is a container to avoid null checks. **of(value)** — use when you're SURE the value isn't null; throws NPE if it is. **ofNullable(value)** — use when the value MIGHT be null; safely wraps it (empty Optional if null). **empty()** — an explicitly empty Optional. Then callers use `.orElse(default)` or `.isPresent()` instead of null checks.
```java
Optional.of("hi");            // ok
Optional.of(null);            // throws NPE
Optional.ofNullable(maybe);   // safe
Optional.empty();             // empty
```

## intermediate vs terminal stream operations
| Intermediate | Terminal |
|--------------|----------|
| Returns Stream (lazy) | Returns result/void (triggers) |
| filter, map, sorted | collect, forEach, reduce, count |

**Explanation:** Stream operations come in two kinds. **Intermediate** operations (filter, map, sorted) return another Stream and are LAZY — they don't actually run yet, they just build up the pipeline. **Terminal** operations (collect, forEach, count, reduce) TRIGGER the whole pipeline to execute and produce a result. Without a terminal operation, nothing happens.
```java
list.stream()
    .filter(x -> x > 5)   // intermediate — lazy
    .map(x -> x * 2)      // intermediate — lazy
    .collect(toList());   // terminal — runs everything now
```

## Java 8 vs Java 17 vs Java 21 (quick)
| Java 8 | Java 17 | Java 21 |
|--------|---------|---------|
| Lambdas, Streams, Optional | Records, sealed, pattern matching | Virtual threads, record patterns |
| 2014 LTS | 2021 LTS | 2023 LTS |

**Explanation:** These are the three most important Long-Term Support (LTS) releases. **Java 8** introduced functional programming (lambdas, streams, Optional) — the biggest shift ever. **Java 17** added records (concise immutable classes), sealed classes, and pattern matching. **Java 21** brought virtual threads (massive concurrency) and record patterns. Being on an LTS matters because they get years of updates; most enterprises run on 8, 17, or 21.
