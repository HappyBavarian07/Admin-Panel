package de.happybavarian07.main;/*
 * @Author HappyBavarian07
 * @Date 15.11.2021 | 17:59
 */

public class NewLanguageFileUpdater {
    private final AdminPanelMain plugin;
    private final LanguageManager lgm;

    public NewLanguageFileUpdater(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.lgm = plugin.getLanguageManager();
    }

    /*
    Entweder default config erstellen und werte von alter Config einlesen und dort reinschreiben
    oder anders
     */

    /*public void updateFile(FileConfiguration oldConfig, String newConfigString, String langName) {
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource(newConfigString))));
        for(String path : oldConfig) {

        }
    }*/
}
