package de.happybavarian07.adminpanel.hooks;/*
 * @Author HappyBavarian07
 * @Date 16.02.2024 | 14:15
 */

import dte.hooksystem.hook.AbstractPluginHook;
import litebans.api.Database;

public class LiteBansHook extends AbstractPluginHook {
    private Object banPluginAPI;
    private AdvancedBanMethodInterface advancedBanMethodInterface;

    public LiteBansHook(String pluginName) {
        super(pluginName);
    }

    @Override
    public void init() throws Exception {
        this.banPluginAPI = queryProvider(Database.class);
        if (this.banPluginAPI == null) {
            this.banPluginAPI = Database.get();
        }
        /* TODO Will implement this sometime, but its complicated and I don't have the time for it nor the will to do it right now
        else if(Bukkit.getPluginManager().isPluginEnabled("AdvancedBan")) {
            this.banPluginAPI = queryProvider(Universal.class);
        }*/
    }


}
