package com.bibireden.playerex.mixin;

import com.bibireden.playerex.util.PlayerEXUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
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
    public abstract boolean damage(int amount, Random random, @Nullable ServerPlayerEntity serverPlayer);

    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at = @At(value = "HEAD"), cancellable = true)
    public void preventUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }

    @Inject(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "HEAD"), cancellable = true)
    public void preventUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    public void preventDamage(int amount, Random random, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"), cancellable = true)
    public <T extends LivingEntity> void preventBreak(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        ItemStack stack = (ItemStack)(Object)this;
        // TODO: If "unbreakable" item, maybe use a tag?
        if (true) {
            if (!PlayerEXUtil.isBroken(stack)) {
                NbtCompound tag = stack.getNbt();
                tag.putBoolean("broken", true);
                stack.setNbt(tag);
            }
            ci.cancel();
        }
    }

    @Inject(method = "setDamage(I)V", at = @At(value = "HEAD"))
    public void removeBrokenOnRepair(int damage, CallbackInfo ci) {
        ItemStack stack = (ItemStack)(Object)this;
        if (PlayerEXUtil.isBroken(stack) && damage < stack.getDamage()) {
            NbtCompound tag = stack.getNbt();
            tag.putBoolean("broken", false);
            stack.setNbt(tag);
        }
    }
}
