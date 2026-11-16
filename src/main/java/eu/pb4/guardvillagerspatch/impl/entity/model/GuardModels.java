package eu.pb4.guardvillagerspatch.impl.entity.model;

import dev.sterner.guardvillagers.GuardVillagers;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.PolyModelInstance;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.EntityModel;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.LayerDefinition;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The guard model, once per variant.
 *
 * <p>Each instance carries its own texture, and generating its assets writes the item models the
 * client draws. The textures are the mod's own 128x128 guard atlas, which the pack already copies.
 */
public interface GuardModels {
    int VARIANTS = 7;

    /** Every instance, so the resource pack can generate the item models for all of them. */
    List<PolyModelInstance<?>> ALL = new ArrayList<>();

    LayerDefinition LAYER = GuardVillagerModel.createBodyLayer();

    Map<Integer, PolyModelInstance<GuardVillagerModel>> BY_VARIANT = Util.make(new HashMap<>(), models -> {
        for (int variant = 0; variant < VARIANTS; variant++) {
            models.put(variant, create(GuardVillagerModel::new, LAYER,
                    GuardVillagers.id("entity/guard/guard_" + variant)));
        }
    });

    static PolyModelInstance<GuardVillagerModel> get(int variant) {
        var model = BY_VARIANT.get(Math.max(0, Math.min(VARIANTS - 1, variant)));
        return model != null ? model : BY_VARIANT.get(0);
    }

    static <T extends EntityModel<?>> PolyModelInstance<T> create(Function<ModelPart, T> modelCreator,
                                                                 LayerDefinition layer, Identifier texture) {
        var instance = PolyModelInstance.create(modelCreator, layer, texture);
        ALL.add(instance);
        return instance;
    }
}
