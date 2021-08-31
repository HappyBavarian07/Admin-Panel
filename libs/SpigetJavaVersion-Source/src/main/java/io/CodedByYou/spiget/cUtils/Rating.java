package io.CodedByYou.spiget.cUtils;

/**
 * Created by CodedByYou on 10/8/2017.
 * Day: Sunday
 * Time: 10:24 PM
 */
public class Rating {
    private int count;
    private int average;
    public Rating(int count , int averge){
        this.count = count;
        this.average = count;
    }

    public int getAverage() {
        return average;
    }

    public int getCount() {
        return count;
    }
}
