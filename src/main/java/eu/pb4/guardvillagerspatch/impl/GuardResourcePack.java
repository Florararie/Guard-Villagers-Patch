package eu.pb4.guardvillagerspatch.impl;

import eu.pb4.guardvillagerspatch.impl.entity.model.GuardModels;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.resourcepack.extras.api.format.atlas.AtlasAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.MapColorTintSource;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Builds the guard's model and texture assets into the Polymer resource pack.
 *
 * <p>The mod's own guard atlas is copied as-is, and each guard model variant writes the item models
 * the client draws the mesh from. This runs after the initial pack creation so the mod's assets are
 * already in the pack when the atlas is assembled.
 */
public final class GuardResourcePack {
    public static final String MOD_ID = "guardvillagerspatch";
    /** The patched mod, whose assets carry the guard textures and name. */
    public static final String TARGET_MOD_ID = "guardvillagers";
    private static final Logger LOGGER = LoggerFactory.getLogger("guardvillagers-polymer-patch");

    private GuardResourcePack() {}

    public static void setup() {
        // The guard's name comes from the mod's lang file, and that name is the title of the
        // inventory screen. Without it vanilla clients read "entity.guardvillagers.guard".
        if (PolymerResourcePackUtils.addModAssets(TARGET_MOD_ID)) {
            LOGGER.info("Registered Guard Villagers assets as a Polymer resource pack source.");
        } else {
            LOGGER.error("Could not read assets from {}, guard names and advancement titles will be missing.",
                    TARGET_MOD_ID);
        }

        // Each part is drawn as an item display whose model id is `<namespace>:-/<model path>`.
        // Polymer only writes the client item definitions behind those ids
        // (assets/<namespace>/items/-/...) for model folders registered as bridged, so without this
        // the client cannot resolve the part models and draws vanilla's missing item for every limb.
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                Identifier.fromNamespaceAndPath(TARGET_MOD_ID, "entity"),
                (model, builder) -> new ItemAsset(
                        // The map color tint is what makes the red hurt flash applied by the entity
                        // holder show up, instead of the component being silently ignored. It has to
                        // default to white, else every unflashed part would be tinted dark brown.
                        new BasicItemModel(model, List.of(new MapColorTintSource(0xFFFFFF))),
                        new ItemAsset.Properties(true, true)));

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(GuardResourcePack::build);
    }

    private static void build(ResourcePackBuilder builder) {
        var atlas = AtlasAsset.builder();
        for (var model : GuardModels.ALL) {
            model.generateAssets(builder::addData, atlas);
        }

        // The inventory screen's toggles and readouts are items, so their icons are item models too.
        // They follow the vanilla item conventions and need no atlas entries of their own.
        GuardIcons.generate(builder);

        // The guard is drawn from item models, and item models resolve their sprites from the item
        // atlas. Adding the same sprite to the block atlas as well makes the client log it as a
        // duplicate and drop the item atlas copy, which leaves every part untextured.
        builder.addData("assets/minecraft/atlases/items.json", atlas.build());
    }
}
