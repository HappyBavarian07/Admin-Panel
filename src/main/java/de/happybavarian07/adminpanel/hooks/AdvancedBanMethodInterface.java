package de.happybavarian07.adminpanel.hooks;/*
 * @Author HappyBavarian07
 * @Date 16.02.2024 | 14:33
 */

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.utils.Permissionable;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class AdvancedBanMethodInterface implements MethodInterface {
    private final File dataFolder;

    public AdvancedBanMethodInterface(File dataFolder){
        this.dataFolder = Objects.requireNonNull(dataFolder);
    }
    @Override
    public void loadFiles() {

    }

    @Override
    public String getFromUrlJson(String s, String s1) {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String[] getKeys(Object o, String s) {
        return new String[0];
    }

    @Override
    public Object getConfig() {
        return null;
    }

    @Override
    public Object getMessages() {
        return null;
    }

    @Override
    public Object getLayouts() {
        return null;
    }

    @Override
    public void setupMetrics() {

    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public String clearFormatting(String s) {
        return null;
    }

    @Override
    public Object getPlugin() {
        return null;
    }

    @Override
    public File getDataFolder() {
        return null;
    }

    @Override
    public void setCommandExecutor(String s, TabCompleter tabCompleter) {

    }

    @Override
    public void sendMessage(Object o, String s) {

    }

    @Override
    public String getName(Object o) {
        return null;
    }

    @Override
    public String getName(String s) {
        return null;
    }

    @Override
    public String getIP(Object o) {
        return null;
    }

    @Override
    public String getInternUUID(Object o) {
        return null;
    }

    @Override
    public String getInternUUID(String s) {
        return null;
    }

    @Override
    public boolean hasPerms(Object o, String s) {
        return false;
    }

    @Override
    public Permissionable getOfflinePermissionPlayer(String s) {
        return null;
    }

    @Override
    public boolean isOnline(String s) {
        return false;
    }

    @Override
    public Object getPlayer(String s) {
        return null;
    }

    @Override
    public void kickPlayer(String s, String s1) {

    }

    @Override
    public Object[] getOnlinePlayers() {
        return new Object[0];
    }

    @Override
    public void scheduleAsyncRep(Runnable runnable, long l, long l1) {

    }

    @Override
    public void scheduleAsync(Runnable runnable, long l) {

    }

    @Override
    public void runAsync(Runnable runnable) {

    }

    @Override
    public void runSync(Runnable runnable) {

    }

    @Override
    public void executeCommand(String s) {

    }

    @Override
    public boolean callChat(Object o) {
        return false;
    }

    @Override
    public boolean callCMD(Object o, String s) {
        return false;
    }

    @Override
    public Object getMySQLFile() {
        return null;
    }

    @Override
    public String parseJSON(InputStreamReader inputStreamReader, String s) {
        return null;
    }

    @Override
    public String parseJSON(String s, String s1) {
        return null;
    }

    @Override
    public Boolean getBoolean(Object o, String s) {
        return null;
    }

    @Override
    public String getString(Object o, String s) {
        return null;
    }

    @Override
    public Long getLong(Object o, String s) {
        return null;
    }

    @Override
    public Integer getInteger(Object o, String s) {
        return null;
    }

    @Override
    public List<String> getStringList(Object o, String s) {
        return null;
    }

    @Override
    public boolean getBoolean(Object o, String s, boolean b) {
        return false;
    }

    @Override
    public String getString(Object o, String s, String s1) {
        return null;
    }

    @Override
    public long getLong(Object o, String s, long l) {
        return 0;
    }

    @Override
    public int getInteger(Object o, String s, int i) {
        return 0;
    }

    @Override
    public boolean contains(Object o, String s) {
        return false;
    }

    @Override
    public String getFileName(Object o) {
        return null;
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {

    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean b) {

    }

    @Override
    public boolean isOnlineMode() {
        return false;
    }

    @Override
    public void notify(String s, List<String> list) {

    }

    @Override
    public void log(String s) {

    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }
}
