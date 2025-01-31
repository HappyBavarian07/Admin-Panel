package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 12.11.2021 | 17:55
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CommandData annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandData {
    /**
     * Specifies whether a command requires a player as the sender.
     * <p>
     * This method is a part of the {@code CommandData} annotation and determines if the associated command
     * must be executed by a player. If this returns {@code true}, other types of command senders
     * (e.g., server console or command blocks) will not be able to execute the annotated command.
     * </p>
     *
     * @return {@code true} if the command requires a player as the sender; {@code false} otherwise.
     */
    boolean playerRequired() default false;
    /**
     * Specifies whether operator (op) permissions are required to execute a command.
     * <p>
     * When set to <code>true</code>, the command will only be executable by players or entities
     * with op (operator) permissions. Otherwise, the command can be executed by any sender
     * with the necessary non-op-specific permissions.
     * </p>
     *
     * @return <code>true</code> if the command requires operator permissions;
     *         <code>false</code> if no specific operator permissions are required (default is <code>false</code>).
     */
    boolean opRequired() default false;
    /**
     * Specifies whether only sub-command arguments that match the defined sub-command arguments
     * are allowed. This is used to enforce strict matching of sub-arguments to ensure command inputs
     * conform to expected patterns or rules.
     *
     * <p>If set to <code>true</code>, sub-command arguments must align precisely with the allowed
     * sub-arguments. If set to <code>false</code>, sub-command arguments are not strictly constrained.
     *
     * @return <code>true</code> if only sub-command arguments that fit to defined sub-arguments are allowed;
     *         <code>false</code> otherwise. Default is <code>false</code>.
     */
    boolean allowOnlySubCommandArgsThatFitToSubArgs() default false;
    /**
     * Specifies whether the command should allow subcommand arguments specific to the sender type.
     * <p>
     * When this value is set to <code>true</code>, the command system may enforce certain subcommand
     * arguments based on the type of the sender (e.g., Player vs. Console).
     * </p>
     *
     * @return <code>true</code> if subcommand arguments should be restricted based on the sender type;
     *         <code>false</code> otherwise.
     */
    boolean senderTypeSpecificSubArgs() default false;
    /**
     * Specifies the minimum number of arguments required for the associated command.
     * <p>
     * This attribute is used to ensure that a command has at least the specified
     * number of arguments provided upon execution.
     * </p>
     *
     * @return the minimum number of arguments required; defaults to 0 if not specified.
     */
    int minArgs() default 0;
    /**
     * Specifies the maximum number of arguments allowed for a command.
     *
     * <p>This parameter is used to define the upper limit on the number of arguments
     * that can be passed to a command. It can help in validation and ensuring
     * commands are invoked with the correct amount of data.</p>
     *
     * <ul>
     *     <li>If not explicitly defined, the default value is {@code Integer.MAX_VALUE},
     *     which means there is no practical restriction on the number of arguments.</li>
     * </ul>
     *
     * @return the maximum number of arguments allowed for the command. Defaults to {@code Integer.MAX_VALUE}.
     */
    int maxArgs() default Integer.MAX_VALUE;
}
