package kz.aitu.daa.assignment2;

import java.util.NoSuchElementException;

public class MinHeap {
    private int[] data = new int[4];
    private int size;
    private long comparisons;

    public void insert(int value) {
        if (size == data.length) {
            int[] bigger = new int[data.length * 2];
            System.arraycopy(data, 0, bigger, 0, size);
            data = bigger;
        }
        int i = size++;
        while (i > 0) {
            int parent = (i - 1) / 2;
            comparisons++;
            if (data[parent] <= value) {
                break;
            }
            data[i] = data[parent];
            i = parent;
        }
        data[i] = value;
    }

    public int peekMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return data[0];
    }

    public int extractMin() {
        int result = peekMin();
        int last = data[--size];
        if (size == 0) {
            return result;
        }
        int i = 0;
        while (2 * i + 1 < size) {
            int child = 2 * i + 1;
            if (child + 1 < size) {
                comparisons++;
                if (data[child + 1] < data[child]) {
                    child++;
                }
            }
            comparisons++;
            if (last <= data[child]) {
                break;
            }
            data[i] = data[child];
            i = child;
        }
        data[i] = last;
        return result;
    }

    public int size() {
        return size;
    }

    public long comparisons() {
        return comparisons;
    }

    public void resetComparisons() {
        comparisons = 0;
    }

    public boolean isValidHeap() {
        for (int i = 1; i < size; i++) {
            if (data[(i - 1) / 2] > data[i]) {
                return false;
            }
        }
        return true;
    }
}
