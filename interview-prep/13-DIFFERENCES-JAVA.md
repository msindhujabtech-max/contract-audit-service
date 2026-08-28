# Differences (X vs Y) — Java

Every common "what is the difference between X and Y" for Java. Interviewers love these.

## JDK vs JRE vs JVM
| JDK | JRE | JVM |
|-----|-----|-----|
| Development Kit (compiler + tools + JRE) | Runtime Environment (JVM + libraries) | Virtual Machine that runs bytecode |
| To develop + run | To run only | To execute bytecode |
| Includes javac | No compiler | Interprets/JITs bytecode |

## == vs equals()
| == | equals() |
|----|----------|
| Compares references (memory address) | Compares content/value |
| Primitive: compares value | Object: default is ==, override for content |
| Cannot override | Can override |

## String vs StringBuilder vs StringBuffer
| String | StringBuilder | StringBuffer |
|--------|---------------|--------------|
| Immutable | Mutable | Mutable |
| Thread-safe (immutable) | Not thread-safe | Thread-safe (synchronized) |
| Slow for concatenation | Fast, single-thread | Slower than Builder |

## Abstract Class vs Interface
| Abstract Class | Interface |
|----------------|-----------|
| Constructors allowed | No constructors |
| Instance fields (state) | Only constants |
| Single inheritance | Multiple inheritance |
| Concrete + abstract methods | Abstract + default/static (Java 8+) |
| "IS-A", shared code | Contract/capability |

## Overloading vs Overriding
| Overloading | Overriding |
|-------------|------------|
| Same name, different parameters | Same signature in subclass |
| Compile-time (static) polymorphism | Runtime (dynamic) polymorphism |
| Same class or subclass | Parent-child classes |
| Return type can differ | Return type same/covariant |

## Checked vs Unchecked Exceptions
| Checked | Unchecked |
|---------|-----------|
| Compile-time enforced | Runtime |
| Must handle or declare | Optional handling |
| IOException, SQLException | NullPointerException, RuntimeException |
| Recoverable conditions | Programming errors |

## Error vs Exception
| Error | Exception |
|-------|-----------|
| Serious JVM problems | Application-level issues |
| Not meant to be caught | Can be handled |
| OutOfMemoryError, StackOverflowError | IOException, NPE |

## final vs finally vs finalize
| final | finally | finalize |
|-------|---------|----------|
| Keyword — constant/no-override | Block after try-catch | Method before GC (deprecated) |
| Compile-time | Always executes | Called by GC |

## ArrayList vs LinkedList
| ArrayList | LinkedList |
|-----------|------------|
| Dynamic array | Doubly-linked list |
| O(1) random access | O(n) random access |
| O(n) insert/delete middle | O(1) insert/delete (node known) |
| Less memory | More (node pointers) |

## ArrayList vs Vector
| ArrayList | Vector |
|-----------|--------|
| Not synchronized | Synchronized |
| Faster | Slower |
| Grows 50% | Grows 100% |
| Modern | Legacy |

## HashMap vs HashSet
| HashMap | HashSet |
|---------|---------|
| Key-value pairs | Unique values only |
| implements Map | implements Set |
| Two objects per entry | One object (backed by HashMap) |

## HashMap vs ConcurrentHashMap vs Hashtable
| HashMap | ConcurrentHashMap | Hashtable |
|---------|-------------------|-----------|
| Not thread-safe | Thread-safe (bucket-level lock) | Thread-safe (whole map lock) |
| 1 null key allowed | No null | No null |
| Fastest | Fast concurrent | Slow (legacy) |

## HashMap vs TreeMap vs LinkedHashMap
| HashMap | TreeMap | LinkedHashMap |
|---------|---------|---------------|
| Unordered | Sorted by key | Insertion order |
| O(1) | O(log n) | O(1) |
| No ordering guarantee | Red-black tree | Doubly-linked list |

## HashSet vs TreeSet vs LinkedHashSet
| HashSet | TreeSet | LinkedHashSet |
|---------|---------|---------------|
| Unordered, O(1) | Sorted, O(log n) | Insertion order |

## Comparable vs Comparator
| Comparable | Comparator |
|------------|------------|
| `compareTo()` | `compare()` |
| Natural ordering, one way | Multiple custom orderings |
| Implemented by the class | Separate class/lambda |
| `Collections.sort(list)` | `Collections.sort(list, comparator)` |

## Iterator vs ListIterator
| Iterator | ListIterator |
|----------|--------------|
| Forward only | Both directions |
| All collections | List only |
| remove() | add(), set(), remove() |

## Fail-fast vs Fail-safe iterators
| Fail-fast | Fail-safe |
|-----------|-----------|
| Throws ConcurrentModificationException | No exception |
| Works on original | Works on a copy |
| ArrayList, HashMap | CopyOnWriteArrayList, ConcurrentHashMap |

## synchronized vs volatile vs Atomic
| synchronized | volatile | Atomic |
|--------------|----------|--------|
| Visibility + atomicity | Visibility only | Visibility + atomicity |
| Blocks (lock) | No lock | Lock-free (CAS) |
| Critical sections | Flags | Counters |

## process vs thread
| Process | Thread |
|---------|--------|
| Independent program | Unit within a process |
| Own memory space | Shares process memory |
| Heavy | Lightweight |
| IPC to communicate | Shared variables |

## Thread vs Runnable
| Thread (extend) | Runnable (implement) |
|-----------------|----------------------|
| Uses single inheritance slot | Can extend another class |
| Tightly coupled | Preferred, flexible |

## Platform Thread vs Virtual Thread (Java 21)
| Platform Thread | Virtual Thread |
|-----------------|----------------|
| 1:1 with OS thread | Many:1, JVM-managed |
| Expensive (~1MB) | Cheap (~KB) |
| Thousands max | Millions possible |
| Blocking wastes OS thread | Blocking doesn't waste OS thread |

## wait() vs sleep()
| wait() | sleep() |
|--------|---------|
| Object method | Thread method |
| Releases lock | Holds lock |
| Needs synchronized block | Anywhere |
| Woken by notify() | Woken by timeout |

## Callable vs Runnable
| Runnable | Callable |
|----------|----------|
| run(), returns void | call(), returns value |
| Can't throw checked exception | Can throw |
| No result | Returns Future |

## Stream vs Collection
| Collection | Stream |
|------------|--------|
| Stores data | Processes data |
| Eager | Lazy |
| Can modify | Read-only (functional) |
| Reusable | Consumed once |

## map() vs flatMap()
| map() | flatMap() |
|-------|-----------|
| 1-to-1 transform | 1-to-many, flattens |
| `Stream<Stream<T>>` possible | Flattens to `Stream<T>` |
| `list.stream().map(x -> x*2)` | `list.stream().flatMap(List::stream)` |

## Optional.of() vs ofNullable() vs empty()
| of() | ofNullable() | empty() |
|------|--------------|---------|
| Non-null value | Nullable value | Empty |
| NPE if null | No NPE | Always empty |

## intermediate vs terminal stream operations
| Intermediate | Terminal |
|--------------|----------|
| Returns Stream (lazy) | Returns result/void (triggers) |
| filter, map, sorted | collect, forEach, reduce, count |

## Java 8 vs Java 17 vs Java 21 (quick)
| Java 8 | Java 17 | Java 21 |
|--------|---------|---------|
| Lambdas, Streams, Optional | Records, sealed, pattern matching | Virtual threads, record patterns |
| 2014 LTS | 2021 LTS | 2023 LTS |
