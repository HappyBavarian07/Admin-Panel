package de.happybavarian07.adminpanel.trolls;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * The elder guardian curse (guardian on the screen and its sound) for the target, with the title and subtitle of
 * {@code Trolls.Jumpscare} in the language file. Leave both empty there to show no title.
 */
public final class Jumpscare implements Troll {
    private final LanguageManager lgm;

    public Jumpscare(LanguageManager lgm) {
        this.lgm = lgm;
    }

    @Override
    public String id() {
        return "Jumpscare";
    }

    @Override
    public Kind kind() {
        return Kind.ONE_SHOT;
    }

    @Override
    public void start(Player staff, Player target) {
        Particle particle = TrollSupport.particle("ELDER_GUARDIAN", "MOB_APPEARANCE");
        if (particle != null) target.spawnParticle(particle, target.getLocation(), 1);
        target.playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
        String title = text(target, "Trolls.Jumpscare.Title");
        String subtitle = text(target, "Trolls.Jumpscare.Subtitle");
        if (!title.isBlank() || !subtitle.isBlank()) target.sendTitle(title, subtitle, 0, 30, 10);
    }

    private String text(Player target, String key) {
        String text = CommandText.get(lgm, target, key);
        return text.startsWith("null path:") || text.equals("null config") ? "" : text;
    }
}
