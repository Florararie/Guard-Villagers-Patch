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

    /**
     * Ids for the states a drawn bow, a racking crossbow and a raised shield are shown in.
     *
     * <p>{@code minecraft:item_model} names an item definition, not a model, so each of these needs a
     * file under {@code assets/guardvillagerspatch/items}. Vanilla's own definitions pick these
     * states with predicates that read the holding entity, which a display entity does not have.
     */
    public static final Identifier BOW_PULLING_0 = heldId("guard_bow_pulling_0");
    public static final Identifier BOW_PULLING_1 = heldId("guard_bow_pulling_1");
    public static final Identifier BOW_PULLING_2 = heldId("guard_bow_pulling_2");
    public static final Identifier CROSSBOW_PULLING_0 = heldId("guard_crossbow_pulling_0");
    public static final Identifier CROSSBOW_PULLING_1 = heldId("guard_crossbow_pulling_1");
    public static final Identifier CROSSBOW_PULLING_2 = heldId("guard_crossbow_pulling_2");
    public static final Identifier CROSSBOW_ARROW = heldId("guard_crossbow_arrow");
    public static final Identifier SHIELD_BLOCKING = heldId("guard_shield_blocking");

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

    /**
     * One item definition per held-weapon state, each pointing straight at the vanilla model whose
     * own predicate would have chosen it while held by a real entity.
     */
    private static void writeHeldItemModels(ResourcePackBuilder builder) {
        for (String state : new String[]{
                "bow_pulling_0", "bow_pulling_1", "bow_pulling_2",
                "crossbow_pulling_0", "crossbow_pulling_1", "crossbow_pulling_2",
                "crossbow_arrow"}) {
            builder.addStringData("assets/" + MOD_ID + "/items/guard_" + state + ".json",
                    "{\"model\":{\"type\":\"minecraft:model\",\"model\":\"minecraft:item/" + state + "\"}}");
        }

        // Copied from vanilla's own blocking branch: the shield is drawn by the shield special
        // renderer, which is what keeps a patterned shield's colours and the blocking orientation.
        // Vanilla's own definition carries a transformation here and it is load bearing: our chain
        // already holds the item at vanilla's third person angle, so the raised shield is one quarter
        // turn off in the mesh itself. It needs a -90 degree roll about the plate's width (a half turn,
        // as vanilla ships, leaves the plate flat with its face to the sky) and then a half turn about
        // the axis the chain leaves pointing up, which is what turns the painted face out instead of
        // the handle side. The two compose into the single quaternion below, a half turn about
        // (0, -1, 1) / sqrt(2); doing it as two separate rotations would tilt the plate again.
        builder.addStringData("assets/" + MOD_ID + "/items/guard_shield_blocking.json",
                "{\"model\":{\"type\":\"minecraft:special\",\"base\":\"minecraft:item/shield_blocking\","
                        + "\"model\":{\"type\":\"minecraft:shield\"},"
                        + "\"transformation\":{\"left_rotation\":[0.0,-0.7071068,0.7071068,0.0],"
                        + "\"right_rotation\":[0.0,0.0,0.0,1.0],\"scale\":[1.0,1.0,1.0],"
                        + "\"translation\":[0.0,0.0,0.0]}}}");
    }

    private static Identifier heldId(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    private static void build(ResourcePackBuilder builder) {
        var atlas = AtlasAsset.builder();
        for (var model : GuardModels.ALL) {
            model.generateAssets(builder::addData, atlas);
        }

        // The inventory screen's toggles and readouts are items, so their icons are item models too.
        // They follow the vanilla item conventions and need no atlas entries of their own.
        GuardIcons.generate(builder);
        writeHeldItemModels(builder);

        // The guard is drawn from item models, and item models resolve their sprites from the item
        // atlas. Adding the same sprite to the block atlas as well makes the client log it as a
        // duplicate and drop the item atlas copy, which leaves every part untextured.
        builder.addData("assets/minecraft/atlases/items.json", atlas.build());
    }
}
