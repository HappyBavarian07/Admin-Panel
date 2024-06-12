package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 19.12.2022 | 12:44
 */

public enum Result {
    SUCCESS(true),
    WARN(true),
    ERROR(false),
    FAILURE(false),
    EMPTYOBJECT(false),
    WRONGMAPIDENTIFIER(false),
    EMPTYLIST(false),
    EMPTYMAP(false);

    private final boolean success;

    Result(boolean success) {
        this.success = success;
    }

    public boolean hasSucceeded() {
        return success;
    }
}
