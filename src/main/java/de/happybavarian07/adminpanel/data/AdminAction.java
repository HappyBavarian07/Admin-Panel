package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.GeneratedValue;
import de.happybavarian07.coolstufflib.jpa.annotations.GenerationType;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

/** One row of the admin journal: who did what to whom and when. Rows are only ever added, never changed. */
@Entity
@Table(name = "admin_actions")
public class AdminAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", primaryKey = true)
    private long id;

    @Column(name = "created_at")
    private long createdAt;

    @Column(name = "actor")
    private String actor;

    @Column(name = "actor_uuid")
    private String actorUuid;

    @Column(name = "action")
    private String action;

    @Column(name = "target")
    private String target;

    @Column(name = "details")
    private String details;

    public AdminAction() {
    }

    public AdminAction(long createdAt, String actor, String actorUuid, String action, String target, String details) {
        this.createdAt = createdAt;
        this.actor = actor;
        this.actorUuid = actorUuid;
        this.action = action;
        this.target = target;
        this.details = details;
    }

    public long getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getActor() {
        return actor == null ? "" : actor;
    }

    public String getActorUuid() {
        return actorUuid == null ? "" : actorUuid;
    }

    public String getAction() {
        return action == null ? "" : action;
    }

    public String getTarget() {
        return target == null ? "" : target;
    }

    public String getDetails() {
        return details == null ? "" : details;
    }
}
