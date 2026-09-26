package kz.aitu.daa.assignment2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int REPEATS = 5;
    private static volatile long sink;

    public static void main(String[] args) throws IOException {
        Path output = Path.of("results", "tables", "results.csv");
        Files.createDirectories(output.getParent());
        try (BufferedWriter csv = Files.newBufferedWriter(output)) {
            csv.write("workload,structure,n,operations,average_time_ms,average_work,work_metric,theory_total\n");
            for (int n : SIZES) {
                Random random = new Random(42);
                int[] values = new int[n];
                for (int i = 0; i < n; i++) {
                    values[i] = random.nextInt(1_000_000);
                }
                int[] indices = new int[10_000];
                for (int i = 0; i < indices.length; i++) {
                    indices[i] = random.nextInt(n);
                }
                int[] searches = new int[1_000];
                int[] inserted = new int[1_000];
                for (int i = 0; i < searches.length; i++) {
                    searches[i] = -1 - random.nextInt(1_000_000);
                    inserted[i] = random.nextInt(1_000_000);
                }

                for (boolean linked : new boolean[]{false, true}) {
                    access(csv, linked, n, values, indices);
                    search(csv, linked, n, values, searches);
                    change(csv, linked, n, values, inserted, 0, "front");
                    change(csv, linked, n, values, inserted, n / 2, "middle");
                }
                priority(csv, n, values);
                System.out.println("Completed n = " + n);
            }
        }
        System.out.println("Saved " + output + " (checksum " + sink + ")");
    }

    private static void access(BufferedWriter csv, boolean linked, int n,
                               int[] values, int[] indices) throws IOException {
        long time = 0;
        long work = 0;
        for (int repeat = 0; repeat < REPEATS; repeat++) {
            IntList list = filled(linked, values);
            list.resetWork();
            long sum = 0;
            long start = System.nanoTime();
            for (int index : indices) {
                sum += list.get(index);
            }
            time += System.nanoTime() - start;
            work += list.work();
            sink = sum;
        }
        write(csv, "random_access", linked ? "LinkedList" : "DynamicArray",
                n, indices.length, time, work, "element_accesses",
                linked ? "O(m*n)" : "O(m)");
    }

    private static void search(BufferedWriter csv, boolean linked, int n,
                               int[] values, int[] searches) throws IOException {
        long time = 0;
        long work = 0;
        for (int repeat = 0; repeat < REPEATS; repeat++) {
            IntList list = filled(linked, values);
            list.resetWork();
            int found = 0;
            long start = System.nanoTime();
            for (int value : searches) {
                if (list.contains(value)) {
                    found++;
                }
            }
            time += System.nanoTime() - start;
            work += list.work();
            sink = found;
        }
        write(csv, "search", linked ? "LinkedList" : "DynamicArray",
                n, searches.length, time, work, "value_comparisons", "O(m*n)");
    }

    private static void change(BufferedWriter csv, boolean linked, int n,
                               int[] values, int[] inserted, int index,
                               String position) throws IOException {
        long insertTime = 0;
        long insertWork = 0;
        long removeTime = 0;
        long removeWork = 0;
        for (int repeat = 0; repeat < REPEATS; repeat++) {
            IntList list = filled(linked, values);
            list.resetWork();
            long start = System.nanoTime();
            for (int value : inserted) {
                list.add(index, value);
            }
            insertTime += System.nanoTime() - start;
            insertWork += list.work();

            // Rebuild outside the timer whenever a batch runs out of elements.
            // This keeps 1,000 removals valid even when n is only 100.
            int remaining = 1_000;
            while (remaining > 0) {
                list = filled(linked, values);
                int batch = Math.min(remaining, n - index);
                list.resetWork();
                start = System.nanoTime();
                for (int i = 0; i < batch; i++) {
                    list.remove(index);
                }
                removeTime += System.nanoTime() - start;
                removeWork += list.work();
                remaining -= batch;
            }
        }
        String structure = linked ? "LinkedList" : "DynamicArray";
        String insertExpected = linked
                ? (index == 0 ? "O(m)" : "O(m*n)")
                : "O(m*n+m^2)";
        String removeExpected = linked && index == 0 ? "O(m)" : "O(m*n)";
        write(csv, "insert_" + position, structure, n, 1_000,
                insertTime, insertWork, "moves_or_node_visits", insertExpected);
        write(csv, "remove_" + position, structure, n, 1_000,
                removeTime, removeWork, "moves_or_node_visits", removeExpected);
    }

    private static void priority(BufferedWriter csv, int n, int[] values) throws IOException {
        long insertTime = 0;
        long insertComparisons = 0;
        long extractTime = 0;
        long extractComparisons = 0;
        for (int repeat = 0; repeat < REPEATS; repeat++) {
            MinHeap heap = new MinHeap();
            long start = System.nanoTime();
            for (int value : values) {
                heap.insert(value);
            }
            insertTime += System.nanoTime() - start;
            insertComparisons += heap.comparisons();

            int[] extracted = new int[n];
            heap.resetComparisons();
            start = System.nanoTime();
            for (int i = 0; i < n; i++) {
                extracted[i] = heap.extractMin();
            }
            extractTime += System.nanoTime() - start;
            extractComparisons += heap.comparisons();
            for (int i = 1; i < n; i++) {
                if (extracted[i - 1] > extracted[i]) {
                    throw new IllegalStateException("Heap output is not sorted");
                }
            }
            sink = extracted[n - 1];
        }
        write(csv, "heap_insert", "MinHeap", n, n, insertTime,
                insertComparisons, "element_comparisons", "O(n*log(n))");
        write(csv, "heap_extract", "MinHeap", n, n, extractTime,
                extractComparisons, "element_comparisons", "O(n*log(n))");
    }

    private static IntList filled(boolean linked, int[] values) {
        IntList list = linked ? new SimpleLinkedList() : new DynamicArray();
        for (int value : values) {
            list.add(value);
        }
        return list;
    }

    private static void write(BufferedWriter csv, String workload, String structure,
                              int n, int operations, long time, long work,
                              String metric, String expected) throws IOException {
        csv.write(String.format(Locale.US, "%s,%s,%d,%d,%.6f,%.1f,%s,%s%n",
                workload, structure, n, operations,
                time / (REPEATS * 1_000_000.0), work / (double) REPEATS,
                metric, expected));
    }
}
