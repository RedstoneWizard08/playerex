package com.bibireden.playerex.mixin;

import com.bibireden.playerex.api.event.PlayerEntityEvents;
import com.bibireden.playerex.util.PlayerEXUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void preventAttack(Entity target, CallbackInfo ci) {
        PlayerEntity entity = (PlayerEntity)(Object)this;
        // TODO: BetterCombat compat
        if (PlayerEXUtil.isBroken(entity.getMainHandStack())) {
            ci.cancel();
        }
    }

    @Inject(method = "interact(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void preventInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (PlayerEXUtil.isBroken(player.getStackInHand(hand))) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "isBlockBreakingRestricted(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/GameMode;)Z", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void preventBreakBlock(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (PlayerEXUtil.isBroken(player.getMainHandStack())) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "attack", at = @At("STORE"), name = "bl3", ordinal = 2)
    private boolean playerex_attack(boolean bl3, Entity target) {
        return PlayerEntityEvents.SHOULD_CRITICAL.invoker().shouldCritical((PlayerEntity)(Object) this, target, bl3);
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 2), name = "f", ordinal = 0)
    private float playerex_attack(float f, Entity target) {
        return PlayerEntityEvents.ON_CRITICAL.invoker().onCriticalDamage((PlayerEntity) (Object) this, target, f);
    }
}
