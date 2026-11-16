package eu.pb4.guardvillagerspatch.impl.entity.model;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.EntityValueExtraction;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.AnimationUtils;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.CubeDeformation;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.CubeListBuilder;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.EntityModel;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.LayerDefinition;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.MeshDefinition;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.ModelPart;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.PartDefinition;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.PartPose;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

/**
 * The guard's own model, rebuilt against FactoryTools' model classes.
 *
 * <p>The geometry is copied cube for cube from the mod's {@code GuardVillagerModel}, including the
 * nose, hat, shoulder pads and quiver, so it draws from the mod's 128x128 guard atlas. FactoryTools
 * turns each part into an item model, which is what lets a vanilla client draw this mesh at all.
 */
public class GuardVillagerModel extends EntityModel<GuardEntity> {
    private static final float TO_RADIANS = (float) (Math.PI / 180.0);
    /** The mod fixes these in its constructor; setup resets the pose every tick, so repeat them. */
    private static final float QUIVER_ROTATION = 0.2617993877991494F;
    private static final float SHOULDER_PAD_ROTATION = 0.3490658503988659F;

    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart body;
    private final ModelPart quiver;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightShoulderPad;
    private final ModelPart leftShoulderPad;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GuardVillagerModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.hat = this.head.getChild("hat");
        this.body = root.getChild("body");
        this.quiver = this.body.getChild("quiver");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightShoulderPad = this.rightArm.getChild("shoulderPad_left");
        this.leftShoulderPad = this.leftArm.getChild("shoulderPad_right");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(52, 50)
                        .addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(49, 99)
                        .addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 75)
                        .mirror().addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(33, 48)
                        .addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        body.addOrReplaceChild("quiver", CubeListBuilder.create().texOffs(100, 0)
                        .addBox(-2.5F, -2.0F, 0.0F, 5, 10, 5, new CubeDeformation(0.0F)),
                PartPose.offset(0.5F, 3.0F, 2.3F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(54, 0)
                        .addBox(-1.0F, 0.0F, -2.0F, 2, 4, 2, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, -4.0F));
        head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.5F, -11.0F, -4.5F, 9, 11, 9, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 48).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 28)
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        leftArm.addOrReplaceChild("shoulderPad_right", CubeListBuilder.create().texOffs(72, 33).mirror()
                        .addBox(0.0F, 0.0F, -3.0F, 5, 3, 6, new CubeDeformation(0.0F)),
                PartPose.offset(-0.5F, -3.5F, 0.0F));
        rightArm.addOrReplaceChild("shoulderPad_left", CubeListBuilder.create().texOffs(72, 33)
                        .addBox(-5.0F, 0.0F, -3.0F, 5, 3, 6, new CubeDeformation(0.0F)),
                PartPose.offset(0.5F, -3.5F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(GuardEntity guard) {
        this.resetPose();

        this.quiver.zRot = QUIVER_ROTATION;
        this.rightShoulderPad.zRot = -SHOULDER_PAD_ROTATION;
        this.leftShoulderPad.zRot = SHOULDER_PAD_ROTATION;

        this.head.yRot = EntityValueExtraction.getRelativeHeadYaw(guard) * TO_RADIANS;
        this.head.xRot = guard.getXRot() * TO_RADIANS;

        ItemStack mainHand = guard.getMainHandItem();
        ItemStack offHand = guard.getOffhandItem();

        if (guard.isPassenger()) {
            this.rightArm.xRot = -0.62831855F;
            this.leftArm.xRot = -0.62831855F;
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = 0.31415927F;
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = -0.31415927F;
            this.leftLeg.zRot = -0.07853982F;
        } else {
            // The server never updates the walk animation, so the holder feeds it from movement.
            float speed = guard.walkAnimation.speed(1.0F);
            float position = guard.walkAnimation.position(1.0F);

            this.rightArm.xRot = Mth.cos(position * 0.6662F + (float) Math.PI) * 2.0F * speed * 0.5F;
            this.leftArm.xRot = Mth.cos(position * 0.6662F) * 2.0F * speed * 0.5F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = Mth.cos(position * 0.6662F) * 1.4F * speed * 0.5F;
            this.leftLeg.xRot = Mth.cos(position * 0.6662F + (float) Math.PI) * 1.4F * speed * 0.5F;
            this.rightLeg.yRot = 0.0F;
            this.leftLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
        }

        float swing = guard.getAttackAnim(1.0F);
        boolean drawing = guard.getUseItemRemainingTicks() > 0
                && (mainHand.getItem() instanceof ProjectileWeaponItem
                || offHand.getItem() instanceof ProjectileWeaponItem);

        if (swing > 0.0F) {
            if (mainHand.isEmpty()) {
                AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, guard);
            } else {
                AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, guard.getMainArm(), swing, guard.tickCount);
            }
        } else if (drawing) {
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.rightArm.xRot = -1.5707964F + this.head.xRot;
            this.leftArm.xRot = -0.9424779F + this.head.xRot;
            this.leftArm.yRot = this.head.yRot - 0.4F;
            this.leftArm.zRot = 1.5707964F;
        } else if (guard.isBlocking()) {
            ModelPart blockingArm = this.getArm(guard.getUsedItemHand() == InteractionHand.MAIN_HAND
                    ? guard.getMainArm()
                    : guard.getMainArm().getOpposite());
            blockingArm.xRot = blockingArm.xRot * 0.5F - ((float) Math.PI / 10F);
            blockingArm.yRot = 0.0F;
        }

        if (guard.kickTicks > 0) {
            float kick = 1.0F - (float) Mth.abs(10 - 2 * guard.kickTicks) / 10.0F;
            this.rightLeg.xRot = Mth.lerp(kick, this.rightLeg.xRot, -1.40F);
        }

        // The mod only shows the quiver while the guard is holding something to shoot, and the
        // shoulder pads are what the armour hides.
        this.quiver.visible = mainHand.getItem() instanceof ProjectileWeaponItem
                || offHand.getItem() instanceof ProjectileWeaponItem;
        boolean chestplate = !guard.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        this.rightShoulderPad.visible = !chestplate;
        this.leftShoulderPad.visible = !chestplate;
    }

    public ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
    }

    public ModelPart getHat() {
        return this.hat;
    }
}
