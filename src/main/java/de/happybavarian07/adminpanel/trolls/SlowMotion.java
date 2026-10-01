package de.happybavarian07.adminpanel.trolls;

/** The target moves at 30 percent of the normal speed. */
public final class SlowMotion extends AttributeToggle {
    SlowMotion() {
        super("SlowMotion", -0.7, "MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED");
    }
}
