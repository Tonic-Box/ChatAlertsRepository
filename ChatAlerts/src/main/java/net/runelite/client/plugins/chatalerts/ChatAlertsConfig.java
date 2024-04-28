package net.runelite.client.plugins.chatalerts;

import lombok.AllArgsConstructor;
import net.runelite.client.config.*;

@ConfigGroup("ChatAlerts")
public interface ChatAlertsConfig extends Config {
    @ConfigSection(
            name = "Alert Mediums",
            description = "",
            position = 0
    )
    String alert_mediums = "alert_mediums";
    @ConfigSection(
            name = "Telemetry Settings",
            description = "",
            position = 1
    )
    String telemetry_settings = "telemetry_settings";

    @ConfigSection(
            name = "Discord Config",
            description = "",
            position = 2
    )
    String discord_config = "discord_config";

    @ConfigItem(
            keyName = "alert_mode",
            name = "Alert Mode",
            description = "",
            position = 0,
            section = telemetry_settings
    )
    default AlertMode alert_mode()
    {
        return AlertMode.EVERYCHAT;
    }

    @ConfigItem(
            keyName = "keywords",
            name = "Key Words",
            description = "",
            position = 1,
            section = telemetry_settings
    )
    default String keywords()
    {
        return "";
    }

    @ConfigItem(
            keyName = "FilterProfanity",
            name = "Profanity Filter",
            description = "",
            position = 2,
            section = telemetry_settings
    )
    default boolean profanityFilter()
    {
        return false;
    }

    @ConfigItem(
            keyName = "alert_level_up",
            name = "Level Up Alerts",
            description = "",
            position = 3,
            section = telemetry_settings
    )
    default boolean alert_level_up() {
        return false;
    }

    @ConfigItem(
            keyName = "alert_sound",
            name = "Play Sound",
            description = "",
            position = 0,
            section = alert_mediums
    )
    default boolean alert_sound() {
        return false;
    }

    @ConfigItem(
            keyName = "alert_win_notification",
            name = "Windows Notification",
            description = "",
            position = 1,
            section = alert_mediums
    )
    default boolean alert_win_notification() {
        return false;
    }

    @ConfigItem(
            keyName = "alert_discord",
            name = "Discord Alerts",
            description = "",
            position = 2,
            section = alert_mediums
    )
    default boolean alert_discord() {
        return false;
    }

    @ConfigItem(
            keyName = "alert_logs",
            name = "Keep Text Logs",
            description = "",
            position = 3,
            section = alert_mediums
    )
    default boolean alert_logs() {
        return false;
    }

    @ConfigItem(
            keyName = "token",
            name = "Discord Bot Token",
            description = "",
            position = 0,
            secret = true,
            section = discord_config
    )
    default String token()
    {
        return "";
    }

    @ConfigItem(
            keyName = "Master UID",
            name = "Your discord UID",
            description = "",
            position = 1,
            section = discord_config
    )
    default String uid() {
        return "";
    }

    @ConfigItem(
            keyName = "discordtoken",
            name = "Start Discord Response Bot",
            description = "",
            position = 2,
            section = discord_config
    )
    default Button discordtoken() {
        return new Button();
    }

    @ConfigItem(
            keyName = "clear",
            name = "Clear Discord Log Chanel",
            description = "",
            position = 3,
            section = discord_config
    )
    default Button clear() {
        return new Button();
    }

    @ConfigItem(
            keyName = "invite",
            name = "Copy Bot Invite to clipboard",
            description = "",
            position = 4,
            section = discord_config
    )
    default Button invite() {
        return new Button();
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