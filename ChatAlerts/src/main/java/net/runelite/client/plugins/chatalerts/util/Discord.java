package net.runelite.client.plugins.chatalerts.util;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.chatalerts.ChatAlertsConfig;
import net.runelite.client.ui.DrawManager;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.client.Static;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class Discord {
    private final DiscordApi dapi;
    private long channel = -1;
    private long selfId = -1;
    private final Class<?> animationID;
    public Discord(ChatAlertsConfig config, DrawManager drawManager)
    {
        this.animationID = AnimationID.class;
        dapi = new DiscordApiBuilder()
                .setToken(config.token())
                .addIntents(Intent.MESSAGE_CONTENT)
                .login().join();

        dapi.addMessageCreateListener(event -> {
            if(config.uid().equals(event.getMessage().getAuthor().getId()+""))
            {
                if (event.getMessageContent().toLowerCase().startsWith("!chatalert"))
                {
                    channel = event.getChannel().getId();
                    event.getChannel().sendMessage("Locked to channel!");
                    return;
                }
            }

            if(selfId == -1 && event.getMessageContent().equals("Locked to channel!"))
            {
                selfId = event.getMessage().getAuthor().getId();
                return;
            }

            if(selfId == -1 || channel == -1)
            {
                return;
            }

            if(event.getChannel().getId() == channel && event.getMessage().getAuthor().getId() != selfId)
            {
                if (event.getMessageContent().toLowerCase().startsWith("!help"))
                {
                    event.getChannel().sendMessage(
                            "**!help** - this message\n" +
                                    "**!status** - display general stats about the accounts running state\n" +
                                    "**!pic** - send a screenshot\n" +
                                    "**!stats** - get current stats\n" +
                                    "**!players:** return a list of nearby players by rsn\n" +
                                    "**!say:** say stuff in game\n\n" +
                                    "**The below commands can only be issued by the bot owner:**\n" +
                                    "**!logout:** log your account out\n" +
                                    "**!chatalert:** run this in the channel you want to lock your discord bot to, or to move it to a new channel"
                    );
                }

                if(!Static.getClient().getGameState().equals(GameState.LOADING) && !Static.getClient().getGameState().equals(GameState.LOGGED_IN))
                    return;


                if(config.uid().equals(event.getMessage().getAuthor().getId()+""))
                {
                    if (event.getMessageContent().toLowerCase().startsWith("!logout"))
                    {
                        Api.invoke(() -> {
                            Widget w = Static.getClient().getWidget(182, 8);
                            if(w != null)
                            {
                                Game.logout();
                            }
                        });
                        return;
                    }
                }

                if (event.getMessageContent().toLowerCase().startsWith("!status"))
                {
                    WorldPoint wp = Static.getClient().getLocalPlayer().getWorldLocation();
                    event.getChannel().sendMessage(
                            "**Location Type:** " + (
                                   (Static.getClient().isInInstancedRegion()) ? "Instance" : "Overworld"
                            ) + "\n" +
                            "**Location:** x=" + wp.getX() + ", y=" + wp.getY() + ", z=" + wp.getPlane() + "\n" +
                            "**State:** " + getState() + "\n" +
                            "**Interacting: **" + getInteracting()
                    );
                }
                else if (event.getMessageContent().toLowerCase().startsWith("!pic"))
                {
                    if(Static.getClient().isLowCpu())
                    {
                        event.getChannel().sendMessage("Cannot send screenshot while in headless mode");
                    }
                    Consumer<Image> imageCallback = (img) -> Threads.submit(() -> takePic(img));
                    drawManager.requestNextFrameListener(imageCallback);
                }
                else if (event.getMessageContent().toLowerCase().startsWith("!players"))
                {
                    String players = Api.invoke(() -> {
                        WorldPoint wp;
                        StringBuilder p = new StringBuilder();
                        for(Player player : Static.getClient().getPlayers())
                        {
                            if(player == null)
                                continue;
                            if (player == Static.getClient().getLocalPlayer())
                                continue;
                            wp = player.getWorldLocation();
                            int x1 = wp.getX() - 25;
                            int y1 = wp.getY() - 25;
                            int x2 = wp.getX() + 25;
                            int y2 = wp.getY() + 25;
                            if(wp.getX() > x1 && wp.getX() < x2 && wp.getY() > y1 && wp.getY() < y2) {
                                p.append(player.getName()).append("\n");
                            }
                        }
                        return p.toString();
                    });
                    event.getChannel().sendMessage(players);
                }
                else if (event.getMessageContent().toLowerCase().startsWith("!stats"))
                {
                    sendStats();
                }

                if (!event.getMessageContent().toLowerCase().startsWith("!say "))
                {
                    return;
                }
                String message = event.getMessageContent().substring(5);
                if(config.profanityFilter() && ProfanityFilter.isBad(message))
                {
                    event.getChannel().sendMessage("Message contains profanity, not sending.");
                    return;
                }
                Keyboard.type(event.getMessageContent().substring(5), true);
            }
        });
    }

    public void shutdown()
    {
        dapi.disconnect();
    }

    private String getState()
    {
        if(Static.getClient().getLocalPlayer() == null)
            return "None";
        String ret = "";
        if(Static.getClient().getLocalPlayer().isIdle())
        {
            ret += "Idle";
        }
        else if(Static.getClient().getLocalPlayer().isMoving())
        {
            ret += "Moving [`";
            int run = Api.invoke(() -> Static.getClient().getVarpValue(173));
            ret += ((run==1)?"Running - ":"Walking - ") + Static.getClient().getEnergy() + "%`]";
        }
        else
        {
            ret += "Animating [`" + fetchAnimation() + "`]";

        }
        return ret;
    }

    private String getInteracting()
    {
        if(Static.getClient().getLocalPlayer() == null)
            return "None";
        Actor actor = Static.getClient().getLocalPlayer().getInteracting();
        if(actor == null)
            return "None";
        if(actor instanceof NPC)
        {
            return "Npc: [`" + Text.sanitize((actor.getName()!=null?actor.getName():"null")) + "`]";
        }
        else if(actor instanceof Player)
        {
            return "Player: [`" + Text.sanitize((actor.getName()!=null?actor.getName():"null")) + "`]";
        }
        return "Unknown...";
    }

    private String fetchAnimation()
    {
        int id = Static.getClient().getLocalPlayer().getAnimation();
        try
        {
            Field[] fields = animationID.getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    if(id == ((int)field.get(animationID)))
                    {
                        return field.getName();
                    }
                }
                catch (Exception ignored) {}
            }
        }
        catch (Exception ex)
        {
            return id + "";
        }
        return id + "";
    }

    public void sendChat(String rsn, String message)
    {
        if(channel == -1)
            return;
        if(dapi.getChannelById(channel).isEmpty())
            return;
        if(dapi.getChannelById(channel).get().asTextChannel().isEmpty())
            return;
        new MessageBuilder()
                .setContent("**" + Api.sanitize(rsn) + ":** " + message)
                .send(dapi.getChannelById(channel).get().asTextChannel().get());
    }

    public void sendChat2(String Title, String message, Image image)
    {
        if(channel == -1)
            return;
        if(dapi.getChannelById(channel).isEmpty())
            return;
        if(dapi.getChannelById(channel).get().asTextChannel().isEmpty())
            return;

        BufferedImage screenshot = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = screenshot.getGraphics();
        graphics.drawImage(image, 0, 0, null);

        EmbedBuilder embed = new EmbedBuilder()
                .setImage(screenshot);

        new MessageBuilder()
                .setContent("**" + Title + "** " + message)
                .setEmbed(embed)
                .send(dapi.getChannelById(channel).get().asTextChannel().get());
    }

    public void clearChanel()
    {
        if(channel == -1)
            return;
        if(dapi.getChannelById(channel).isEmpty())
            return;
        if(dapi.getChannelById(channel).get().asTextChannel().isEmpty())
            return;
        for(Message message : dapi.getChannelById(channel).get().asTextChannel().get().getMessagesAsStream().toArray(Message[]::new))
        {
            message.delete();
        }
    }

    public String getInvite()
    {
        return dapi.createBotInvite();
    }

    public void sendStats()
    {
        if(channel == -1)
            return;
        if(dapi.getChannelById(channel).isEmpty())
            return;
        if(dapi.getChannelById(channel).get().asTextChannel().isEmpty())
            return;

        try
        {
            BufferedImage stats = StatsImageGenerator.generate();
            EmbedBuilder embed = new EmbedBuilder()
                    .setImage(stats);
            dapi.getChannelById(channel).get().asTextChannel().get().sendMessage(embed);
        }
        catch (Exception ignored) {}
    }

    private void takePic(Image image)
    {
        if(channel == -1)
            return;
        if(dapi.getChannelById(channel).isEmpty())
            return;
        if(dapi.getChannelById(channel).get().asTextChannel().isEmpty())
            return;

        BufferedImage screenshot = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = screenshot.getGraphics();
        graphics.drawImage(image, 0, 0, null);

        EmbedBuilder embed = new EmbedBuilder()
                .setImage(screenshot);

        dapi.getChannelById(channel).get().asTextChannel().get().sendMessage(embed);
    }
}