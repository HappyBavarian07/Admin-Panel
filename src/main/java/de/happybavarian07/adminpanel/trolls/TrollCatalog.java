package de.happybavarian07.adminpanel.trolls;

import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.plugin.Plugin;

/** The trolls that ship with the plugin. */
public final class TrollCatalog {
    private TrollCatalog() {
    }

    /**
     * Registers every troll the running server supports. {@code NoJump} and {@code SlowMotion} are left out when the
     * server has no attribute for them.
     */
    public static void registerAll(TrollRegistry registry, Plugin plugin, LanguageManager lgm) {
        LegacyTrolls.registerAll(registry, plugin);
        registry.register(new CreeperHiss());
        registry.register(new FakeExplosion());
        registry.register(new Jumpscare(lgm));
        registry.register(new DemoScreen());
        registry.register(new SafeTnt(plugin));
        registry.register(new Launch(plugin));
        registry.register(new RandomHop(lgm));
        registry.register(new SpinCamera(plugin));
        NoJump noJump = new NoJump();
        if (noJump.isSupported()) registry.register(noJump);
        SlowMotion slowMotion = new SlowMotion();
        if (slowMotion.isSupported()) registry.register(slowMotion);
    }
}
