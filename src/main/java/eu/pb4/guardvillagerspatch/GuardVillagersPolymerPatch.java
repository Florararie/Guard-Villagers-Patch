package eu.pb4.guardvillagerspatch;

import dev.sterner.guardvillagers.GuardVillagers;
import eu.pb4.guardvillagerspatch.impl.GuardPolymerEntity;
import eu.pb4.guardvillagerspatch.impl.GuardResourcePack;
import eu.pb4.guardvillagerspatch.impl.GuardSpawnEggItem;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import net.fabricmc.api.ModInitializer;
import net.minecraft.sounds.SoundEvents;

public class GuardVillagersPolymerPatch implements ModInitializer {

    @Override
    public void onInitialize() {
        GuardResourcePack.setup();
        PolymerEntityUtils.registerOverlay(GuardVillagers.GUARD_VILLAGER, GuardPolymerEntity::new);
        PolymerItemUtils.registerOverlay(GuardVillagers.GUARD_SPAWN_EGG, new GuardSpawnEggItem(GuardVillagers.GUARD_SPAWN_EGG));

        PolymerSoundEvent.registerOverlay(GuardVillagers.GUARD_AMBIENT, SoundEvents.VILLAGER_AMBIENT);
        PolymerSoundEvent.registerOverlay(GuardVillagers.GUARD_HURT, SoundEvents.VILLAGER_HURT);
        PolymerSoundEvent.registerOverlay(GuardVillagers.GUARD_DEATH, SoundEvents.VILLAGER_DEATH);
    }
}
