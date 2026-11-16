package eu.pb4.guardvillagerspatch.impl;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;

/**
 * Shows a guard to vanilla clients as a mannequin.
 *
 * <p>Mannequins render a player skin and their equipment, so a guard looks like an armed, armoured
 * villager guard instead of an ordinary villager: the pack gives it the mod's own guard skin for
 * its variant, and the helmet, chestplate, leggings, boots, shield and weapon it carries are
 * visible in the world.
 */
public class GuardPolymerEntity implements PolymerEntity {
    /** First tracked data index the guard adds on top of the vanilla mob ones. */
    private static final int GUARD_DATA_START = 16;

    private static final int NOT_COMPUTED = -2;
    private static int profileIndex = NOT_COMPUTED;
    private static int descriptionIndex = NOT_COMPUTED;

    private final GuardEntity guard;

    public GuardPolymerEntity(GuardEntity guard) {
        this.guard = guard;
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.MANNEQUIN;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        // The guard's own fields sit on indices a mannequin uses for its own data, so drop them.
        data.removeIf(value -> value.id() >= GUARD_DATA_START);

        int profile = profileIndex();
        if (profile >= 0) {
            data.add(new SynchedEntityData.DataValue<>(profile, EntityDataSerializers.RESOLVABLE_PROFILE,
                    GuardSkins.profile(this.guard.getGuardVariant())));
        }

        // Mannequins draw a description label under themselves unless it is cleared.
        int description = descriptionIndex();
        if (description >= 0) {
            data.add(new SynchedEntityData.DataValue<>(description, EntityDataSerializers.OPTIONAL_COMPONENT,
                    Optional.<Component>empty()));
        }
    }

    private static int profileIndex() {
        if (profileIndex == NOT_COMPUTED) {
            profileIndex = findIndex(EntityDataSerializers.RESOLVABLE_PROFILE);
        }
        return profileIndex;
    }

    private static int descriptionIndex() {
        if (descriptionIndex == NOT_COMPUTED) {
            descriptionIndex = findIndex(EntityDataSerializers.OPTIONAL_COMPONENT);
        }
        return descriptionIndex;
    }

    private static int findIndex(net.minecraft.network.syncher.EntityDataSerializer<?> serializer) {
        var defaults = PolymerEntityUtils.getDefaultSynchedEntityData(EntityType.MANNEQUIN);
        if (defaults != null) {
            for (int i = 0; i < defaults.length; i++) {
                var item = defaults[i];
                if (item != null && item.getAccessor().serializer() == serializer) {
                    return i;
                }
            }
        }

        return -1;
    }
}
