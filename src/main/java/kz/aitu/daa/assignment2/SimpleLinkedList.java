package kz.aitu.daa.assignment2;

public class SimpleLinkedList implements IntList {
    private static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private long work;

    @Override
    public void add(int value) {
        Node node = new Node(value);
        if (size == 0) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
        work++; // One structural touch; individual assignments are not counted.
    }

    @Override
    public void add(int index, int value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(index);
        }
        if (index == size) {
            add(value);
            return;
        }
        Node node = new Node(value);
        if (index == 0) {
            node.next = head;
            head = node;
        } else {
            Node previous = nodeAt(index - 1);
            node.next = previous.next;
            previous.next = node;
        }
        size++;
        work++; // One structural touch.
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        Node removed;
        Node previous = null;
        if (index == 0) {
            removed = head;
            head = head.next;
        } else {
            previous = nodeAt(index - 1);
            removed = previous.next;
            previous.next = removed.next;
        }
        size--;
        if (size == 0) {
            tail = null;
        } else if (removed == tail) {
            tail = previous;
        }
        work++; // One structural touch.
        return removed.value;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    @Override
    public boolean contains(int value) {
        for (Node current = head; current != null; current = current.next) {
            work++; // One value comparison.
            if (current.value == value) {
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

    private Node nodeAt(int index) {
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
            work++; // Follow one next reference.
        }
        work++; // Read the final node.
        return current;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
    }
}
