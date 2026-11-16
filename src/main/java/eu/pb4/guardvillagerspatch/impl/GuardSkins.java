package eu.pb4.guardvillagerspatch.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Guard skins, shipped through the Polymer resource pack.
 *
 * <p>Guards are shown as mannequins, so their skin is a regular player skin. The mod's own guard
 * atlas ({@code textures/entity/guard/guard_N.png}) is a humanoid layout, so those files are
 * remapped to the vanilla 64x64 player skin layout and shipped in our namespace; the client then
 * uses them as a local asset instead of fetching a skin from Mojang.
 */
public final class GuardSkins {
    public static final String MOD_ID = "guardvillagerspatch";
    public static final int VARIANT_COUNT = 7;
    private static final Logger LOGGER = LoggerFactory.getLogger("guardvillagers-polymer-patch");

    /** Vanilla only exposes profiles without a skin patch, so the constructor is opened once. */
    private static final Constructor<ResolvableProfile.Static> PROFILE_CONSTRUCTOR = findConstructor();

    private static final ResolvableProfile[] PROFILES = new ResolvableProfile[VARIANT_COUNT];

    private GuardSkins() {}

    public static String textureName(int variant) {
        return "entity/guard/guard_" + clamp(variant);
    }

    /** Destination path inside our resource pack. */
    public static String packPath(int variant) {
        return "assets/" + MOD_ID + "/textures/" + textureName(variant) + ".png";
    }

    public static ResolvableProfile profile(int variant) {
        int index = clamp(variant);
        var profile = PROFILES[index];
        if (profile == null) {
            profile = createProfile(index);
            PROFILES[index] = profile;
        }

        return profile;
    }

    private static ResolvableProfile createProfile(int variant) {
        var gameProfile = new GameProfile(
                UUID.nameUUIDFromBytes(("guardvillagers-" + variant).getBytes(StandardCharsets.UTF_8)), "Guard");

        if (PROFILE_CONSTRUCTOR == null) {
            // Without a skin patch the mannequin still works, it just uses a default player skin.
            return ResolvableProfile.createResolved(gameProfile);
        }

        var texture = new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath(MOD_ID, textureName(variant)));
        var patch = PlayerSkin.Patch.create(Optional.of(texture), Optional.empty(), Optional.empty(),
                Optional.of(PlayerModelType.WIDE));

        try {
            return PROFILE_CONSTRUCTOR.newInstance(Either.left(gameProfile), patch);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Could not attach the guard skin, using a default player skin instead", e);
            return ResolvableProfile.createResolved(gameProfile);
        }
    }

    private static Constructor<ResolvableProfile.Static> findConstructor() {
        try {
            var constructor = ResolvableProfile.Static.class.getDeclaredConstructor(Either.class, PlayerSkin.Patch.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Could not access the profile constructor, guards will use default player skins", e);
            return null;
        }
    }

    private static int clamp(int variant) {
        return Math.max(0, Math.min(VARIANT_COUNT - 1, variant));
    }
}
