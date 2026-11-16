package eu.pb4.guardvillagerspatch.mixin;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import eu.pb4.guardvillagerspatch.impl.GuardMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuardEntity.class)
public abstract class GuardEntityMixin {

    @Inject(method = "openGui", at = @At("HEAD"), cancellable = true)
    private void guardvillagerspatch$openVanillaMenu(ServerPlayer player, CallbackInfo ci) {
        var guard = (GuardEntity) (Object) this;

        guard.setOwnerId(player.getUUID());
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }

        guard.interacting = true;
        player.openMenu(new SimpleMenuProvider(
                (syncId, inventory, ignored) -> new GuardMenu(syncId, inventory, guard),
                guard.getDisplayName()));

        ci.cancel();
    }
}
