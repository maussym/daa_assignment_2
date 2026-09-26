package kz.aitu.daa.assignment2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MinHeapTest {
    @Test
    void emptyAndOneElement() {
        MinHeap heap = new MinHeap();
        assertThrows(NoSuchElementException.class, heap::peekMin);
        assertThrows(NoSuchElementException.class, heap::extractMin);
        heap.insert(4);
        assertTrue(heap.isValidHeap());
        assertEquals(4, heap.peekMin());
        assertEquals(4, heap.extractMin());
        assertEquals(0, heap.size());
    }

    @Test
    void duplicatesAndNegativeValues() {
        MinHeap heap = new MinHeap();
        for (int value : new int[]{5, -2, 5, 0, -2}) {
            heap.insert(value);
            assertTrue(heap.isValidHeap());
        }
        for (int expected : new int[]{-2, -2, 0, 5, 5}) {
            assertEquals(expected, heap.extractMin());
            assertTrue(heap.isValidHeap());
        }
    }

    @Test
    void compareWithPriorityQueueOnLargeInput() {
        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> reference = new PriorityQueue<>();
        Random random = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            int value = random.nextInt();
            heap.insert(value);
            reference.add(value);
            assertTrue(heap.isValidHeap());
            assertEquals(reference.peek(), heap.peekMin());
        }
        while (!reference.isEmpty()) {
            assertEquals(reference.remove(), heap.extractMin());
            assertTrue(heap.isValidHeap());
        }
        assertEquals(0, heap.size());
    }
}
