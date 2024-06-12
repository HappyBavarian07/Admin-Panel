package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 06.11.2021 | 12:22
 */

import java.util.*;

public class PaginatedList<T extends Comparable<T>> {
    private final Map<Integer, List<T>> resultMap;
    private final LinkedList<T> listOfThings;
    private int maxItemsPerPage = 1;
    private boolean sorted = false;

    public PaginatedList(List<T> listOfThings) {
        this.listOfThings = new LinkedList<>(listOfThings);
        this.resultMap = new HashMap<>();
    }

    public PaginatedList(Set<T> listOfThings) {
        this.listOfThings = new LinkedList<>(listOfThings);
        resultMap = new HashMap<>();
    }

    public Map<Integer, List<T>> getResultMap() throws ListNotSortedException {
        if (!sorted) throw new ListNotSortedException("The List isn't sorted yet! (Method: getResultMap())");
        return resultMap;
    }

    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }

    public PaginatedList<T> maxItemsPerPage(int maxItemsPerPage) {
        this.maxItemsPerPage = maxItemsPerPage;
        return this;
    }

    public List<T> getListOfThings() {
        return listOfThings;
    }

    // TODO Add Sort Method that can take a Comparator or a Function to sort the List if you want to use a Custom Sorting Algorithm
    /**
     * Sorts the list with the given sorting algorithm
     *
     * @param sortingAlgorithm The sorting algorithm to use
     * @param reSort           If the list should be resorted if it was already sorted
     * @return The sorted list
     */
    public PaginatedList<T> sort(String sortingAlgorithm, boolean reSort) {
        if (sorted && !reSort) return this;
        switch (sortingAlgorithm) {
            case "bubble":
                bubbleSort();
                break;
            case "quick":
                quickSort();
                break;
            case "insertion":
                insertionSort();
                break;
            case "selection":
                selectionSort();
                break;
            case "merge":
                mergeSort();
                break;
            case "heap":
                heapSort();
                break;
            case "shell":
                shellSort();
                break;
            case "comb":
                combSort();
                break;
            case "counting":
                if (!(listOfThings.get(0) instanceof Integer)) {
                    throw new IllegalArgumentException("Counting sort can only be used with Integers");
                }
                countingSort();
                break;
            case "radix":
                if (!(listOfThings.get(0) instanceof Integer)) {
                    throw new IllegalArgumentException("Radix sort can only be used with Integers");
                }
                radixSort();
                break;
            case "alphabetic":
                if (listOfThings.get(0) instanceof String) {
                    alphabeticSort();
                } else {
                    throw new IllegalArgumentException("Alphabetical sort can only be used with Strings");
                }
                break;
            case "alphanumeric":
                alphanumericSort();
                break;
            case "subcommand":
                if (listOfThings.get(0) instanceof SubCommand) {
                    subcommandSort();
                } else {
                    throw new IllegalArgumentException("SubCommand sort can only be used with SubCommands");
                }
                break;
            default:
                // This Method just puts the List into the Different Pages
                sorted = true;
                normalKindaUnsorted();
                break;
        }
        return this;
    }

    public static class SubCommandComparator implements Comparator<SubCommand> {
        @Override
        public int compare(SubCommand subCommand1, SubCommand subCommand2) {
            // Implement the comparison logic here
            // For example, you can compare the names of the sub-commands
            return subCommand1.compareTo(subCommand2);
        }
    }

    private void subcommandSort() {
        if(!(listOfThings.get(0) instanceof SubCommand)) {
            throw new IllegalArgumentException("SubCommand sort can only be used with SubCommands");
        }

        List<SubCommand> list = (List<SubCommand>) new ArrayList<>(listOfThings);
        list.sort(new SubCommandComparator());
        listOfThings.clear();
        listOfThings.addAll((Collection<? extends T>) list);
        sorted = true;
        normalKindaUnsorted();
    }

    // private sorting methods for the public sort method
    private void bubbleSort() {
        List<T> list = new ArrayList<>(listOfThings);
        //System.out.println("BubbleSort: " + list);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).compareTo(list.get(j + 1)) > 0) {
                    T temp = list.get(j);
                    //System.out.println("Temp: " + temp);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
        listOfThings.clear();
        listOfThings.addAll(list);

        //System.out.println("BubbleSort: " + listOfThings);

        sorted = true;
        normalKindaUnsorted();
    }

    private void quickSort() {
        List<T> list = new ArrayList<>(listOfThings);
        quickSort(list, 0, list.size() - 1);

        listOfThings.clear();
        listOfThings.addAll(list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void quickSort(List<T> list, int low, int high) {
        if (low < high) {
            int pi = partition(list, low, high);
            quickSort(list, low, pi - 1);
            quickSort(list, pi + 1, high);
        }
    }

    private int partition(List<T> list, int low, int high) {
        T pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (list.get(j).compareTo(pivot) < 0) {
                i++;
                T temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }
        T temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);
        return i + 1;
    }

    private void insertionSort() {
        List<T> list = new ArrayList<>(listOfThings);
        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);
            int j = i - 1;
            while (j >= 0 && list.get(j).compareTo(key) > 0) {
                list.set(j + 1, list.get(j));
                j = j - 1;
            }
            list.set(j + 1, key);
        }

        listOfThings.clear();
        listOfThings.addAll(list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void selectionSort() {
        List<T> list = new ArrayList<>(listOfThings);
        for (int i = 0; i < list.size() - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).compareTo(list.get(minIndex)) < 0) {
                    minIndex = j;
                }
            }
            T temp = list.get(minIndex);
            list.set(minIndex, list.get(i));
            list.set(i, temp);
        }

        listOfThings.clear();
        listOfThings.addAll(list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void mergeSort() {
        List<T> list = new ArrayList<>(listOfThings);
        mergeSort(list, 0, list.size() - 1);

        listOfThings.clear();
        listOfThings.addAll(list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void mergeSort(List<T> list, int l, int r) {
        if (l < r) {
            int m = l + (r - l) / 2;
            mergeSort(list, l, m);
            mergeSort(list, m + 1, r);
            merge(list, l, m, r);
        }
    }

    private void merge(List<T> list, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;
        List<T> L = new ArrayList<>();
        List<T> R = new ArrayList<>();
        for (int i = 0; i < n1; i++) {
            L.add(list.get(l + i));
        }
        for (int j = 0; j < n2; j++) {
            R.add(list.get(m + 1 + j));
        }
        int i = 0, j = 0;
        int k = l;
        while (i < n1 && j < n2) {
            if (((Comparable<T>) L.get(i)).compareTo(R.get(j)) <= 0) {
                list.set(k, L.get(i));
                i++;
            } else {
                list.set(k, R.get(j));
                j++;
            }
            k++;
        }
        while (i < n1) {
            list.set(k, L.get(i));
            i++;
            k++;
        }
        while (j < n2) {
            list.set(k, R.get(j));
            j++;
            k++;
        }
    }

    private void heapSort() {
        List<T> list = new ArrayList<>(listOfThings);
        int n = list.size();
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(list, n, i);
        }
        for (int i = n - 1; i > 0; i--) {
            T temp = list.get(0);
            list.set(0, list.get(i));
            list.set(i, temp);
            heapify(list, i, 0);
        }

        listOfThings.clear();
        listOfThings.addAll(list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void heapify(List<T> list, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < n && list.get(l).compareTo(list.get(largest)) > 0) {
            largest = l;
        }
        if (r < n && list.get(r).compareTo(list.get(largest)) > 0) {
            largest = r;
        }
        if (largest != i) {
            T swap = list.get(i);
            list.set(i, list.get(largest));
            list.set(largest, swap);
            heapify(list, n, largest);
        }
    }

    private void shellSort() {
        List<T> list = new ArrayList<>(listOfThings);
        int n = list.size();
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i += 1) {
                T temp = list.get(i);
                int j;
                for (j = i; j >= gap && list.get(j - gap).compareTo(temp) > 0; j -= gap) {
                    list.set(j, list.get(j - gap));
                }
                list.set(j, temp);
            }
        }

        listOfThings.clear();
        listOfThings.addAll(list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void combSort() {
        List<T> list = new ArrayList<>(listOfThings);
        int n = list.size();
        int gap = n;
        boolean swapped = true;
        while (gap != 1 || swapped) {
            gap = getNextGap(gap);
            swapped = false;
            for (int i = 0; i < n - gap; i++) {
                if (list.get(i).compareTo(list.get(i + gap)) > 0) {
                    T temp = list.get(i);
                    list.set(i, list.get(i + gap));
                    list.set(i + gap, temp);
                    swapped = true;
                }
            }
        }

        listOfThings.clear();
        listOfThings.addAll(list);

        sorted = true;
        normalKindaUnsorted();
    }

    private int getNextGap(int gap) {
        gap = (gap * 10) / 13;
        return Math.max(gap, 1);
    }

    private void countingSort() {
        List<Integer> list = (List<Integer>) new ArrayList<>(listOfThings);
        int n = list.size();
        List<Integer> output = new ArrayList<>(Collections.nCopies(n, null));
        int max = list.get(0);
        for (int i = 1; i < n; i++) {
            if (list.get(i).compareTo(max) > 0) {
                max = list.get(i);
            }
        }
        List<Integer> count = new ArrayList<>(Collections.nCopies(max + 1, 0));
        for (Integer t : list) {
            count.set(t, count.get(t) + 1);
        }
        for (int i = 1; i < count.size(); i++) {
            count.set(i, count.get(i) + count.get(i - 1));
        }
        for (int i = n - 1; i >= 0; i--) {
            output.set(count.get(list.get(i)) - 1, list.get(i));
            count.set(list.get(i), count.get(list.get(i)) - 1);
        }
        for (int i = 0; i < n; i++) {
            list.set(i, output.get(i));
        }

        listOfThings.clear();
        if (list.get(0) != null)
            listOfThings.addAll((Collection<? extends T>) list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void radixSort() {
        List<Integer> list = (List<Integer>) new ArrayList<>(listOfThings);
        int n = list.size();
        int max = (int) list.get(0);
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countSort(list, n, exp);
        }

        listOfThings.clear();
        if (list.get(0) != null)
            listOfThings.addAll((Collection<? extends T>) list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void countSort(List<Integer> list, int n, int exp) {
        List<Integer> output = new ArrayList<>(Collections.nCopies(n, null));
        int[] count = new int[10];
        for (int i = 0; i < n; i++) {
            count[(list.get(i) / exp) % 10]++;
        }
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }
        for (int i = n - 1; i >= 0; i--) {
            output.set(count[(list.get(i) / exp) % 10] - 1, list.get(i));
            count[(list.get(i) / exp) % 10]--;
        }
        for (int i = 0; i < n; i++) {
            list.set(i, output.get(i));
        }
    }

    private void alphabeticSort() {
        List<T> list = new ArrayList<>(listOfThings);

        Collections.sort(list);

        listOfThings.clear();
        listOfThings.addAll(list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void alphanumericSort() {
        List<String> list = (List<String>) new ArrayList<>(listOfThings);
        list.sort(new NaturalOrderComparator());

        listOfThings.clear();
        if (list.get(0) != null)
            listOfThings.addAll((Collection<? extends T>) list);

        sorted = true;
        normalKindaUnsorted();
    }

    private void normalKindaUnsorted() {
        List<T> list = new ArrayList<>(listOfThings);
        int index = 0;
        int page = 1;
        List<T> pageList = new ArrayList<>();
        for (T t : list) {
            if (index == maxItemsPerPage) {
                resultMap.put(page, pageList);
                pageList = new ArrayList<>();
                index = 0;
                page++;
            }
            pageList.add(t);
            index++;
        }
        if (!pageList.isEmpty()) {
            resultMap.put(page, pageList);
        }


    }

    public List<T> getPage(int page) throws ListNotSortedException {
        if (!sorted) throw new ListNotSortedException("The List isn't sorted yet! (Method: getPage())");
        return getResultMap().get(page);
    }

    public boolean containsPage(int page) throws ListNotSortedException {
        if (!sorted) throw new ListNotSortedException("The List isn't sorted yet! (Method: containsPage())");
        return getResultMap().containsKey(page);
    }

    public int getMaxPage() throws ListNotSortedException {
        if (!sorted) throw new ListNotSortedException("The List isn't sorted yet! (Method: getMaxPage())");
        return getResultMap().size();
    }

    public static class ListNotSortedException extends Exception {
        public ListNotSortedException() {
        }

        public ListNotSortedException(String message) {
            super(message);
        }

        public ListNotSortedException(String message, Throwable cause) {
            super(message, cause);
        }

        public ListNotSortedException(Throwable cause) {
            super(cause);
        }

        public ListNotSortedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
