package eu.pb4.guardvillagerspatch.impl;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import eu.pb4.guardvillagerspatch.impl.entity.GuardElementHolder;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.IdentifiedUniqueEntityAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.UniqueIdentifiableAttachment;
import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.function.Consumer;

/**
 * Shows a guard to vanilla clients as its own model.
 *
 * <p>The guard entity itself is sent as an invisible item display, and a virtual entity holder
 * draws the guard's mesh on top of it, one item display per body part. Unlike a mannequin, this
 * keeps the guard's nose, hat, shoulder pads and quiver, so an unmodded client sees a guard rather
 * than a player.
 */
public class GuardPolymerEntity implements PolymerEntity {
    public static final Identifier MODEL = Identifier.fromNamespaceAndPath("guardvillagerspatch", "guard_model");

    private final GuardEntity guard;

    public GuardPolymerEntity(GuardEntity guard) {
        this.guard = guard;
        // The client half of an integrated server builds guards too, and those live in a
        // ClientLevel. A virtual entity can only be attached through a ServerLevel's tracker entry,
        // so attaching there throws a ClassCastException and takes the client down.
        if (guard.level() instanceof ServerLevel) {
            IdentifiedUniqueEntityAttachment.ofTicking(MODEL, new GuardElementHolder(guard), guard);
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ITEM_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial) {
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.TELEPORTATION_DURATION, 3));
        }
    }

    @Override
    public void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        // The client has no guard to animate; the model holder does the swinging.
        if (packet instanceof ClientboundAnimatePacket) {
            return;
        }

        var attachment = UniqueIdentifiableAttachment.get(this.guard, MODEL);
        if (attachment == null) {
            PolymerEntity.super.onEntityPacketSent(consumer, packet);
            return;
        }

        var holder = (GuardElementHolder) attachment.holder();

        // Passengers cannot ride an item display, so send them onto the interaction element.
        if (packet instanceof ClientboundSetPassengersPacket passengers && passengers.getPassengers().length != 0) {
            consumer.accept(VirtualEntityUtils.createClientboundSetPassengersPacket(
                    this.guard.getId(), IntList.of(holder.interaction.getEntityId())));
            consumer.accept(VirtualEntityUtils.createClientboundSetPassengersPacket(
                    holder.interaction.getEntityId(), passengers.getPassengers()));
            return;
        }

        // Leads need a real (if invisible) entity to attach to.
        if (packet instanceof ClientboundSetEntityLinkPacket link) {
            consumer.accept(VirtualEntityUtils.createClientboundSetEntityLinkPacket(
                    holder.leadAttachment.getEntityId(), link.getDestId()));
            return;
        }

        PolymerEntity.super.onEntityPacketSent(consumer, packet);
    }
}
