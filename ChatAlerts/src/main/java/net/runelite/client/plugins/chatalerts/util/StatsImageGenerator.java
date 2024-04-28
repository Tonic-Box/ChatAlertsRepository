package net.runelite.client.plugins.chatalerts.util;

import net.runelite.api.Skill;
import net.runelite.client.plugins.chatalerts.ChatAlertsPlugin;
import net.runelite.client.util.ImageUtil;
import net.unethicalite.client.Static;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static net.runelite.api.Skill.*;
public class StatsImageGenerator
{
    private static final List<Skill> skills = new ArrayList<>()
    {{
        add(ATTACK);
        add(HITPOINTS);
        add(MINING);
        add(STRENGTH);
        add(AGILITY);
        add(SMITHING);
        add(DEFENCE);
        add(HERBLORE);
        add(FISHING);
        add(RANGED);
        add(THIEVING);
        add(COOKING);
        add(PRAYER);
        add(CRAFTING);
        add(FIREMAKING);
        add(MAGIC);
        add(FLETCHING);
        add(WOODCUTTING);
        add(RUNECRAFT);
        add(SLAYER);
        add(FARMING);
        add(CONSTRUCTION);
        add(HUNTER);
    }};

    public static BufferedImage generate() throws Exception {
        // Load base image
        final BufferedImage img = ImageUtil.loadImageResource(ChatAlertsPlugin.class, "base.png");

        // Load font
        Graphics2D g2d = img.createGraphics();
        Font font = loadFont("font.ttf").deriveFont(10f);
        g2d.setFont(font);

        // Create color for the text
        Color yellow = new Color(255, 255, 51);
        g2d.setColor(yellow);

        int col = 0;
        int row = 0;
        int total = 0;
        for(Skill skill : skills) {
            if(col == 3) {
                col = 0;
                row++;
            }
            int real = Static.getClient().getRealSkillLevel(skill);
            total += real;
            int boosted = Static.getClient().getBoostedSkillLevel(skill);
            int xBase = 36 + (col * 62); // values[2] should be the column index
            int yBase = 15 + (row * 32); // values[3] should be the row index
            g2d.drawString(String.valueOf(boosted), xBase, yBase);
            g2d.drawString(String.valueOf(real), xBase + 13, yBase + 13);
            col++;
        }

        int len = String.valueOf(total).length();
        int offset = 7;
        if (len == 3) {
            offset = 3;
        }
        if (len == 4) {
            offset = 0;
        }

        g2d.drawString(String.valueOf(total), 146 + offset, 28+(7*32));

        g2d.dispose();
        return img;
    }

    private static Font loadFont(String path) throws Exception {
        InputStream is = ChatAlertsPlugin.class.getResourceAsStream(path);
        if (is == null) {
            throw new RuntimeException("Font file not found at " + path);
        }
        return Font.createFont(Font.TRUETYPE_FONT, is);
    }
}
