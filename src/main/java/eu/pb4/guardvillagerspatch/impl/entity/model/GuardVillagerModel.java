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
import eu.pb4.guardvillagerspatch.impl.GuardResourcePack;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

        // Every goal sets its use state on the server, so the holder can pose the arms from what
        // the guard is doing right now instead of guessing from the items alone.
        float swing = guard.getAttackAnim(1.0F);
        ItemStack usedStack = guard.getUseItem();
        boolean using = guard.isUsingItem() && !usedStack.isEmpty();

        if (swing > 0.0F) {
            if (mainHand.isEmpty()) {
                AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, guard);
            } else {
                AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, guard.getMainArm(), swing, guard.tickCount);
            }
        } else if (using && usedStack.getItem() instanceof BowItem) {
            this.poseBow(guard);
        } else if (using && usedStack.getItem() instanceof CrossbowItem) {
            this.poseCrossbowCharge(guard, usedStack);
        } else if (guard.isBlocking()) {
            this.poseBlock(guard);
        } else if (CrossbowItem.isCharged(mainHand)) {
            this.poseCrossbowHold(guard.getMainArm());
        } else if (CrossbowItem.isCharged(offHand)) {
            this.poseCrossbowHold(guard.getMainArm().getOpposite());
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

    public ModelPart getHead() {
        return this.head;
    }

    public ModelPart getBody() {
        return this.body;
    }

    public ModelPart getLeg(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? this.rightLeg : this.leftLeg;
    }

    /** Both arms on the bow, the way vanilla poses a drawn bow. */
    private void poseBow(GuardEntity guard) {
        HumanoidArm bowArm = armOf(guard, guard.getUsedItemHand());
        float side = bowArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        ModelPart hold = this.getArm(bowArm);
        ModelPart pull = this.getArm(bowArm.getOpposite());

        hold.yRot = -0.1F * side + this.head.yRot;
        hold.xRot = -1.5707964F + this.head.xRot;
        pull.xRot = -0.9424779F + this.head.xRot;
        pull.yRot = this.head.yRot - 0.4F * side;
    }

    /**
     * The rack, the way pillagers do it: vanilla's own crossbow charge pose, which pulls the
     * holding arm in against the chest while the other hand follows the string back as the charge
     * fills. The helper mirrors itself for a left-handed guard.
     */
    private void poseCrossbowCharge(GuardEntity guard, ItemStack stack) {
        HumanoidArm arm = armOf(guard, guard.getUsedItemHand());
        float duration = Math.max(1, CrossbowItem.getChargeDuration(stack, guard));

        AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, duration,
                guard.getTicksUsingItem(), arm == HumanoidArm.RIGHT);
    }

    /** A racked crossbow is carried up and ready, again in vanilla's own illager pose. */
    private void poseCrossbowHold(HumanoidArm arm) {
        AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, arm == HumanoidArm.RIGHT);
    }

    /** Vanilla's shield pose: the forearm comes across the body and the shield faces the threat. */
    private void poseBlock(GuardEntity guard) {
        ModelPart arm = this.getArm(armOf(guard, guard.getUsedItemHand()));
        float side = arm == this.rightArm ? 1.0F : -1.0F;

        arm.xRot = arm.xRot * 0.5F - 0.9424779F;
        arm.yRot = -0.5235988F * side;
    }

    /**
     * The model a held stack has to be drawn with.
     *
     * <p>Vanilla picks the drawn bow, the half-racked crossbow, the loaded crossbow and the raised
     * shield with item model predicates that read the holding entity. A display entity has no
     * entity to read, so the same choice is made here and handed over through {@code ITEM_MODEL}.
     * Null means the stack's own model is already right.
     */
    public static Identifier heldItemModel(GuardEntity guard, ItemStack stack, boolean usedInHand) {
        if (stack.isEmpty()) {
            return null;
        }
        if (usedInHand && stack.getItem() instanceof BowItem) {
            float drawn = bowDrawProgress(guard);
            return drawn >= 0.9F ? GuardResourcePack.BOW_PULLING_2
                    : drawn >= 0.65F ? GuardResourcePack.BOW_PULLING_1 : GuardResourcePack.BOW_PULLING_0;
        }
        if (usedInHand && stack.getItem() instanceof CrossbowItem) {
            float pull = crossbowChargeProgress(guard, stack);
            // Vanilla's last frame only starts at a full charge, which the guard's goal releases on
            // in that same tick, so it would never be seen. Starting it where the string already
            // looks home gives the rack the same three frames the bow has.
            return pull >= 0.85F ? GuardResourcePack.CROSSBOW_PULLING_2
                    : pull >= 0.58F ? GuardResourcePack.CROSSBOW_PULLING_1 : GuardResourcePack.CROSSBOW_PULLING_0;
        }
        if (stack.is(Items.SHIELD) && guard.isBlocking()) {
            return GuardResourcePack.SHIELD_BLOCKING;
        }
        if (CrossbowItem.isCharged(stack)) {
            return GuardResourcePack.CROSSBOW_ARROW;
        }
        return null;
    }

    /** Vanilla scales a bow's draw over twenty ticks. */
    private static float bowDrawProgress(GuardEntity guard) {
        return Mth.clamp(guard.getTicksUsingItem() / 20.0F, 0.0F, 1.0F);
    }

    /**
     * How much of the rack is done. A crossbow's use duration is 72000 ticks, so the ticks left in
     * the use say nothing about the charge; the ticks spent using it over the charge duration is
     * the number vanilla's own pull predicate reads.
     */
    private static float crossbowChargeProgress(GuardEntity guard, ItemStack stack) {
        int duration = Math.max(1, CrossbowItem.getChargeDuration(stack, guard));
        return Mth.clamp(guard.getTicksUsingItem() / (float) duration, 0.0F, 1.0F);
    }

    private static HumanoidArm armOf(GuardEntity guard, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? guard.getMainArm() : guard.getMainArm().getOpposite();
    }
}
