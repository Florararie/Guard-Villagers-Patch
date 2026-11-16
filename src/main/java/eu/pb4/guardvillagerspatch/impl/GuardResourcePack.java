package eu.pb4.guardvillagerspatch.impl;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Adds our bundled guard skins to the Polymer resource pack.
 *
 * <p>The skins live in this mod at {@code assets/guardvillagerspatch/textures/entity/guard/},
 * converted from the Guard Villagers guard atlas to the player skin layout.
 */
public final class GuardResourcePack {
    public static final String MOD_ID = GuardSkins.MOD_ID;
    private static final Logger LOGGER = LoggerFactory.getLogger("guardvillagers-polymer-patch");

    private GuardResourcePack() {}

    public static void setup() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(GuardResourcePack::build);
    }

    private static void build(ResourcePackBuilder builder) {
        for (int variant = 0; variant < GuardSkins.VARIANT_COUNT; variant++) {
            var path = GuardSkins.packPath(variant);
            var source = GuardResourcePack.class.getResourceAsStream("/" + path);
            if (source == null) {
                LOGGER.warn("Guard skin {} is missing from this jar; that variant will use a default skin", path);
                continue;
            }

            try (source) {
                builder.addData(path, source.readAllBytes());
            } catch (IOException e) {
                LOGGER.warn("Could not add guard skin {}", path, e);
            }
        }
    }
}
