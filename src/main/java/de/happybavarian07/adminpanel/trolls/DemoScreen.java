package de.happybavarian07.adminpanel.trolls;

import org.bukkit.entity.Player;

/** Shows the target the welcome screen of the demo mode. The target closes it with ESC. */
public final class DemoScreen implements Troll {
    @Override
    public String id() {
        return "DemoScreen";
    }

    @Override
    public Kind kind() {
        return Kind.ONE_SHOT;
    }

    @Override
    public void start(Player staff, Player target) {
        target.showDemoScreen();
    }
}
