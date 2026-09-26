# Assignment 2: Data Structures and Performance

**Name:** Maussymzhan Makazhan  
**Group:** SE-2527

## 1. Overview

I implemented a dynamic array, a singly linked list, and a min-heap for integers. I compare their correctness, complexity, and speed. The tests use Java collections to check my results, but the three structures are my own implementations.

### Run the project

Use JDK 17 or newer and Maven. Python with Matplotlib is needed only to remake the plots.

```bash
mvn clean test
mvn exec:java
python plot_results.py
```

Run the commands from this folder. The benchmark saves [results.csv](results/tables/results.csv). These results were measured on Windows with Java 25.

## 2. Complexity Analysis

Here `n` is the number of stored elements. `O` is an upper bound, `Ω` is a lower bound, and `Θ` is a tight bound (both upper and lower). Auxiliary space means extra space for one operation, not the space used to store the whole structure.

| Structure | Operation | Best | Average | Worst | Auxiliary space |
| --- | --- | --- | --- | --- | --- |
| Dynamic array | `add(x)` | `Θ(1)` | amortized `Θ(1)` | `Θ(n)` on resize | `O(n)` on resize, else `Θ(1)` |
| Dynamic array | `add(index,x)` | `Θ(1)` at end | `Θ(n)` | `Θ(n)` | `O(n)` on resize, else `Θ(1)` |
| Dynamic array | `remove(index)` | `Θ(1)` at end | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| Dynamic array | `get(index)` | `Θ(1)` | `Θ(1)` | `Θ(1)` | `Θ(1)` |
| Dynamic array | `contains(x)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| Linked list | `add(x)` | `Θ(1)` | `Θ(1)` | `Θ(1)` | `Θ(1)` |
| Linked list | `add(index,x)` | `Θ(1)` at head or tail | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| Linked list | `remove(index)` | `Θ(1)` at head | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| Linked list | `get(index)` | `Θ(1)` at head | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| Linked list | `contains(x)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| Min-heap | `insert(x)` | `Θ(1)` | amortized `O(log n)` | `Θ(n)` if array resizes, otherwise `Θ(log n)` | `O(n)` on resize, else `Θ(1)` |
| Min-heap | `peekMin()` | `Θ(1)` | `Θ(1)` | `Θ(1)` | `Θ(1)` |
| Min-heap | `extractMin()` | `Θ(1)` | `O(log n)` | `Θ(log n)` | `Θ(1)` |

The average indexed cases assume random positions. An array reads an index directly but shifts elements for front or middle changes. A linked list follows nodes to reach an index, but changing its head is fast. Its tail pointer makes `add(x)` constant time. Both structures must inspect every value for an unsuccessful search, so that case is `Θ(n)` and also `Ω(n)`.

The array and heap double their capacity when full. Copying makes one resize `Θ(n)`, but repeated appends have amortized `Θ(1)` cost. In the heap, the minimum is at the root. `peekMin()` is `Θ(1)`, while moving a value up or down takes at most `O(log n)` steps.

## 3. Correctness: Two Loop Invariants

### 3.1 Dynamic array `add(index, x)`

This loop makes room for the new value by shifting elements right. `oldSize` is the size before insertion.

1. **Invariant:** Before iteration `i`, old elements from `i` to `oldSize - 1` are already one place to the right. Elements before `i` have not moved.
2. **Initialization:** At `i = oldSize`, nothing has moved yet. The invariant is true.
3. **Maintenance:** `data[i] = data[i - 1]` shifts one more element. Moving from right to left does not overwrite an element that is still needed.
4. **Termination:** At `i = index`, all elements from `index` onward have shifted right.
5. **Correctness:** Writing `x` at `index` gives the old sequence with `x` inserted. If the array was full, resizing copied the old values before the loop.

### 3.2 Min-heap `insert(x)`

This loop finds the right place for `x` by moving larger parents down.

1. **Invariant:** The old heap order is still correct except possibly between `x` and the parent of its current position.
2. **Initialization:** The new position is at the end, so the old heap has not changed.
3. **Maintenance:** If a parent is larger than `x`, move it down. The lower part stays ordered; now only the next parent above might be too large.
4. **Termination:** Stop at the root or when the parent is no larger than `x`.
5. **Correctness:** Put `x` in the open position. Its parent and the elements below it are in order, so the whole heap is valid.

The JUnit tests cover empty structures, one value, duplicates, invalid indices, and large inputs. They compare results with Java's `ArrayList`, `LinkedList`, and `PriorityQueue`. Heap tests also check its order after inserts and removals.

## 4. Experimental Setup

I tested `n = 100, 1,000, 10,000, 100,000`. Here `n` is the starting size and `m` is the number of operations. Each test runs five times; the table shows the average. I used `Random(42)` and `System.nanoTime()`. Data generation, setup, and printing are outside the timer. Search values are negative while stored values are nonnegative, so each search checks the whole structure.

| Workload | Operations per run | Recorded work count |
| --- | --- | --- |
| Random access | 10,000 random `get(index)` calls | Array reads or linked nodes visited. |
| Search | 1,000 `contains(value)` calls | Value comparisons. |
| Insertion and removal | 1,000 calls at index `0`, then separately at original `n / 2` | Array elements shifted or copied; linked nodes visited plus one structural touch per update. |
| Priority processing | `n` inserts into an empty heap, then `n` extractions | Comparisons between heap values. |

Insertion starts with `n` values. Removal starts again from the original `n` values. For `n = 100`, 1,000 removals need several batches because the structure would become empty. Rebuilding between batches is not timed. The middle index is always the original `n / 2`.

The work count means different things in different workloads, so compare it only within one workload. For the list, one add or remove counts as one structural touch, not every pointer assignment. Heap counts include value comparisons, not resizing copies. The benchmark checks that heap output is sorted. Very short times can change between runs because of JVM warm-up, JIT, garbage collection, and caching.

## 5. Results

Times are averages in milliseconds. All values are also in [results.csv](results/tables/results.csv). `A` means dynamic array and `L` means linked list.

### Random access: 10,000 gets

| n | A time | A accesses | L time | L node accesses | Theory for A / L |
| ---: | ---: | ---: | ---: | ---: | --- |
| 100 | 0.534 | 10,000 | 1.506 | 511,327 | `O(m)` / `O(mn)` |
| 1,000 | 0.079 | 10,000 | 11.679 | 5,021,262 | `O(m)` / `O(mn)` |
| 10,000 | 0.163 | 10,000 | 146.749 | 50,188,951 | `O(m)` / `O(mn)` |
| 100,000 | 0.060 | 10,000 | 1,587.339 | 504,940,938 | `O(m)` / `O(mn)` |

### Search: 1,000 absent values

| n | A time | A comparisons | L time | L comparisons | Theory for both |
| ---: | ---: | ---: | ---: | ---: | --- |
| 100 | 0.450 | 100,000 | 0.476 | 100,000 | `Θ(mn)` |
| 1,000 | 1.723 | 1,000,000 | 3.350 | 1,000,000 | `Θ(mn)` |
| 10,000 | 5.457 | 10,000,000 | 32.991 | 10,000,000 | `Θ(mn)` |
| 100,000 | 37.475 | 100,000,000 | 469.844 | 100,000,000 | `Θ(mn)` |

### Insertion and removal: 1,000 operations each

Array work counts shifts and copies. List work counts node visits and structural touches. Front is index `0`; middle is the original `n / 2`.

| Operation | n | A time | A work | L time | L work | Theory for A / L |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| Insert front | 100 | 2.245 | 601,420 | 0.087 | 1,000 | `O(mn+m²)` / `O(m)` |
| Insert front | 1,000 | 0.379 | 1,500,524 | 0.056 | 1,000 | `O(mn+m²)` / `O(m)` |
| Insert front | 10,000 | 1.205 | 10,499,500 | 0.015 | 1,000 | `O(mn+m²)` / `O(m)` |
| Insert front | 100,000 | 16.986 | 100,499,500 | 0.026 | 1,000 | `O(mn+m²)` / `O(m)` |
| Remove front | 100 | 0.332 | 49,500 | 0.145 | 1,000 | `O(mn)` / `O(m)` |
| Remove front | 1,000 | 0.176 | 499,500 | 0.032 | 1,000 | `O(mn)` / `O(m)` |
| Remove front | 10,000 | 0.906 | 9,499,500 | 0.029 | 1,000 | `O(mn)` / `O(m)` |
| Remove front | 100,000 | 14.808 | 99,499,500 | 0.011 | 1,000 | `O(mn)` / `O(m)` |
| Insert middle | 100 | 0.192 | 551,420 | 0.268 | 51,000 | `O(mn+m²)` / `O(mn)` |
| Insert middle | 1,000 | 0.396 | 1,000,524 | 1.039 | 501,000 | `O(mn+m²)` / `O(mn)` |
| Insert middle | 10,000 | 0.536 | 5,499,500 | 14.738 | 5,001,000 | `O(mn+m²)` / `O(mn)` |
| Insert middle | 100,000 | 5.743 | 50,499,500 | 144.350 | 50,001,000 | `O(mn+m²)` / `O(mn)` |
| Remove middle | 100 | 0.050 | 24,500 | 0.212 | 51,000 | `O(mn)` / `O(mn)` |
| Remove middle | 1,000 | 0.165 | 249,500 | 1.010 | 501,000 | `O(mn)` / `O(mn)` |
| Remove middle | 10,000 | 0.363 | 4,499,500 | 11.497 | 5,001,000 | `O(mn)` / `O(mn)` |
| Remove middle | 100,000 | 5.316 | 49,499,500 | 157.285 | 50,001,000 | `O(mn)` / `O(mn)` |

### Priority processing: `n` heap inserts, then `n` extractions

| n | Insert time | Insert comparisons | Extract time | Extract comparisons | Theory for both total phases |
| ---: | ---: | ---: | ---: | ---: | --- |
| 100 | 0.050 | 194 | 0.069 | 841 | `O(n log n)` |
| 1,000 | 0.053 | 2,232 | 0.116 | 14,994 | `O(n log n)` |
| 10,000 | 0.384 | 22,593 | 1.068 | 216,736 | `O(n log n)` |
| 100,000 | 3.522 | 227,662 | 20.891 | 2,831,463 | `O(n log n)` |

### Plots

Both axes use logarithmic scales. All results, including removal, are in the tables and CSV.

![Average execution time against initial size](results/plots/time_vs_n.png)

![Average work count against initial size](results/plots/work_vs_n.png)

## 6. Performance and Design Analysis

1. **What happens when `n` grows?** Array access stays near 10,000 reads. List access, search comparisons, and middle-operation work grow with `n`. Heap work also grows because it processes more values.
2. **Do results match theory?** Yes for the work counts: list access and both searches grow with `n`, head list changes stay constant per operation, and heap extraction grows roughly as `n log n`.
3. **What does not match exactly?** Some small timings go down when `n` grows. JVM warm-up and measurement noise matter when an operation takes less than a millisecond. Big-O predicts growth, not exact times.
4. **Why can the same Big-O have different times?** Both searches are `Θ(n)`, but for `n = 100,000` the array took 37.5 ms and the list 469.8 ms. Array values are next to each other in memory; list nodes need pointer traversal.
5. **Why do implementation details matter?** Arrays use cache-friendly memory. Lists allocate nodes. Resizing, JIT, and garbage collection also affect time.
6. **When is a dynamic array useful?** For frequent indexed reads and appends. At `n = 100,000`, 10,000 random reads took 0.060 ms for the array and 1,587 ms for the list.
7. **When is a linked list useful?** For frequent changes at the front. Middle operations are slower because the list must first find the position.
8. **Why use a heap for priorities?** The smallest value is at the root. `peekMin()` is `Θ(1)`, and insert/extract normally follow at most `O(log n)` levels. The test also checked sorted extraction.
9. **How do we choose?** Use an array for indexed access, a list for front changes, and a heap for repeated minimum extraction. The most common operation decides the choice.

## 7. Design Recommendations

There is no single best structure. Choose one based on the operations your program uses most.

## 8. Conclusion

The tests passed. The results show that theory explains the main trends, while real times also depend on memory and the JVM. I learned to check both correctness and performance before choosing a structure.
