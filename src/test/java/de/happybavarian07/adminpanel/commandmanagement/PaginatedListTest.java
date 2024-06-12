package de.happybavarian07.adminpanel.commandmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    private Map<Integer, List<Integer>> testSortAlgorithm(String algorithm) {
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort(algorithm, false);
        try {
            Map<Integer, List<Integer>> resultMap = paginatedList.getResultMap();
            System.out.println("Results for " + algorithm + " sort:");
            resultMap.forEach((key, value) -> System.out.println("Page " + key + ": " + value));
            return resultMap;
        } catch (PaginatedList.ListNotSortedException e) {
            fail("List should be sorted");
            return null;
        }
    }

    @Test
    public void testBubbleSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("bubble");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testQuickSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("quick");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testInsertionSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("insertion");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testSelectionSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("selection");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testMergeSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("merge");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testHeapSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("heap");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testShellSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("shell");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testCombSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("comb");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testCountingSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("counting");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testRadixSort() {
        Map<Integer, List<Integer>> resultMap = testSortAlgorithm("radix");
        assertEquals(Arrays.asList(1, 2, 3), resultMap.get(0));
    }

    @Test
    public void testAlphanumericSort() {
        List<String> listOfThings = Arrays.asList("item20", "item3", "item1", "item2", "item10");
        PaginatedList<String> paginatedList = new PaginatedList<>(listOfThings);
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort("alphanumeric", false);
        try {
            List<String> firstPage = paginatedList.getPage(0);
            Map<Integer, List<String>> resultMap = paginatedList.getResultMap();
            System.out.println("Results for alphanumeric sort:");
            resultMap.forEach((key, value) -> System.out.println("Page " + key + ": " + value));
            assertEquals(Arrays.asList("item1", "item2", "item3"), firstPage);
        } catch (PaginatedList.ListNotSortedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAlphabeticalSort() {
        List<String> listOfThings = Arrays.asList("itemC", "itemA", "itemB", "itemD", "itemE");
        PaginatedList<String> paginatedList = new PaginatedList<>(listOfThings);
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort("alphabetic", false);
        try {
            List<String> firstPage = paginatedList.getPage(0);
            Map<Integer, List<String>> resultMap = paginatedList.getResultMap();
            System.out.println("Results for alphabetic sort:");
            resultMap.forEach((key, value) -> System.out.println("Page " + key + ": " + value));
            assertEquals(Arrays.asList("itemA", "itemB", "itemC"), firstPage);
        } catch (PaginatedList.ListNotSortedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetPage() {
        paginatedList.maxItemsPerPage(3);
        paginatedList.sort("bubble", false);
        try {
            List<Integer> firstPage = paginatedList.getPage(0);
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
            assertTrue(paginatedList.containsPage(0));
            assertFalse(paginatedList.containsPage(3));
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