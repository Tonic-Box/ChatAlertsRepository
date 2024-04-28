package net.runelite.client.plugins.chatalerts;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.annotations.Component;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.chatalerts.util.*;
import net.runelite.client.plugins.chatalerts.types.Pair;
import net.runelite.client.ui.DrawManager;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Extension
@PluginDescriptor(name = "Chat Alerts", description = "Discord OSRS Account Monitor", tags = {"TonicBox", "discord", "monitor", "alert"})
public class ChatAlertsPlugin extends Plugin
{
    @Inject
    private ChatAlertsConfig config;
    @Inject
    private DrawManager drawManager;
    private final Pattern LEVEL_UP_PATTERN = Pattern.compile(".*Your ([a-zA-Z]+) (?:level is|are)? now (\\d+)\\.");
    private String logs_dir;
    private String current_log_file;
    private Discord discord;
    private boolean shouldProcessLevelUp = false;

    @Override
    protected void startUp() {
        try {
            Files.createDirectories(Paths.get(RuneLite.RUNELITE_DIR.toString() + "\\ChatAlertLogs\\"));
        }
        catch(Exception ignored) { }
        this.logs_dir = RuneLite.RUNELITE_DIR.toString() + "\\ChatAlertLogs\\";
        String fileName = new SimpleDateFormat("yyyy-MM-dd'.txt'").format(new Date());
        try {Files.createFile(Paths.get(this.logs_dir + fileName));} catch(Exception ignored) {}
        this.current_log_file = this.logs_dir + fileName;
        ProfanityFilter.loadConfigs();
    }

    @Override
    protected void shutDown() {
        discord.shutdown();
        Threads.shutdown();
    }

    @Provides
    ChatAlertsConfig provideConfig(ConfigManager configManager) {
        return (ChatAlertsConfig) configManager.getConfig(ChatAlertsConfig.class);
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if(event.getType() != ChatMessageType.PUBLICCHAT)
            return;
        if (Objects.requireNonNull(Static.getClient().getLocalPlayer()).getName() == null)
            return;


        //log alert
        if(config.alert_logs()) {
            Threads.submit(new Runnable()
            {
                @SneakyThrows
                public void run()
                {
                    String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getTimestamp());
                    String text = "[" + ts + "] " + event.getName() + ": " + event.getMessage() + "\n";
                    Writer fileWriter = new FileWriter(current_log_file, true);
                    fileWriter.write(text);
                    fileWriter.close();
                }
            });
        }

        if(Text.standardize(event.getName()).equalsIgnoreCase(Text.standardize(Static.getClient().getLocalPlayer().getName())))
            return;

        if(config.alert_mode() == ChatAlertsConfig.AlertMode.KEYWORDSONLY) {
            if(config.keywords().isEmpty())
                return;
            String[] keywords = config.keywords().split(",");
            if(!containsFromArray(event.getMessage(), keywords)) {
                return;
            }
        }

        //Sound alert
        if(config.alert_sound()) {
            Threads.submit(new Runnable()
            {
                @SneakyThrows
                public void run()
                {
                    Sound.tone(392,500, 0.2);
                }
            });
        }

        //Windows Notification alert
        if(config.alert_win_notification()) {
            Threads.submit(new Runnable()
            {
                @SneakyThrows
                public void run()
                {
                    displayTray(event.getName(), event.getMessage());
                }
            });
        }

        if(config.alert_discord()) {
            if(discord != null)
            {
                discord.sendChat(event.getName(), event.getMessage());
            }
        }
    }

    public boolean containsFromArray(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }

    public void displayTray(String rsn, String text) throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon trayIcon = new TrayIcon(image, "OSRS Chat Alert");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("OSRS Chat Alert");
        tray.add(trayIcon);
        trayIcon.displayMessage("Chat Alert: " + rsn, text, TrayIcon.MessageType.INFO);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!config.alert_level_up() || !shouldProcessLevelUp) {
            return;
        }
        shouldProcessLevelUp = false;

        Pair<String,String> levelUp = null;
        if (Static.getClient().getWidget(ComponentID.LEVEL_UP_LEVEL) != null)
        {
            levelUp = parseLevelUpWidget(ComponentID.LEVEL_UP_LEVEL);
        }
        else if (Static.getClient().getWidget(ComponentID.DIALOG_SPRITE_TEXT) != null)
        {
            String text = Static.getClient().getWidget(ComponentID.DIALOG_SPRITE_TEXT).getText();
            if (!Text.removeTags(text).contains("High level gamble"))
            {
                levelUp = parseLevelUpWidget(ComponentID.DIALOG_SPRITE_TEXT);
            }
        }

        if(levelUp == null)
            return;

        String skill = levelUp.getKey();
        String level = levelUp.getValue();
        //log alert
        if(config.alert_logs()) {
            Threads.submit(new Runnable()
            {
                @SneakyThrows
                public void run()
                {
                    String text = "[" + Api.timeStampFull() + "] " + "Level Up (" + skill + "): " + level +  "\n";
                    Writer fileWriter = new FileWriter(current_log_file, true);
                    fileWriter.write(text);
                    fileWriter.close();
                }
            });
        }

        //Sound alert
        if(config.alert_sound()) {
            Threads.submit(new Runnable()
            {
                @SneakyThrows
                public void run()
                {
                    Sound.tone(392,500, 0.2);
                }
            });
        }

        //Windows Notification alert
        if(config.alert_win_notification()) {
            Threads.submit(new Runnable()
            {
                @SneakyThrows
                public void run()
                {
                    displayTray("Level Up", "" + skill + ": " + level);
                }
            });
        }

        //Discord alert
        if(config.alert_discord()) {
            if(discord != null)
            {
                Threads.submit(new Runnable()
                {
                    @SneakyThrows
                    public void run()
                    {
                        Consumer<Image> imageCallback = (img) -> discord.sendChat2("[Level Up(" + skill + ")]", level, img);
                        drawManager.requestNextFrameListener(imageCallback);
                    }
                });
            }
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if (event.getGroupId() == InterfaceID.LEVEL_UP)
        {
            shouldProcessLevelUp = true;
        }
    }

    private Pair<String,String> parseLevelUpWidget(@Component int levelUpLevel)
    {
        Widget levelChild = Static.getClient().getWidget(levelUpLevel);
        if (levelChild == null)
        {
            return null;
        }

        Matcher m = LEVEL_UP_PATTERN.matcher(levelChild.getText());
        if (!m.matches())
        {
            return null;
        }

        String skillName = m.group(1);
        String skillLevel = m.group(2);
        return new Pair<>(skillName, skillLevel);
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("ChatAlerts"))
            return;

        switch (configButtonClicked.getKey()) {
            case "logFolder":
                Threads.submit(new Runnable() {
                    @SneakyThrows
                    public void run() {
                        Desktop.getDesktop().open(new File(logs_dir));
                    }
                });
                break;
            case "discordtoken":
                if(this.discord != null)
                    return;
                this.discord = new Discord(config, drawManager);
                break;
            case "invite":
                if(this.discord == null)
                    return;
                StringSelection selection = new StringSelection(this.discord.getInvite());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                break;
            case "clear":
                if(this.discord == null)
                    return;
                discord.clearChanel();
                break;
        }
    }
}
