package kz.aitu.daa.assignment2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class IntListTest {
    private final IntList[] lists = {new DynamicArray(), new SimpleLinkedList()};

    @Test
    void emptyAndInvalidIndices() {
        for (IntList list : lists) {
            assertEquals(0, list.size());
            assertFalse(list.contains(5));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 5));
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 5));
        }
    }

    @Test
    void oneElementAndDuplicates() {
        for (IntList list : lists) {
            list.add(7);
            assertEquals(7, list.get(0));
            list.add(0, 7);
            list.add(2, 9);
            assertEquals(3, list.size());
            assertTrue(list.contains(7));
            assertEquals(7, list.remove(0));
            assertEquals(9, list.remove(1));
            assertEquals(7, list.remove(0));
            assertEquals(0, list.size());
        }
    }

    @Test
    void compareWithJavaLists() {
        for (IntList list : lists) {
            List<Integer> reference = list instanceof SimpleLinkedList
                    ? new LinkedList<>() : new ArrayList<>();
            Random random = new Random(42);
            for (int i = 0; i < 2_000; i++) {
                int action = random.nextInt(4);
                if (reference.isEmpty() || action == 0) {
                    int value = random.nextInt(50);
                    list.add(value);
                    reference.add(value);
                } else if (action == 1) {
                    int index = random.nextInt(reference.size() + 1);
                    int value = random.nextInt(50);
                    list.add(index, value);
                    reference.add(index, value);
                } else if (action == 2) {
                    int index = random.nextInt(reference.size());
                    assertEquals(reference.remove(index), list.remove(index));
                } else {
                    int index = random.nextInt(reference.size());
                    assertEquals(reference.get(index), list.get(index));
                }
                assertEquals(reference.size(), list.size());
                assertEquals(reference.contains(7), list.contains(7));
            }
            for (int i = 0; i < reference.size(); i++) {
                assertEquals(reference.get(i), list.get(i));
            }
        }
    }

    @Test
    void workCountersMeasureTheDocumentedOperations() {
        DynamicArray array = new DynamicArray();
        SimpleLinkedList linked = new SimpleLinkedList();
        for (int value : new int[]{1, 2, 3}) {
            array.add(value);
            linked.add(value);
        }
        array.resetWork();
        linked.resetWork();
        assertEquals(3, array.get(2));
        assertEquals(1, array.work());
        assertEquals(3, linked.get(2));
        assertEquals(3, linked.work());
        array.resetWork();
        linked.resetWork();
        assertFalse(array.contains(9));
        assertFalse(linked.contains(9));
        assertEquals(3, array.work());
        assertEquals(3, linked.work());
    }

    @Test
    void largeAppendAndBoundaryRemoval() {
        for (IntList list : lists) {
            for (int i = 0; i < 10_000; i++) {
                list.add(i);
            }
            assertEquals(10_000, list.size());
            assertEquals(0, list.remove(0));
            assertEquals(9_999, list.remove(list.size() - 1));
            assertEquals(1, list.get(0));
            assertEquals(9_998, list.get(list.size() - 1));
        }
    }
}
