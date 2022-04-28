package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 06.11.2021 | 12:22
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaginatedList<T> {
    private final Map<Integer, List<T>> resultMap;
    private int maxItemsPerPage = 1;
    private final List<T> listOfThings;
    private boolean sorted = false;

    public PaginatedList(List<T> listOfThings) {
        this.listOfThings = listOfThings;
        this.resultMap = new HashMap<>();
    }

    public Map<Integer, List<T>> getResultMap() throws ListNotSortedException {
        if(!sorted) throw new ListNotSortedException("The List isn't sorted yet! (Method: getResultMap())");
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

    public PaginatedList<T> sort() {
        if(sorted) return this;
        resultMap.clear();
        int count = 0;
        int currentPage = 1;
        List<T> tempObs = new ArrayList<>();
        for (T sub : listOfThings) {
            tempObs.add(sub);
            if (count == (maxItemsPerPage-1) || listOfThings.get(listOfThings.size() - 1) == sub) {
                List<T> tempTempObs = new ArrayList<>(tempObs);
                resultMap.put(currentPage, tempTempObs);
                count = 0;
                currentPage += 1;
                tempObs.clear();
            } else {
                count++;
            }
        }
        if(!resultMap.isEmpty()) {
            sorted = true;
        }
        return this;
    }

    public List<T> getPage(int page) throws ListNotSortedException {
        if(!sorted) throw new ListNotSortedException("The List isn't sorted yet! (Method: getPage())");
        return getResultMap().get(page);
    }

    public boolean containsPage(int page) throws ListNotSortedException {
        if(!sorted) throw new ListNotSortedException("The List isn't sorted yet! (Method: containsPage())");
        return getResultMap().containsKey(page);
    }

    public int getMaxPage() throws ListNotSortedException {
        if(!sorted) throw new ListNotSortedException("The List isn't sorted yet! (Method: getMaxPage())");
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
