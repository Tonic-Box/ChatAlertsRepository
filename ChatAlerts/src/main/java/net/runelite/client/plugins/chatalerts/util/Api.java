package net.runelite.client.plugins.chatalerts.util;

import net.runelite.api.Client;
import net.unethicalite.client.Static;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Api
{
    private static final SimpleDateFormat DATE_FORMAT_FULL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMAT_SHORT = new SimpleDateFormat("HH:mm:ss");
    public static <T> T invoke(Supplier<T> supplier)
    {
        if (!Static.getClient().isClientThread())
        {
            CompletableFuture<T> future = new CompletableFuture<>();
            Runnable runnable = () -> future.complete(supplier.get());
            Static.getClientThread().invoke(runnable);
            return future.join();
        }
        else
        {
            return supplier.get();
        }
    }

    public static void invoke(Runnable runnable)
    {
        Static.getClientThread().invoke(runnable);
    }

    public static String timeStampFull()
    {
        return DATE_FORMAT_FULL.format(new Date());
    }

    public static String timeStampShort()
    {
        return DATE_FORMAT_SHORT.format(new Date());
    }

    /**
     * Cleans the ironman status icon from player name string if present and
     * corrects spaces.
     * @param text Player name to lookup.
     * @return Cleaned player name.
     */
    public static String sanitize(String text)
    {
        if(text == null)
        {
            return null;
        }
        String cleaned = text.replace('\u00A0', ' ').replace('_', ' ');
        return (cleaned.contains("<img") ? cleaned.substring(text.lastIndexOf('>') + 1) : cleaned).trim().replaceAll("<[^>]+>", "");
    }
}
