package de.happybavarian07.adminpanel.permissions.api;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired on the main thread after the permission manager changed a user or a group, so other plugins can refresh what they cache.
 * It cannot be cancelled; the change has already happened.
 */
public class PermissionChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    /** What happened. */
    public enum Kind {
        /** An entry was set or changed. */
        SET,
        /** An entry was removed. */
        UNSET,
        /** A user joined a group or a group got a parent. */
        GROUP_ADDED,
        /** A user left a group or a group lost a parent. */
        GROUP_REMOVED,
        /** A temporary entry ended. */
        EXPIRED,
        GROUP_CREATED,
        GROUP_DELETED,
        /** Weight, prefix, suffix or display name of a group changed. */
        GROUP_CHANGED,
        /** All entries of a user were removed. */
        USER_CLEARED,
        /** Everything was reloaded from the database. */
        RELOADED
    }

    private final Kind kind;
    private final String subject;
    private final String detail;
    private final CommandSender actor;

    /**
     * @param subject {@code user:<uuid>} or {@code group:<name>}, empty for {@link Kind#RELOADED}
     * @param detail  the permission or group concerned, may be empty
     * @param actor   who did it, {@code null} for the console or the system
     */
    public PermissionChangeEvent(Kind kind, String subject, String detail, @Nullable CommandSender actor) {
        this.kind = kind;
        this.subject = subject;
        this.detail = detail;
        this.actor = actor;
    }

    public Kind getKind() {
        return kind;
    }

    public String getSubject() {
        return subject;
    }

    public String getDetail() {
        return detail;
    }

    public @Nullable CommandSender getActor() {
        return actor;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
