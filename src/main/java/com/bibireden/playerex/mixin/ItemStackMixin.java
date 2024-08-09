package com.bibireden.playerex.mixin;

import com.bibireden.playerex.util.PlayerEXUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Shadow
    public abstract boolean hurt(int amount, RandomSource random, @Nullable ServerPlayer serverPlayer);

    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At(value = "HEAD"), cancellable = true)
    public void preventUse(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }

    @Inject(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "HEAD"), cancellable = true)
    public void preventUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", at = @At(value = "HEAD"), cancellable = true)
    public void preventDamage(int amount, RandomSource random, ServerPlayer user, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"), cancellable = true)
    public <T extends LivingEntity> void preventBreak(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        ItemStack stack = (ItemStack)(Object)this;
        // TODO: If "unbreakable" item, maybe use a tag?
        if (true) {
            if (!PlayerEXUtil.isBroken(stack)) {
                CompoundTag tag = stack.getTag();
                tag.putBoolean("broken", true);
                stack.setTag(tag);
            }
            ci.cancel();
        }
    }

    @Inject(method = "setDamageValue(I)V", at = @At(value = "HEAD"))
    public void removeBrokenOnRepair(int damage, CallbackInfo ci) {
        ItemStack stack = (ItemStack)(Object)this;
        if (PlayerEXUtil.isBroken(stack) && damage < stack.getDamageValue()) {
            CompoundTag tag = stack.getTag();
            tag.putBoolean("broken", false);
            stack.setTag(tag);
        }
    }
}
