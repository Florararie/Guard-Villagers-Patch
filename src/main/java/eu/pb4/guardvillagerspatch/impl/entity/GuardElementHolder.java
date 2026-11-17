package eu.pb4.guardvillagerspatch.impl.entity;

import com.mojang.math.Axis;
import dev.sterner.guardvillagers.common.entity.GuardEntity;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.EntityModelTransforms;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.PolyModelInstance;
import eu.pb4.factorytools.api.virtualentity.emuvanilla.model.ModelPart;
import eu.pb4.guardvillagerspatch.impl.entity.model.GuardArmorModel;
import eu.pb4.guardvillagerspatch.impl.entity.model.GuardArmorModels;
import eu.pb4.guardvillagerspatch.impl.entity.model.GuardModels;
import eu.pb4.guardvillagerspatch.impl.entity.model.GuardVillagerModel;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws a guard out of one item display per model part.
 *
 * <p>Each limit of the model gets its own display, positioned from the model's part matrices every
 * tick, so the guard walks, looks around, swings and blocks like the real one. The guard also keeps
 * an interaction element so clicks and punches still reach it, and a lead anchor.
 */
public class GuardElementHolder extends ElementHolder {
    private static final Matrix4fStack STACK = new Matrix4fStack(64);
    /** Matches the 0.9375 scale the mod's renderer applies. */
    private static final float GUARD_SCALE = 0.9375F;
    private static final int TELEPORT_DURATION = 3;

    protected final GuardEntity entity;
    public final InteractionElement interaction;
    public final LeadAttachmentElement leadAttachment = new LeadAttachmentElement();

    private final Map<ModelPart, ItemDisplayElement> partElements = new IdentityHashMap<>();
    private final Map<ModelPart, PolyModelInstance<?>> partOwners = new IdentityHashMap<>();
    private final List<PolyModelInstance<GuardArmorModel>> armor = new ArrayList<>();
    private final List<PolyModelInstance<GuardArmorModel>> previousArmor = new ArrayList<>();
    private final ItemDisplayElement mainHandItem;
    private final ItemDisplayElement offHandItem;
    private PolyModelInstance<GuardVillagerModel> model;
    private int variant = -1;
    private double lastX;
    private double lastZ;
    private boolean hurt;
    private boolean noTick = true;

    public GuardElementHolder(GuardEntity guard) {
        this.entity = guard;
        this.lastX = guard.getX();
        this.lastZ = guard.getZ();

        var handler = VirtualElement.InteractionHandler.redirect(guard);
        this.interaction = new InteractionElement(handler);
        this.interaction.setSendPositionUpdates(false);

        this.leadAttachment.setOffset(new Vec3(0, guard.getBbHeight() / 2, 0));
        this.leadAttachment.setInteractionHandler(handler);
        this.addElement(this.leadAttachment);
        this.addPassengerElement(this.interaction);

        HumanoidArm mainArm = guard.getMainArm();
        this.mainHandItem = heldItem(mainArm);
        this.offHandItem = heldItem(mainArm.getOpposite());

        setModel(GuardModels.get(guard.getGuardVariant()));
    }

    private ItemDisplayElement heldItem(HumanoidArm arm) {
        var element = new ItemDisplayElement();
        element.setItemDisplayContext(arm == HumanoidArm.RIGHT
                ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                : ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        element.setInterpolationDuration(1);
        element.setTeleportDuration(TELEPORT_DURATION);
        element.setViewRange(2);
        this.addElement(element);
        return element;
    }

    private void setModel(PolyModelInstance<GuardVillagerModel> newModel) {
        if (this.model == newModel) {
            return;
        }

        // Every part display is rebuilt on the next tick, from whichever models are in use then.
        for (var element : this.partElements.values()) {
            this.removeElement(element);
        }
        this.partElements.clear();
        this.partOwners.clear();

        this.model = newModel;

        if (!this.noTick) {
            this.tick();
        }
    }

    @Override
    public boolean startWatching(net.minecraft.server.network.ServerGamePacketListenerImpl player) {
        if (this.noTick) {
            this.onTick();
        }
        return super.startWatching(player);
    }

    @Override
    protected void onTick() {
        this.noTick = false;

        this.interaction.setSize(this.entity.getBbWidth(), this.entity.getBbHeight());
        this.interaction.setCustomName(this.entity.getCustomName());
        this.interaction.setCustomNameVisible(this.entity.isCustomNameVisible());

        if (this.entity.getGuardVariant() != this.variant) {
            this.variant = this.entity.getGuardVariant();
            setModel(GuardModels.get(this.variant));
        }

        updateWalkAnimation();
        renderModel();

        super.onTick();
    }

    /**
     * Feeds the walk animation from the server's own movement, since Minecraft only updates it on
     * the client, and there is no guard entity on the client any more.
     */
    private void updateWalkAnimation() {
        double dx = this.entity.getX() - this.lastX;
        double dz = this.entity.getZ() - this.lastZ;
        this.lastX = this.entity.getX();
        this.lastZ = this.entity.getZ();

        float movement = Mth.sqrt((float) (dx * dx + dz * dz)) * 4.0F;
        this.entity.walkAnimation.update(Math.min(movement, 1.0F), 0.4F, 1.0F);
    }

    private void renderModel() {
        var dimensions = this.entity.getDimensions(this.entity.getPose());

        STACK.pushMatrix();
        // Keep the model out of nearby blocks the way a real entity's box would.
        STACK.translate(0.0F, -dimensions.height() / 2, 0.0F);
        EntityModelTransforms.livingEntityTransform(this.entity, STACK, matrix4f -> matrix4f.scale(GUARD_SCALE));

        this.model.model().setupAnim(this.entity);

        boolean newHurt = this.entity.hurtTime > 0 || this.entity.deathTime > 0;
        if (this.hurt != newHurt) {
            this.hurt = newHurt;
            refreshPartItems();
        }

        updateArmor();

        this.model.model().render(STACK, (part, matrix, hidden) -> updateElement(this.model, part, matrix, hidden));
        for (var instance : this.armor) {
            instance.model().render(STACK, (part, matrix, hidden) -> updateElement(instance, part, matrix, hidden));
        }

        renderHeldItems();
        STACK.popMatrix();
    }

    /**
     * Picks the armor shells to draw, then drops the displays of any shell that is no longer worn or
     * has been swapped for another material. Those parts are not walked while they are off, so their
     * displays would otherwise stay in the holder forever.
     */
    private void updateArmor() {
        this.previousArmor.clear();
        this.previousArmor.addAll(this.armor);
        this.armor.clear();

        if (!this.entity.isInvisible()) {
            GuardArmorModels.collect(this.entity, this.armor);
            for (var instance : this.armor) {
                instance.model().copyPose(this.model.model());
            }
        }

        for (var instance : this.previousArmor) {
            if (this.armor.contains(instance)) {
                continue;
            }
            for (var part : instance.model().allParts()) {
                var element = this.partElements.remove(part);
                this.partOwners.remove(part);
                if (element != null) {
                    this.removeElement(element);
                }
            }
        }
    }

    private void renderHeldItems() {
        var root = this.model.model().root();
        HumanoidArm mainArm = this.entity.getMainArm();

        renderHeldItem(root, mainArm, this.mainHandItem);
        renderHeldItem(root, mainArm.getOpposite(), this.offHandItem);
    }

    /**
     * Places a held item at the end of the animated arm using the same transform vanilla's
     * {@code ItemInHandLayer} uses. The item's own third person display transform then orients it,
     * which is what makes a sword point forward and a shield sit flat against the arm.
     */
    private void renderHeldItem(ModelPart root, HumanoidArm arm, ItemDisplayElement element) {
        ItemStack stack = this.entity.getItemHeldByArm(arm);
        InteractionHand hand = arm == this.entity.getMainArm()
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        boolean usedInHand = this.entity.isUsingItem() && this.entity.getUsedItemHand() == hand;

        // The item models vanilla would pick for a drawing bow or a racking crossbow read the
        // holding entity, which a display does not have, so the model is chosen here instead.
        Identifier model = GuardVillagerModel.heldItemModel(this.entity, stack, usedInHand);
        if (model != null) {
            ItemStack shown = stack.copy();
            shown.set(DataComponents.ITEM_MODEL, model);
            element.setItem(shown);
        } else {
            element.setItem(stack);
        }

        if (stack.isEmpty()) {
            return;
        }

        STACK.pushMatrix();
        root.translateAndRotate(STACK);
        this.model.model().getArm(arm).translateAndRotate(STACK);

        boolean blockingShield = stack.is(Items.SHIELD)
                && this.entity.isBlocking() && this.entity.getUsedItemHand() == hand;

        STACK.rotate(Axis.XP.rotationDegrees(-90.0F));
        STACK.rotate(Axis.YP.rotationDegrees(180.0F));
        boolean left = arm == HumanoidArm.LEFT;
        // Vanilla's -0.625 along the arm. The main hand needs it a little shallower so a crossbow's
        // grip lands in the fist; the offhand keeps the full depth so a shield's band sits at the
        // wrist instead of riding up onto the forearm.
        STACK.translate((left ? -1 : 1) / 16.0F, 0.125F, left ? -0.625F : -0.500F);
        // Our model space is a half turn away from the one vanilla's ItemInHandLayer constants were
        // written for (FactoryTools bakes the part models with that turn). Cancelling it after the
        // translate leaves the grip point alone and only turns the item itself, and only the turn
        // about local Y leaves a crossbow the right way up. A raised shield is the exception: that
        // turn rolls it around the forearm, so the plate leaves the vertical. Vanilla's chain without
        // it is what holds a blocking shield the right way round, and the raised shield's own item
        // definition supplies the mirror vanilla applies to the shield model.
        if (!blockingShield) {
            STACK.rotate(Axis.YP.rotationDegrees(180.0F));
        }

        element.setTransformation(new Matrix4f(STACK));
        STACK.popMatrix();

        updateDisplayElement(element);
        element.startInterpolationIfDirty();
    }

    /**
     * Places one model part's display. A part with no geometry of its own, like the model's root,
     * has no model to draw and is skipped; a part the model hides has its display taken out of the
     * holder so the client stops drawing it.
     */
    private void updateElement(PolyModelInstance<?> owner, ModelPart part, Matrix4f matrix, boolean hidden) {
        var element = this.partElements.get(part);
        var current = this.getElements();

        if (hidden) {
            if (element != null && current.contains(element)) {
                this.removeElement(element);
            }
            return;
        }

        if (element == null) {
            var stack = partStack(owner, part);
            if (stack == null) {
                return;
            }
            element = EntityModelHelper.createItemDisplay(stack);
            this.partElements.put(part, element);
            this.partOwners.put(part, owner);
            this.addElement(element);
            element.setTeleportDuration(0);
        } else if (!current.contains(element)) {
            this.addElement(element);
            element.setTeleportDuration(0);
        } else {
            element.setTeleportDuration(TELEPORT_DURATION);
        }

        element.setTransformation(matrix);
        updateDisplayElement(element);
        element.startInterpolationIfDirty();
    }

    private ItemStack partStack(PolyModelInstance<?> owner, ModelPart part) {
        var parts = this.hurt ? owner.damagedModelParts() : owner.modelParts();
        var stack = parts.apply(part);
        return stack != null ? stack.get() : null;
    }

    /**
     * Swaps every drawn part between its normal and its hurt-flashed item. The armor is drawn from
     * its own models, so they have to be walked as well as the guard's own.
     */
    private void refreshPartItems() {
        for (var entry : this.partElements.entrySet()) {
            var owner = this.partOwners.get(entry.getKey());
            if (owner == null) {
                continue;
            }
            var stack = partStack(owner, entry.getKey());
            if (stack != null) {
                entry.getValue().setItem(stack);
            }
        }
    }

    private void updateDisplayElement(ItemDisplayElement element) {
        EntityDimensions dimensions = this.entity.getDimensions(this.entity.getPose());
        element.setDisplaySize(dimensions.width() * 2, dimensions.height() * 2);
        element.setOffset(new Vec3(0, dimensions.height() / 2, 0));
    }

    /** Creates one display element per model part, sized and offset like the rest of the body. */
    private static final class EntityModelHelper {
        private EntityModelHelper() {}

        static ItemDisplayElement createItemDisplay(ItemStack stack) {
            var element = ItemDisplayElementUtil.createSimple(stack);
            element.setInterpolationDuration(1);
            element.setTeleportDuration(TELEPORT_DURATION);
            element.setViewRange(2);
            return element;
        }
    }
}
