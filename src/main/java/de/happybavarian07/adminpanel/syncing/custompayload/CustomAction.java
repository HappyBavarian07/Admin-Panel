package de.happybavarian07.adminpanel.syncing.custompayload;

import java.util.Map;

public enum CustomAction {
    EXECUTE_COMMAND("Execute a command on the server or a player.", "Player/UUID", "Command"),
    SEND_MESSAGE("Send a message to a player or a group of players.", "Player/UUID", "Message"),
    // Prefix can be null or "", but it will always be replaced with %prefix% in the message
    BROADCAST_MESSAGE("Broadcast a message to all players on the server or network.", "Prefix", "Message"),
    TELEPORT_PLAYER("Teleport a player to a specific location or another player.", "Player/UUID", "Location"),
    GIVE_ITEM("Give an item to a player's inventory.", "Player/UUID", "Item"),
    SPAWN_ENTITY("Create and spawn a custom entity in the game world.", "Location", "Entity"),
    CHANGE_GAME_MODE("Change a player's game mode (e.g., Survival, Creative).", "Player/UUID", "GameMode"),
    KICK_PLAYER("Kick a player from the server with a custom message.", "Player/UUID", "KickReason"),
    BAN_PLAYER("Ban a player from the server or network.", "Player/UUID", "BanReason", "Duration", "DurationUnit", "BanType"),
    UNBAN_PLAYER("Remove a ban on a player.", "Player/UUID"),
    MUTE_PLAYER("Mute a player's chat or voice communication.", "Player/UUID", "MuteReason", "Duration", "DurationUnit", "MuteType"),
    UNMUTE_PLAYER("Remove a mute on a player.", "Player/UUID"),
    SEND_TITLE("Display a title and subtitle on a player's screen.", "Player/UUID", "Title", "Subtitle", "FadeIn", "Stay", "FadeOut"),
    SEND_ACTION_BAR("Show a message in the player's action bar.", "Player/UUID", "Message"),
    REQUEST_INFORMATION("Request specific information from another server or player.", "Player/UUID", "Request"),
    PLAY_SOUND("Play a sound at a specific location or to a player.", "Player/UUID", "Sound", "Location"),
    WEATHER_CONTROL("Change the weather conditions in the game (e.g., clear, rainy, thunderstorm).", "World", "WeatherType"),
    CHANGE_TIME_OF_DAY("Modify the time of day in the game world (e.g., day, night).", "World", "Time"),
    PLAYER_FOLLOW("Make one player follow another within the game world", "Player/UUID", "TargetPlayer/TargetUUID"),
    NOACTION("No action", "");

    private final String description;
    private final String[] args;

    CustomAction(String description, String... args) {
        this.description = description;
        this.args = args;
    }

    public static CustomAction fromString(String s) {
        try {
            return valueOf(s.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return CustomAction.NOACTION;
        }
    }

    private static boolean contains(String s) {
        try {
            valueOf(s.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the data array has the correct number of arguments for the action.
     * Also checks if the data array has the correct types of arguments for the action.
     * <p>
     * Example:
     * <p>
     * args = ["Player/UUID", "Message"]<p>
     * data = ["Player:[Name]", "UUID:[UUID]", "Message:[Message]"]<p>
     * action = SEND_MESSAGE<p>
     * -> true, because the Data Array has the correct number of arguments and the correct types.<p>
     * <p>
     * args = ["Player/UUID", "Message"]<p>
     * data = ["Player:[Name]", "UUID:[UUID]"]<p>
     * action = SEND_MESSAGE<p>
     * -> false, because the Data Array misses the Message Argument.<p>
     * It will also fail if you don't add the "Message:" or "Player:" at the start of the data.<p>
     * <p>
     *
     * @param data   The data map (Now a Map)
     * @param action The action
     * @return true if the data array has the correct number of arguments and the correct types of arguments for the action.
     */
    public static boolean dataHasCorrectArgs(Map<String, String> data, CustomAction action) {
        String[] actionArgs = action.getArgs();

        for (String argGroup : actionArgs) {
            String[] subArgs = argGroup.split("/"); // Split combined arguments
            boolean subArgFound = false;

            for (String subArg : subArgs) {
                if (data.containsKey(subArg)) {
                    subArgFound = true;
                    break;
                }
            }

            if (!subArgFound) {
                return false; // Check if at least one sub-argument is present in the data
            }
        }

        return true; // All sub-arguments were found
    }

    /**
     * Checks if the data array has the correct number of arguments for the action.
     * Also checks if the data array has the correct types of arguments for the action.
     * <p>
     * Example:
     * <p>
     * args = ["Player/UUID", "Message"]<p>
     * data = ["Player:[Name]", "UUID:[UUID]", "Message:[Message]"]<p>
     * action = SEND_MESSAGE<p>
     * -> true, because the Data Array has the correct number of arguments and the correct types.<p>
     * <p>
     * args = ["Player/UUID", "Message"]<p>
     * data = ["Player:[Name]", "UUID:[UUID]"]<p>
     * action = SEND_MESSAGE<p>
     * -> false, because the Data Array misses the Message Argument.<p>
     * It will also fail if you don't add the "Message:" or "Player:" at the start of the data.<p>
     * <p>
     *
     * @param data   The data map (Now a Map)
     * @param actionArgs The action Args
     * @return true if the data array has the correct number of arguments and the correct types of arguments for the action.
     */
    public static boolean dataHasCorrectArgs(Map<String, String> data, String[] actionArgs) {
        for (String argGroup : actionArgs) {
            String[] subArgs = argGroup.split("/"); // Split combined arguments
            boolean subArgFound = false;

            for (String subArg : subArgs) {
                if (data.containsKey(subArg)) {
                    subArgFound = true;
                    break;
                }
            }

            if (!subArgFound) {
                return false; // Check if at least one sub-argument is present in the data
            }
        }

        return true; // All sub-arguments were found
    }


    public String getDescription() {
        return description;
    }

    public String[] getArgs() {
        return args;
    }
}

