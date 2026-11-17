package eu.pb4.guardvillagerspatch.impl.entity.model;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.PolyModelInstance;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.LayerDefinition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The armor shells a guard can wear, one model per material.
 *
 * <p>An item display paints a single texture, so armor cannot borrow the texture from the piece the
 * guard is actually wearing the way the mod's armor layer does. Instead each material gets its own
 * copy of the shell, drawn from vanilla's own armor texture, and the holder picks the one matching
 * the equippable asset of the worn piece.
 *
 * <p>Only vanilla's humanoid armor materials are built. That is what the mod's own loot tables hand
 * out, and it is what can be resolved here: the asset id an item declares is the name of its
 * texture folder, so vanilla materials are known at pack build time while an arbitrary modded one
 * is not. A piece made of anything else is left undrawn rather than painted with a wrong texture.
 */
public final class GuardArmorModels {
    /** The humanoid materials vanilla ships a layer texture for. */
    private static final List<String> MATERIALS =
            List.of("leather", "chainmail", "copper", "iron", "gold", "diamond", "netherite", "turtle_scute");
    /** The ones of those that also have the slimmer leggings layer; turtle shell is a helmet only. */
    private static final List<String> LEGGINGS =
            List.of("leather", "chainmail", "copper", "iron", "gold", "diamond", "netherite");

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static final LayerDefinition OUTER = GuardArmorModel.outerLayer();
    private static final LayerDefinition INNER = GuardArmorModel.innerLayer();

    /** Every instance, so the resource pack can generate the item models for all of them. */
    public static final List<PolyModelInstance<GuardArmorModel>> ALL = new ArrayList<>();
    private static final Map<Identifier, PolyModelInstance<GuardArmorModel>> BY_TEXTURE = new LinkedHashMap<>();

    static {
        for (var material : MATERIALS) {
            add(texture("humanoid", material), false);
            if (LEGGINGS.contains(material)) {
                add(texture("humanoid_leggings", material), true);
            }
        }
    }

    private GuardArmorModels() {}

    private static void add(Identifier texture, boolean leggings) {
        var instance = PolyModelInstance.create(GuardArmorModel::new, leggings ? INNER : OUTER, texture);
        BY_TEXTURE.put(texture, instance);
        ALL.add(instance);
    }

    private static Identifier texture(String layer, String material) {
        return Identifier.fromNamespaceAndPath("minecraft", "entity/equipment/" + layer + "/" + material);
    }

    /**
     * Fills {@code out} with the shells to draw this tick, each one already showing only the parts
     * its slot covers. A guard can wear four materials at once, so the list is per tick rather than
     * one model per guard.
     */
    public static void collect(GuardEntity guard, List<PolyModelInstance<GuardArmorModel>> out) {
        out.clear();

        for (var slot : SLOTS) {
            var instance = instance(guard.getItemBySlot(slot), slot);
            if (instance == null || out.contains(instance)) {
                continue;
            }
            instance.model().hideAll();
            out.add(instance);
        }

        for (var slot : SLOTS) {
            var instance = instance(guard.getItemBySlot(slot), slot);
            if (instance != null) {
                instance.model().show(slot);
            }
        }
    }

    @Nullable
    private static PolyModelInstance<GuardArmorModel> instance(ItemStack stack, EquipmentSlot slot) {
        var id = texture(stack, slot);
        return id == null ? null : BY_TEXTURE.get(id);
    }

    /**
     * The armor texture the mod's armor layer would draw for this piece, or null when it would draw
     * nothing. The equippable asset id is the name of the texture folder, and leggings are the one
     * slot drawn from the second layer.
     */
    @Nullable
    public static Identifier texture(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) {
            return null;
        }

        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != slot || equippable.assetId().isEmpty()) {
            return null;
        }

        var asset = equippable.assetId().get().identifier();
        if (!asset.getNamespace().equals("minecraft")) {
            return null;
        }

        boolean leggings = slot == EquipmentSlot.LEGS;
        var material = asset.getPath();
        if (!MATERIALS.contains(material) || (leggings && !LEGGINGS.contains(material))) {
            return null;
        }

        return texture(leggings ? "humanoid_leggings" : "humanoid", material);
    }
}
