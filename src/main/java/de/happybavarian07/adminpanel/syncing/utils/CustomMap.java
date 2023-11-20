package de.happybavarian07.adminpanel.syncing.utils;/*
 * @Author HappyBavarian07
 * @Date 23.11.2022 | 15:40
 */

import java.util.Map;

public class CustomMap<T1, T2> {
    private Map<T1, T2> map;

    public CustomMap(Map<T1, T2> map) {
        this.map = map;
    }

    public Map<T1, T2> getMap() {
        return map;
    }

    public void setMap(Map<T1, T2> map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "CustomMap{" +
                "map=" + map +
                '}';
    }
}
