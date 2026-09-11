# Data Structures & Algorithms — Interview Preparation

A focused reference for coding rounds. Covers complexity, key patterns, and common problems with Java solutions.

## Big-O Complexity (must know)

| Notation | Name | Example |
|----------|------|---------|
| O(1) | Constant | HashMap get, array index |
| O(log n) | Logarithmic | Binary search |
| O(n) | Linear | Loop through a list |
| O(n log n) | Linearithmic | Efficient sorting (merge, quick) |
| O(n²) | Quadratic | Nested loops (bubble sort) |
| O(2^n) | Exponential | Recursive fibonacci (naive) |

**Explanation:** Big-O describes how runtime/space grows as input size `n` grows — it's about scalability, not exact speed. O(1) doesn't change with size (grabbing element #5 from an array). O(log n) halves the problem each step (binary search of a sorted array). O(n) touches each element once. O(n log n) is the best general sorting can do. O(n²) — nested loops — degrades fast and is a red flag for large inputs. Always state the time AND space complexity of your solution.

```java
// O(1) — direct access
int x = array[5];

// O(n) — one pass
for (int v : array) sum += v;

// O(n²) — nested loops (avoid for large n)
for (int i = 0; i < n; i++)
    for (int j = 0; j < n; j++) ...
```

---

## Data Structures Cheat Sheet

| Structure | Access | Search | Insert | Delete | Use when |
|-----------|--------|--------|--------|--------|----------|
| Array | O(1) | O(n) | O(n) | O(n) | Index-based access |
| ArrayList | O(1) | O(n) | O(1)* | O(n) | Dynamic array |
| LinkedList | O(n) | O(n) | O(1) | O(1) | Frequent insert/delete |
| HashMap | - | O(1) | O(1) | O(1) | Key-value lookup |
| TreeMap | - | O(log n) | O(log n) | O(log n) | Sorted keys |
| Stack | O(n) | O(n) | O(1) | O(1) | LIFO |
| Queue | O(n) | O(n) | O(1) | O(1) | FIFO |
| Heap/PQ | O(1) peek | O(n) | O(log n) | O(log n) | Min/max priority |

**Explanation:** Choosing the right structure IS half the interview. Most problems are solved fast by reaching for a **HashMap** (O(1) lookups — turns O(n²) into O(n)), a **HashSet** (fast dedup/membership), a **Stack** (matching brackets, undo), a **Queue** (BFS, scheduling), or a **PriorityQueue/Heap** (top-K, min/max). When asked to optimize a brute-force solution, the first question is usually "can a HashMap remove the inner loop?"

---

## Key Patterns (recognize these — most problems fit a pattern)

### 1. Two Pointers
Two indices moving through data, often on sorted arrays.
```java
// Find if two numbers sum to target (sorted array)
int left = 0, right = arr.length - 1;
while (left < right) {
    int sum = arr[left] + arr[right];
    if (sum == target) return true;
    else if (sum < target) left++;   // need bigger → move left up
    else right--;                     // need smaller → move right down
}
```

### 2. Sliding Window
A window that grows/shrinks over a sequence — for subarrays/substrings.
```java
// Longest substring without repeating characters
int longestUnique(String s) {
    Set<Character> window = new HashSet<>();
    int left = 0, max = 0;
    for (int right = 0; right < s.length(); right++) {
        while (window.contains(s.charAt(right))) {
            window.remove(s.charAt(left++));  // shrink from left
        }
        window.add(s.charAt(right));
        max = Math.max(max, right - left + 1);
    }
    return max;
}
```

### 3. HashMap for O(1) lookup
```java
// Two Sum — the classic
int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int need = target - nums[i];
        if (seen.containsKey(need)) return new int[]{seen.get(need), i};
        seen.put(nums[i], i);
    }
    return new int[]{};
}
```

### 4. Fast & Slow Pointers (cycle detection)
```java
// Detect a cycle in a linked list
boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;         // moves 1
        fast = fast.next.next;    // moves 2
        if (slow == fast) return true;  // they meet → cycle
    }
    return false;
}
```

### 5. BFS / DFS (trees & graphs)
```java
// BFS — level by level (uses a Queue)
void bfs(TreeNode root) {
    Queue<TreeNode> queue = new LinkedList<>();
    queue.add(root);
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        process(node);
        if (node.left != null) queue.add(node.left);
        if (node.right != null) queue.add(node.right);
    }
}

// DFS — recursion (or a Stack)
void dfs(TreeNode node) {
    if (node == null) return;
    process(node);
    dfs(node.left);
    dfs(node.right);
}
```
> **BFS vs DFS:** BFS explores level by level (shortest path in unweighted graphs, uses a queue). DFS goes deep first (path finding, uses recursion/stack).

---

## Common Coding Problems (know the approach)

| Problem | Pattern | Complexity |
|---------|---------|------------|
| Two Sum | HashMap | O(n) |
| Reverse a string/list | Two pointers | O(n) |
| Valid parentheses | Stack | O(n) |
| Merge sorted lists | Two pointers | O(n) |
| Find duplicates | HashSet | O(n) |
| Longest substring no repeat | Sliding window | O(n) |
| Binary search | Divide & conquer | O(log n) |
| Level-order traversal | BFS (queue) | O(n) |
| Detect cycle | Fast/slow pointers | O(n) |
| Top K elements | Heap/PriorityQueue | O(n log k) |
| Fibonacci / climbing stairs | Dynamic programming | O(n) |

**Explanation:** Interviewers rarely want an obscure algorithm — they want to see you recognize the pattern and reason clearly. When you get a problem: (1) restate it and confirm constraints, (2) give a brute-force answer and its complexity, (3) identify the pattern that optimizes it (usually HashMap, two pointers, sliding window, or a heap), (4) code it cleanly, (5) test with an example and edge cases (empty, single element, duplicates).

---

## Valid Parentheses (Stack example — very common)
```java
boolean isValid(String s) {
    Stack<Character> stack = new Stack<>();
    Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');
    for (char c : s.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') {
            stack.push(c);
        } else {
            if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false;
        }
    }
    return stack.isEmpty();
}
```

## Top K Frequent (Heap example)
```java
int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> count = new HashMap<>();
    for (int n : nums) count.merge(n, 1, Integer::sum);

    // Min-heap of size k
    PriorityQueue<Integer> heap = new PriorityQueue<>(
        (a, b) -> count.get(a) - count.get(b));
    for (int key : count.keySet()) {
        heap.add(key);
        if (heap.size() > k) heap.poll();  // remove least frequent
    }
    // heap now holds the k most frequent
    return heap.stream().mapToInt(Integer::intValue).toArray();
}
```

---

## Dynamic Programming (DP) basics

Break a problem into overlapping subproblems, store results to avoid recomputation.
```java
// Fibonacci with memoization — O(n) instead of O(2^n)
int fib(int n, int[] memo) {
    if (n <= 1) return n;
    if (memo[n] != 0) return memo[n];       // reuse stored result
    return memo[n] = fib(n-1, memo) + fib(n-2, memo);
}

// Climbing stairs (how many ways to climb n steps, 1 or 2 at a time)
int climbStairs(int n) {
    int[] dp = new int[n + 1];
    dp[0] = 1; dp[1] = 1;
    for (int i = 2; i <= n; i++) dp[i] = dp[i-1] + dp[i-2];
    return dp[n];
}
```
> **DP signal:** the problem asks for "number of ways", "min/max", or "can you reach", and choices overlap. Two styles: top-down (memoization) or bottom-up (tabulation).

---

## Sorting Algorithms (know the trade-offs)

| Algorithm | Time | Space | Stable | Notes |
|-----------|------|-------|--------|-------|
| Quick Sort | O(n log n) avg, O(n²) worst | O(log n) | No | Fast in practice |
| Merge Sort | O(n log n) | O(n) | Yes | Stable, good for linked lists |
| Heap Sort | O(n log n) | O(1) | No | In-place |
| Bubble/Insertion | O(n²) | O(1) | Yes | Only tiny inputs |

**Explanation:** In practice you call `Collections.sort()` / `Arrays.sort()` (which use tuned merge/quick sort), but interviewers may ask the trade-offs. Merge sort guarantees O(n log n) and is stable (keeps equal elements' order) but needs extra space. Quick sort is usually fastest and in-place but can degrade to O(n²) on bad pivots. Heap sort is in-place O(n log n) but not stable.

---

## Interview Tips for Coding Rounds

1. **Clarify first** — input types, size, edge cases, can you modify input?
2. **Think out loud** — explain your approach before coding
3. **Start with brute force** — state its complexity, then optimize
4. **Name the pattern** — "this looks like a sliding-window problem"
5. **Code cleanly** — meaningful names, handle edge cases
6. **Test** — walk through an example, check empty/null/single-element cases
7. **State complexity** — always give final time and space Big-O

> Even if the role is architecture-focused, showing structured problem-solving matters. Practice ~20-30 common problems on the patterns above.
