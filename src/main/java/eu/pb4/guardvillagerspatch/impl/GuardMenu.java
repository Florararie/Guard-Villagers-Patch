package eu.pb4.guardvillagerspatch.impl;

import dev.sterner.guardvillagers.GuardVillagers;
import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

/**
 * The guard inventory, in a chest screen vanilla clients can open.
 *
 * <p>The single row holds the six guard slots followed by the mod screen's two buttons and a
 * status readout, so every visible slot does something: helmet, chestplate, leggings, boots,
 * offhand, main hand, follow toggle, patrol toggle and the guard's health and armour.
 */
public class GuardMenu extends AbstractContainerMenu {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private static final int GUARD_SLOTS = 6;
    private static final int FOLLOW_SLOT = 6;
    private static final int PATROL_SLOT = 7;
    private static final int STATUS_SLOT = 8;
    private static final int BUTTONS = 3;
    private static final int PLAYER_START = 9;
    private static final int PLAYER_END = 45;

    private final GuardEntity guard;
    private final Container guardInventory;
    private final Player player;
    private final SimpleContainer buttons = new SimpleContainer(BUTTONS);

    public GuardMenu(int syncId, Inventory playerInventory, GuardEntity guard) {
        this(syncId, playerInventory, guard.guardInventory, guard);
    }

    public GuardMenu(int syncId, Inventory playerInventory, Container guardInventory, GuardEntity guard) {
        super(MenuType.GENERIC_9x1, syncId);
        this.guard = guard;
        this.guardInventory = guardInventory;
        this.player = playerInventory.player;
        guardInventory.startOpen(this.player);

        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            this.addSlot(new GuardSlot(i, 8 + i * 18, ARMOR_SLOTS[i]));
        }
        this.addSlot(new GuardSlot(4, 8 + 4 * 18, EquipmentSlot.OFFHAND));
        this.addSlot(new GuardSlot(5, 8 + 5 * 18, EquipmentSlot.MAINHAND));

        for (int i = 0; i < BUTTONS; i++) {
            this.addSlot(new Slot(this.buttons, i, 8 + (GUARD_SLOTS + i) * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 30 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 88));
        }

        this.refreshButtons();
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput clickType, Player player) {
        if (slotIndex == FOLLOW_SLOT) {
            this.guard.setFollowing(!this.guard.isFollowing());
            this.guard.setOwnerId(player.getUUID());
            this.guard.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            this.refreshButtons();
            // The client predicted picking the button up; send the row back.
            this.broadcastChanges();
            return;
        }

        if (slotIndex == PATROL_SLOT) {
            if (!this.guard.isPatrolling()) {
                this.guard.setPatrolPos(this.guard.blockPosition());
            }
            this.guard.setPatrolling(!this.guard.isPatrolling());
            this.guard.setOwnerId(player.getUUID());
            this.guard.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            this.refreshButtons();
            this.broadcastChanges();
            return;
        }

        if (slotIndex == STATUS_SLOT) {
            this.broadcastChanges();
            return;
        }

        super.clicked(slotIndex, button, clickType, player);
    }

    @Override
    public void broadcastChanges() {
        // Health, armour and the toggle states can change while the screen is open.
        this.refreshButtons();
        super.broadcastChanges();
    }

    private void refreshButtons() {
        boolean following = this.guard.isFollowing();
        boolean patrolling = this.guard.isPatrolling();

        this.buttons.setItem(FOLLOW_SLOT - GUARD_SLOTS, icon(Items.LEAD,
                following ? GuardIcons.FOLLOWING : GuardIcons.STAYING,
                text(following ? "Following" : "Staying",
                        following ? ChatFormatting.GREEN : ChatFormatting.GRAY),
                text("Click to toggle", ChatFormatting.DARK_GRAY)));

        this.buttons.setItem(PATROL_SLOT - GUARD_SLOTS, icon(Items.COMPASS,
                patrolling ? GuardIcons.PATROLLING : GuardIcons.NOT_PATROLLING,
                text(patrolling ? "Patrolling" : "Not patrolling",
                        patrolling ? ChatFormatting.AQUA : ChatFormatting.GRAY),
                text("Click to toggle", ChatFormatting.DARK_GRAY)));

        // The mod screen draws hearts and an armour row; a chest screen has one slot for both, so
        // the heart carries the numbers and colours itself by how hurt the guard is.
        double health = this.guard.getHealth();
        double maxHealth = Math.max(1.0F, this.guard.getMaxHealth());
        double ratio = health / maxHealth;
        var healthColor = ratio >= 0.75 ? ChatFormatting.GREEN
                : ratio >= 0.35 ? ChatFormatting.YELLOW : ChatFormatting.RED;

        this.buttons.setItem(STATUS_SLOT - GUARD_SLOTS, icon(Items.GOLDEN_APPLE, GuardIcons.HEALTH,
                Component.translatable("guardinventory.health",
                                Component.literal(Math.round(health) + " / " + Math.round(maxHealth)))
                        .withStyle(style -> style.withItalic(false).withColor(healthColor)),
                Component.translatable("guardinventory.armor", Component.literal(String.valueOf(this.guard.getArmorValue())))
                        .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY)),
                text(following ? "Following you" : "Staying in place", ChatFormatting.DARK_GRAY)));
    }

    private static ItemStack icon(net.minecraft.world.item.Item item, Identifier model,
                                  Component name, Component... lore) {
        var stack = new ItemStack(item);
        // The client draws the item the component names, so the slot shows the mod's button art
        // instead of whatever item a server-side stack had to be made of.
        stack.set(DataComponents.ITEM_MODEL, model);
        stack.set(DataComponents.CUSTOM_NAME, name);
        stack.set(DataComponents.LORE, new ItemLore(List.of(lore)));
        return stack;
    }

    private static Component text(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(style -> style.withItalic(false).withColor(color));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        if (index >= GUARD_SLOTS && index < PLAYER_START) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < GUARD_SLOTS) {
            if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_START) {
            int target = this.findGuardSlotFor(stack);
            if (target < 0 || !this.moveItemStackTo(stack, target, target + 1, false)) {
                if (index < PLAYER_START + 27) {
                    if (!this.moveItemStackTo(stack, PLAYER_START + 27, PLAYER_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_START + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }

    private int findGuardSlotFor(ItemStack stack) {
        for (int i = 0; i < GUARD_SLOTS; i++) {
            Slot slot = this.slots.get(i);
            if (slot.mayPlace(stack) && !slot.hasItem()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.guard.isAlive() && this.guard.distanceTo(player) < 8.0F;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.guardInventory.stopOpen(player);
        this.guard.interacting = false;
    }

    private class GuardSlot extends Slot {
        private final int guardSlot;
        private final EquipmentSlot equipmentSlot;

        private GuardSlot(int guardSlot, int x, EquipmentSlot equipmentSlot) {
            super(GuardMenu.this.guardInventory, guardSlot, x, 18);
            this.guardSlot = guardSlot;
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (this.guardSlot < ARMOR_SLOTS.length && GuardMenu.this.guard.getEquipmentSlotForItem(stack) != this.equipmentSlot) {
                return false;
            }
            return GuardVillagers.hotvChecker(GuardMenu.this.player, GuardMenu.this.guard);
        }

        @Override
        public int getMaxStackSize() {
            return this.guardSlot < ARMOR_SLOTS.length ? 1 : 64;
        }

        @Override
        public void setByPlayer(ItemStack stack) {
            super.setByPlayer(stack);
            GuardMenu.this.guard.setItemSlot(this.equipmentSlot, stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return GuardVillagers.hotvChecker(player, GuardMenu.this.guard);
        }
    }
}
