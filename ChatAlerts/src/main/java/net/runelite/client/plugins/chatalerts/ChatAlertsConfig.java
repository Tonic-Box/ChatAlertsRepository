package net.runelite.client.plugins.chatalerts;

import lombok.AllArgsConstructor;
import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ChatAlerts")
public interface ChatAlertsConfig extends Config {
    @ConfigItem(
            keyName = "alert_mode",
            name = "Alert Mode",
            description = "",
            position = -1
    )
    default AlertMode alert_mode()
    {
        return AlertMode.EVERYCHAT;
    }

    @ConfigItem(
            keyName = "keywords",
            name = "Key Words",
            description = "",
            position = 0
    )
    default String keywords()
    {
        return "";
    }

    @ConfigItem(
            keyName = "FilterProfanity",
            name = "Profanity Filter",
            description = "",
            position = 1
    )
    default boolean profanityFilter()
    {
        return false;
    }

    @ConfigItem(
            keyName = "alert_sound",
            name = "Play Sound",
            description = "",
            position = 2
    )
    default boolean alert_sound() {
        return false;
    }

    @ConfigItem(
            keyName = "alert_win_notification",
            name = "Windows Notification",
            description = "",
            position = 3
    )
    default boolean alert_win_notification() {
        return false;
    }

    @ConfigItem(
            keyName = "alert_discord",
            name = "Discord Alerts",
            description = "",
            position = 4
    )
    default boolean alert_discord() {
        return false;
    }

    @ConfigItem(
            keyName = "alert_level_up",
            name = "Level Up Alerts",
            description = "",
            position = 5
    )
    default boolean alert_level_up() {
        return false;
    }

    @ConfigItem(
            keyName = "token",
            name = "Discord Bot Token",
            description = "",
            position = 6
    )
    default String token()
    {
        return "";
    }

    @ConfigItem(
            keyName = "Master UID",
            name = "Your discord UID",
            description = "",
            position = 7
    )
    default String uid() {
        return "";
    }

    @ConfigItem(
            keyName = "discordtoken",
            name = "Start Discord Response Bot",
            description = "",
            position = 7
    )
    default Button discordtoken() {
        return new Button();
    }

    @ConfigItem(
            keyName = "clear",
            name = "Clear Discord Log Chanel",
            description = "",
            position = 8
    )
    default Button clear() {
        return new Button();
    }

    @ConfigItem(
            keyName = "invite",
            name = "Copy Bot Invite to clipboard",
            description = "",
            position = 8
    )
    default Button invite() {
        return new Button();
    }

    @ConfigItem(
            keyName = "alert_logs",
            name = "Keep Text Logs",
            description = "",
            position = 9
    )
    default boolean alert_logs() {
        return false;
    }

    @ConfigItem(
            keyName = "logFolder",
            name = "Open Logs Folder",
            description = "",
            position = 10
    )
    default Button logFolder() {
        return new Button();
    }

    @AllArgsConstructor
    enum AlertMode
    {
        EVERYCHAT("Every Chat"),
        KEYWORDSONLY("Keywords Only");

        private final String value;

        @Override
        public String toString()
        {
            return value;
        }
    }
}