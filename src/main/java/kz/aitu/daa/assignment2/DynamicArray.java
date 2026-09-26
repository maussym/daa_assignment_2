package kz.aitu.daa.assignment2;

public class DynamicArray implements IntList {
    private int[] data = new int[4];
    private int size;
    private long work;

    @Override
    public void add(int value) {
        add(size, value);
    }

    @Override
    public void add(int index, int value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(index);
        }
        if (size == data.length) {
            int[] bigger = new int[data.length * 2];
            for (int i = 0; i < size; i++) {
                bigger[i] = data[i];
                work++; // One copied element.
            }
            data = bigger;
        }
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            work++; // One shifted element.
        }
        data[index] = value;
        size++;
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        int result = data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            work++; // One shifted element.
        }
        size--;
        return result;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        work++; // One array access.
        return data[index];
    }

    @Override
    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            work++; // One value comparison.
            if (data[i] == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public long work() {
        return work;
    }

    @Override
    public void resetWork() {
        work = 0;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
    }
}

