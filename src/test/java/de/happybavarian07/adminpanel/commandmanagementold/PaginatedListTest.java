package de.happybavarian07.adminpanel.commandmanagementold;

import de.happybavarian07.coolstufflib.commandmanagement.PaginatedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PaginatedListTest {
    private PaginatedList<Integer> paginatedList;

    @BeforeEach
    public void setUp() {
        List<Integer> listOfThings = Arrays.asList(5, 3, 7, 1, 4, 6, 2);
        paginatedList = new PaginatedList<>(listOfThings);
    }

    @Test
    public void testMaxItemsPerPage() {
        paginatedList.maxItemsPerPage(3);
        assertEquals(3, paginatedList.getMaxItemsPerPage());
    }

    private PaginatedList<Integer> testSortAlgorithm(String algorithm) {
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort(algorithm, false);
        return paginatedList;
    }

    private <E extends Comparable<E>> void assertPageEquals(PaginatedList<E> pl, int page, List<E> expected) {
        try {
            assertEquals(expected, pl.getPage(page));
        } catch (PaginatedList.ListNotSortedException e) {
            fail("List should be sorted");
        }
    }

    @Test
    public void testBubbleSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("bubble");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 2, Arrays.asList(4, 5, 6));
    }

    @Test
    public void testQuickSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("quick");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 3, Collections.singletonList(7));
    }

    @Test
    public void testInsertionSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("insertion");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 2, Arrays.asList(4, 5, 6));
    }

    @Test
    public void testSelectionSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("selection");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 2, Arrays.asList(4, 5, 6));
    }

    @Test
    public void testMergeSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("merge");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 2, Arrays.asList(4, 5, 6));
    }

    @Test
    public void testHeapSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("heap");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 2, Arrays.asList(4, 5, 6));
    }

    @Test
    public void testShellSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("shell");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 3, Collections.singletonList(7));
    }

    @Test
    public void testCombSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("comb");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 2, Arrays.asList(4, 5, 6));
    }

    @Test
    public void testCountingSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("counting");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 2, Arrays.asList(4, 5, 6));
    }

    @Test
    public void testRadixSort() {
        PaginatedList<Integer> pl = testSortAlgorithm("radix");
        assertPageEquals(pl, 1, Arrays.asList(1, 2, 3));
        assertPageEquals(pl, 3, Collections.singletonList(7));
    }

    @Test
    public void testAlphanumericSort() {
        List<String> listOfThings = Arrays.asList("item20", "item3", "item1", "item2", "item10");
        PaginatedList<String> paginatedList = new PaginatedList<>(listOfThings);
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort("alphanumeric", false);
        assertPageEquals(paginatedList, 1, Arrays.asList("item1", "item2", "item3"));
        assertPageEquals(paginatedList, 2, Arrays.asList("item10", "item20"));
    }

    @Test
    public void testAlphabeticalSort() {
        List<String> listOfThings = Arrays.asList("itemC", "itemA", "itemB", "itemD", "itemE");
        PaginatedList<String> paginatedList = new PaginatedList<>(listOfThings);
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort("alphabetic", false);
        assertPageEquals(paginatedList, 1, Arrays.asList("itemA", "itemB", "itemC"));
        assertPageEquals(paginatedList, 2, Arrays.asList("itemD", "itemE"));
    }

    @Test
    public void testGetPage() {
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort("bubble", false);
        try {
            List<Integer> firstPage = paginatedList.getPage(1);
            assertEquals(Arrays.asList(1, 2, 3), firstPage);
        } catch (PaginatedList.ListNotSortedException e) {
            fail("List should be sorted");
        }
    }

    @Test
    public void testContainsPage() {
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort("bubble", false);
        try {
            assertTrue(paginatedList.containsPage(1));
            assertFalse(paginatedList.containsPage(4));
        } catch (PaginatedList.ListNotSortedException e) {
            fail("List should be sorted");
        }
    }

    @Test
    public void testGetMaxPage() {
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort("bubble", false);
        try {
            assertEquals(3, paginatedList.getMaxPage());
        } catch (PaginatedList.ListNotSortedException e) {
            fail("List should be sorted");
        }
    }
}