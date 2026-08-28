# Java Core — Interview Preparation

## Java Version History (What was added, why, and examples)

Interviewers love asking "which Java version are you on?" and "what features do you use?". Here's the complete timeline.

### Java 8 (2014) — The most important release

| Feature | Purpose | Example |
|---------|---------|---------|
| Lambda Expressions | Write functions inline, less boilerplate | `list.forEach(x -> System.out.println(x))` |
| Stream API | Process collections functionally | `list.stream().filter(x -> x > 5).collect(toList())` |
| Functional Interfaces | Interface with one abstract method | `Predicate`, `Function`, `Consumer`, `Supplier` |
| Default Methods | Add methods to interfaces without breaking implementers | `default void log() {...}` |
| Optional | Avoid NullPointerException | `Optional.ofNullable(x).orElse("default")` |
| Method References | Shorthand for lambdas | `list.forEach(System.out::println)` |
| `java.time` API | Better date/time handling | `LocalDate.now()`, `LocalDateTime` |

**Lambda example:**
```java
// Before Java 8
Runnable r = new Runnable() {
    public void run() { System.out.println("Hello"); }
};

// Java 8 lambda
Runnable r = () -> System.out.println("Hello");
```

**Stream example (very common interview question):**
```java
List<String> names = List.of("Anna", "Bob", "Alice", "Charlie");

// Filter names starting with 'A', convert to uppercase, sort
List<String> result = names.stream()
        .filter(n -> n.startsWith("A"))
        .map(String::toUpperCase)
        .sorted()
        .collect(Collectors.toList());
// Result: [ALICE, ANNA]
```

**Functional interfaces cheat sheet:**
```java
Predicate<Integer> isEven = n -> n % 2 == 0;      // takes T, returns boolean
Function<Integer, String> toStr = n -> "N=" + n;  // takes T, returns R
Consumer<String> printer = s -> System.out.println(s); // takes T, returns void
Supplier<String> greet = () -> "Hello";           // takes nothing, returns T
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b; // takes T,U returns R
```

### Java 11 (2018) — First LTS after 8

| Feature | Purpose | Example |
|---------|---------|---------|
| `var` in lambda params | Type inference | `(var x, var y) -> x + y` |
| New String methods | Convenience | `" ".isBlank()`, `"a\nb".lines()`, `"ab".repeat(3)` |
| `HttpClient` (standard) | Built-in HTTP client | Replaces old HttpURLConnection |
| Run single-file programs | No compile step | `java Hello.java` |

**In plain terms:** Java 11 is mostly about small quality-of-life wins rather than a big paradigm shift like Java 8. The one that matters most in real services is the built-in `HttpClient` — before Java 11 you either wrestled with the clunky `HttpURLConnection` or pulled in a third-party library. Now the JDK ships a modern client that supports async calls out of the box, which is handy when a microservice needs to call another service without adding a dependency.

```java
// Java 11 built-in HttpClient — call another microservice with no extra library
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://contract-service/api/contracts/42"))
        .build();
HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(resp.statusCode() + " → " + resp.body());
```

```java
// New String methods
"  ".isBlank();           // true
"hello".repeat(3);        // "hellohellohello"
" trim ".strip();         // "trim" (Unicode-aware, better than trim())
"a\nb\nc".lines().count(); // 3
```

### Java 17 (2021) — Current LTS, widely used

| Feature | Purpose | Example |
|---------|---------|---------|
| Records | Immutable data classes, no boilerplate | `record Point(int x, int y) {}` |
| Sealed Classes | Restrict which classes can extend | `sealed interface Shape permits Circle, Square` |
| Pattern Matching for instanceof | Cleaner type checks | `if (obj instanceof String s) { use s }` |
| Switch Expressions | Switch returns a value | `int x = switch(day) { case MON -> 1; ... }` |
| Text Blocks | Multi-line strings | `"""multi\nline"""` |

**Record example (you used this in your audit service):**
```java
public record AuditRequest(String contractName, String status, int wordCount) {}
// Auto-generates: constructor, getters (contractName()), equals, hashCode, toString
```

**Sealed class example:**
```java
public sealed interface Shape permits Circle, Rectangle {}
public final class Circle implements Shape { double radius; }
public final class Rectangle implements Shape { double width, height; }
// Only Circle and Rectangle can implement Shape — compiler enforces it
```

**Pattern matching + switch:**
```java
static String describe(Object obj) {
    return switch (obj) {
        case Integer i -> "Integer: " + i;
        case String s  -> "String of length " + s.length();
        case null      -> "null value";
        default        -> "Unknown";
    };
}
```

### Java 21 (2023) — Latest LTS (you use this in your projects)

| Feature | Purpose | Example |
|---------|---------|---------|
| Virtual Threads | Lightweight threads for massive concurrency | `Thread.ofVirtual().start(...)` |
| Pattern Matching for switch (final) | Match on types + conditions | `case Integer i when i > 10 -> ...` |
| Record Patterns | Destructure records | `if (obj instanceof Point(int x, int y))` |
| Sequenced Collections | First/last element access | `list.getFirst()`, `list.getLast()` |

**Virtual Threads (huge interview topic for 2024+):**
```java
// Old: platform threads are expensive (1MB each), limited to ~thousands
// New: virtual threads are cheap, can run millions

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 1_000_000).forEach(i -> {
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return i;
        });
    });
}
// A million concurrent tasks without running out of memory
```
> **Why it matters:** Traditional threads map 1:1 to OS threads. Virtual threads are managed by the JVM, so blocking I/O (DB calls, HTTP) doesn't waste an OS thread. Great for high-throughput microservices.

---

## Object-Oriented Programming (OOP) — 4 Pillars

Interviewers always ask these. Have crisp definitions + examples ready.

### 1. Encapsulation
Bundling data and methods, hiding internal state via private fields + public getters/setters.
```java
public class Account {
    private double balance;  // hidden
    public double getBalance() { return balance; }
    public void deposit(double amt) {
        if (amt > 0) balance += amt;  // controlled access
    }
}
```

### 2. Inheritance
A class acquires properties of another (IS-A relationship).
```java
class Animal { void eat() {} }
class Dog extends Animal { void bark() {} }
// Dog IS-A Animal
```

### 3. Polymorphism
Same method, different behavior. Two types:
- **Compile-time (overloading):** same method name, different parameters
- **Runtime (overriding):** subclass changes parent's method

```java
// Overloading (compile-time)
int add(int a, int b) { return a + b; }
double add(double a, double b) { return a + b; }

// Overriding (runtime)
class Animal { String sound() { return "..."; } }
class Cat extends Animal { String sound() { return "Meow"; } }
Animal a = new Cat();
a.sound(); // "Meow" — resolved at runtime
```

### 4. Abstraction
Hiding implementation, showing only essential features. Via abstract classes and interfaces.
```java
abstract class Shape {
    abstract double area();  // no implementation
}
class Circle extends Shape {
    double r;
    double area() { return Math.PI * r * r; }
}
```

### Abstract Class vs Interface (very common question)

| Abstract Class | Interface |
|----------------|-----------|
| Can have constructors | No constructors |
| Can have state (fields) | Only constants (public static final) |
| Single inheritance | Multiple inheritance |
| Can have concrete + abstract methods | All abstract (until Java 8 default methods) |
| Use when classes share common code | Use to define a contract/capability |

**In plain terms:** Think of an abstract class as a partially built house — it comes with some rooms already finished (concrete methods) and a shared foundation (state), but a class can only inherit from one. An interface is more like a checklist of capabilities ("must be able to fly", "must be able to swim") that any class can promise to fulfill, and a class can sign up for many of them. Reach for an abstract class when your subclasses share real code; reach for an interface when you just need to define what something can do.

---

## Java Collections Framework

### Hierarchy

```
Collection
├── List (ordered, allows duplicates)
│   ├── ArrayList (dynamic array, fast random access)
│   ├── LinkedList (doubly-linked, fast insert/delete)
│   └── Vector (synchronized, legacy)
├── Set (no duplicates)
│   ├── HashSet (unordered, O(1))
│   ├── LinkedHashSet (insertion order)
│   └── TreeSet (sorted, O(log n))
└── Queue (FIFO)
    ├── PriorityQueue (heap-based)
    └── Deque / ArrayDeque (double-ended)

Map (key-value, not a Collection)
├── HashMap (unordered, O(1))
├── LinkedHashMap (insertion order)
├── TreeMap (sorted by key)
└── ConcurrentHashMap (thread-safe)
```

**In simple terms:** Pick the container by what you need: a `List` when order and duplicates matter (like a shopping list), a `Set` when you only want unique items (like a guest list with no repeats), a `Queue` when you process items in arrival order (like a checkout line), and a `Map` when you look things up by a key (like a phone book: name → number).

```java
List<String> tasks = new ArrayList<>();          // ordered, duplicates OK
Set<String> uniqueTags = new HashSet<>();        // no duplicates
Queue<String> jobs = new ArrayDeque<>();         // FIFO processing
Map<String, Integer> ages = new HashMap<>();     // key → value lookup
ages.put("Bob", 30);                             // ages.get("Bob") → 30
```

### ArrayList vs LinkedList

| | ArrayList | LinkedList |
|--|-----------|------------|
| Backing | Dynamic array | Doubly-linked nodes |
| Random access `get(i)` | O(1) fast | O(n) slow |
| Insert/delete middle | O(n) slow | O(1) fast (if node known) |
| Memory | Less | More (stores pointers) |
| Use when | Frequent reads | Frequent inserts/deletes |

**In plain terms:** An ArrayList is like a numbered row of lockers — jumping straight to locker #500 is instant, but squeezing a new locker into the middle means shifting everything after it. A LinkedList is like a treasure hunt where each item holds a note pointing to the next — inserting or removing is quick once you're standing at the right spot, but finding item #500 means walking through 499 notes first. In practice, ArrayList wins for most real-world code because reads dominate.

### HashMap internals (top interview question)

- Stores key-value pairs in an array of "buckets"
- `hashCode()` determines the bucket, `equals()` resolves collisions
- Since Java 8: buckets with >8 entries convert from linked list to **balanced tree (red-black)** for O(log n) instead of O(n)
- Default capacity 16, load factor 0.75 (resizes when 75% full)

```java
Map<String, Integer> map = new HashMap<>();
map.put("a", 1);           // hashCode("a") → bucket index
map.getOrDefault("b", 0);  // 0 if not present
map.computeIfAbsent("c", k -> 10); // compute if missing
map.merge("a", 5, Integer::sum);   // a → 6
```

### HashMap vs ConcurrentHashMap vs Hashtable

| | HashMap | ConcurrentHashMap | Hashtable |
|--|---------|-------------------|-----------|
| Thread-safe | No | Yes (segment/bucket locking) | Yes (whole map lock) |
| Null keys | 1 allowed | Not allowed | Not allowed |
| Performance | Fastest | Fast (concurrent) | Slow (fully synchronized) |
| Use | Single-thread | Multi-thread | Legacy, avoid |

**In plain terms:** Imagine a shared whiteboard. Hashtable puts one lock on the whole board, so only one person can write at a time and everyone else waits — safe but slow. ConcurrentHashMap divides the board into sections so several people can write to different sections at once — safe and fast. HashMap has no lock at all, which is perfect when only one thread ever touches it but corrupts data if shared. Use HashMap alone, ConcurrentHashMap when threads share it, and leave Hashtable in the past.

### CopyOnWriteArrayList (you used this)

Thread-safe list where every write creates a new copy of the array. Great for read-heavy, write-rare scenarios.
```java
List<String> list = new CopyOnWriteArrayList<>();
// Reads never block; writes copy the whole array
```

---

## String Handling

### String vs StringBuilder vs StringBuffer

| | String | StringBuilder | StringBuffer |
|--|--------|---------------|--------------|
| Mutable | No (immutable) | Yes | Yes |
| Thread-safe | Yes (immutable) | No | Yes (synchronized) |
| Performance | Slow for concat | Fast | Slower than Builder |

**In plain terms:** A `String` is like writing in permanent ink — every time you "change" it you actually throw the old page away and copy everything onto a fresh one, which is wasteful in a loop. `StringBuilder` is a pencil-and-eraser notepad: you keep editing the same page, so it's fast, but only one person (thread) should hold it. `StringBuffer` is the same notepad with a lock on it, so two threads can't scribble at once — safer but slower. Rule of thumb: use `String` for fixed text, `StringBuilder` when you build text in a single thread (the common case, e.g. assembling an audit report), and `StringBuffer` only when multiple threads append to the same buffer.

```java
// Concatenating in a String loop makes 1000 throwaway String objects
String csv = "";
for (String tag : List.of("HIGH", "LEGAL", "REVIEW")) csv += tag + ",";

// StringBuilder reuses one buffer — the go-to for building an audit summary
StringBuilder sb = new StringBuilder();
for (String tag : List.of("HIGH", "LEGAL", "REVIEW")) sb.append(tag).append(",");
String flags = sb.toString(); // "HIGH,LEGAL,REVIEW,"
```

```java
// Bad — creates many objects in a loop
String s = "";
for (int i = 0; i < 1000; i++) s += i;

// Good — one mutable buffer
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) sb.append(i);
String result = sb.toString();
```

### Why is String immutable?
1. **Security** — used in class loading, network connections, file paths
2. **Thread-safety** — can be shared without synchronization
3. **String pool** — JVM caches literals for reuse (memory efficient)
4. **hashCode caching** — safe to cache since value never changes

---

## Common Java Interview Questions (Quick Answers)

**Q: `==` vs `.equals()`?**
> `==` compares references (memory address). `.equals()` compares content. For objects, override `equals()` and `hashCode()` together.

**Q: Why override hashCode with equals?**
> Contract: equal objects must have equal hash codes. Otherwise HashMap/HashSet break — you'd store a key but never find it.

**Q: `final`, `finally`, `finalize`?**
> `final` = constant/no-override/no-inherit. `finally` = block that always runs after try-catch. `finalize()` = deprecated GC method before object destruction.

**Q: Checked vs Unchecked exceptions?**
> Checked (IOException) — must handle/declare, compile-time. Unchecked (NullPointerException, RuntimeException) — optional handling, runtime.

**Q: `static` keyword?**
> Belongs to the class, not instances. One copy shared across all objects. Static methods can't access instance variables.
