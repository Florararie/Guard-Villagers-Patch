package eu.pb4.guardvillagerspatch.impl;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * Renders the guard spawn egg as the vanilla villager one.
 */
public class GuardSpawnEggItem implements PolymerItem {

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.VILLAGER_SPAWN_EGG;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        // Use the villager spawn egg model instead of the mod's own item model.
        return null;
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        out.set(DataComponents.ITEM_NAME, Component.literal("Guard Spawn Egg"));
    }
}
