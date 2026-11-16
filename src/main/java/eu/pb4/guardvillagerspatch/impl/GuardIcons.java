package eu.pb4.guardvillagerspatch.impl;

import dev.sterner.guardvillagers.GuardVillagers;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * The icons the guard inventory draws where a real item would be.
 *
 * <p>A vanilla client can only draw item stacks in a container slot, so the mod screen's buttons and
 * readouts become items carrying a {@code minecraft:item_model} component. That lets the toggles use
 * the mod author's own button art instead of a lead and a compass.
 *
 * <p>Each icon is a plain 16x16 sprite under {@code textures/item}, which vanilla's own item atlas
 * already collects from every namespace, so nothing here has to touch the atlas by hand.
 */
public final class GuardIcons {
    public static final Identifier FOLLOWING = id("icon_following");
    public static final Identifier STAYING = id("icon_staying");
    public static final Identifier PATROLLING = id("icon_patrolling");
    public static final Identifier NOT_PATROLLING = id("icon_not_patrolling");
    public static final Identifier HEALTH = id("icon_health");

    private static final int SIZE = 16;

    /** Drawn here rather than copied, so the pack ships no Mojang art. */
    private static final String[] HEART = {
            ".XX..XX.",
            "XRRXXRRX",
            "XhRRRRRX",
            "XRRRRRRX",
            ".XRRRRX.",
            "..XRRX..",
            "...XX...",
            "........",
    };

    private GuardIcons() {}

    public static void generate(ResourcePackBuilder builder) {
        // The mod's own 20x18 button sprites, rescaled so they are not distorted in a 16x16 slot.
        fromModTexture(builder, "icon_following", "gui/sprites/following/following");
        fromModTexture(builder, "icon_staying", "gui/sprites/following/not_following");
        fromModTexture(builder, "icon_patrolling", "gui/sprites/patrolling/patrolling1");
        fromModTexture(builder, "icon_not_patrolling", "gui/sprites/patrolling/notpatrolling1");

        var heart = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int colour = switch (HEART[y].charAt(x)) {
                    case 'X' -> 0xFF1B1B1B;
                    case 'R' -> 0xFFD93A3A;
                    case 'h' -> 0xFFF07A7A;
                    default -> 0;
                };
                heart.setRGB(x, y, colour);
            }
        }
        write(builder, "icon_health", scale(heart));
    }

    /** Copies one of the patched mod's GUI sprites into this pack as a 16x16 item texture. */
    private static void fromModTexture(ResourcePackBuilder builder, String name, String path) {
        try (InputStream stream = GuardVillagers.class.getResourceAsStream("/assets/guardvillagers/textures/" + path + ".png")) {
            if (stream == null) {
                // Without the sprite the slot still shows its base item, so this is not fatal.
                System.err.println("[guardvillagers-polymer-patch] Missing guard GUI sprite " + path);
                return;
            }

            write(builder, name, scale(ImageIO.read(stream)));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read guard GUI sprite " + path, e);
        }
    }

    private static BufferedImage scale(BufferedImage source) {
        // Nearest neighbour keeps the pixel art crisp instead of smearing it.
        var scaled = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(source, 0, 0, SIZE, SIZE, null);
        graphics.dispose();
        return scaled;
    }

    private static void write(ResourcePackBuilder builder, String name, BufferedImage image) {
        var png = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", png);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not encode the icon " + name, e);
        }

        builder.addData("assets/" + GuardResourcePack.MOD_ID + "/textures/item/" + name + ".png", png.toByteArray());

        // The generated parent gives the icon the same 16x16 quad and GUI transform as a vanilla
        // item, which is what the earlier hand-built element model was missing.
        builder.addStringData("assets/" + GuardResourcePack.MOD_ID + "/models/item/" + name + ".json",
                "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\""
                        + GuardResourcePack.MOD_ID + ":item/" + name + "\"}}");

        builder.addStringData("assets/" + GuardResourcePack.MOD_ID + "/items/" + name + ".json",
                "{\"model\":{\"type\":\"minecraft:model\",\"model\":\""
                        + GuardResourcePack.MOD_ID + ":item/" + name + "\"}}");
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(GuardResourcePack.MOD_ID, path);
    }
}
