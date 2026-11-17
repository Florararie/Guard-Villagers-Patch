package eu.pb4.guardvillagerspatch.impl.entity.model;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.CubeDeformation;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.CubeListBuilder;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.EntityModel;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.LayerDefinition;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.MeshDefinition;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.ModelPart;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.PartDefinition;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.PartPose;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;

/**
 * The armor the guard wears, rebuilt against FactoryTools' model classes.
 *
 * <p>This is the mod's own {@code GuardArmorModel}: vanilla's humanoid armor shell, except that the
 * head is swapped for the box that fits the guard's taller villager head. The outer variant carries
 * the helmet, chestplate and boots at the inflate the mod uses for them; the inner variant is the
 * one leggings are drawn with. It has no animation of its own, the holder copies the guard's poses
 * onto it every tick so the plates move with the limbs they sit on.
 */
public class GuardArmorModel extends EntityModel<GuardEntity> {
    private static final CubeDeformation OUTER = new CubeDeformation(1.0F);
    private static final CubeDeformation INNER = new CubeDeformation(0.5F);
    /**
     * The mod lifts the armor head to the villager head's top, and its crown ends up in exactly the
     * same plane as the guard's own headband, which draws a solid box with its top at the same
     * height. A hundredth of a model unit is far below anything visible and takes the helmet out of
     * that plane, so the crown no longer flickers while the band stays on.
     */
    private static final float HEAD_Y = 1.02F;

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GuardArmorModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    /** Helmets, chestplates and boots: the mod's outer layer. */
    public static LayerDefinition outerLayer() {
        MeshDefinition mesh = humanoid(OUTER);
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -10.0F, -4.0F, 8, 8, 8, OUTER),
                PartPose.offset(0.0F, HEAD_Y, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Leggings: the mod's inner layer, which hugs the leg rather than covering it. */
    public static LayerDefinition innerLayer() {
        return LayerDefinition.create(humanoid(INNER), 64, 32);
    }

    /** Vanilla's {@code HumanoidModel.createMesh}, which is what the mod's armor layer is built on. */
    private static MeshDefinition humanoid(CubeDeformation dilation) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, dilation),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, dilation),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, dilation),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, dilation),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, dilation),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, dilation),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return mesh;
    }

    /**
     * Takes the guard's limb poses. Only the rotations are copied: the armor shares the guard's part
     * offsets except for the head, whose lift above keeps the crown out of the headband's plane.
     */
    public void copyPose(GuardVillagerModel guard) {
        copyRotation(guard.getHead(), this.head);
        copyRotation(guard.getBody(), this.body);
        copyRotation(guard.getArm(HumanoidArm.RIGHT), this.rightArm);
        copyRotation(guard.getArm(HumanoidArm.LEFT), this.leftArm);
        copyRotation(guard.getLeg(HumanoidArm.RIGHT), this.rightLeg);
        copyRotation(guard.getLeg(HumanoidArm.LEFT), this.leftLeg);
    }

    private static void copyRotation(ModelPart from, ModelPart to) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }

    public void hideAll() {
        this.head.visible = false;
        this.body.visible = false;
        this.rightArm.visible = false;
        this.leftArm.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
    }

    /** Shows the pieces one slot covers, the same split the mod's armor layer makes. */
    public void show(EquipmentSlot slot) {
        switch (slot) {
            case HEAD -> this.head.visible = true;
            case CHEST -> {
                this.body.visible = true;
                this.rightArm.visible = true;
                this.leftArm.visible = true;
            }
            case LEGS, FEET -> {
                this.rightLeg.visible = true;
                this.leftLeg.visible = true;
            }
            default -> {
            }
        }
    }
}
