package de.happybavarian07.adminpanel.trolls;

/** The target cannot jump: the jump strength is scaled to zero. Needs a server with the jump strength attribute. */
public final class NoJump extends AttributeToggle {
    NoJump() {
        super("NoJump", -1.0, "JUMP_STRENGTH", "GENERIC_JUMP_STRENGTH");
    }
}
