package com.github.clevernucleus.playerex.mixin.client;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.clevernucleus.dataattributes_dc.api.DataAttributesAPI;
import com.github.clevernucleus.playerex.api.ExAPI;
import com.github.clevernucleus.playerex.config.ConfigImpl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    // Constructor
    private LivingEntityRendererMixin(EntityRendererFactory.Context ctx, M model, float shadowRadius) {
        super(ctx);
    }

    // Custom method to check if the level nameplate should be rendered
    @Unique
    private boolean playerex$shouldRenderLevel(T livingEntity) {
        if (this.dispatcher.camera == null) return false;

        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);
        float f = ((Entity) livingEntity).isSneaky() ? 32.0f : 64.0f;

        if (d >= (double) (f * f))
            return false;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayerEntity = minecraftClient.player;
        boolean bl = !livingEntity.isInvisibleTo(clientPlayerEntity);

        if (livingEntity != clientPlayerEntity && clientPlayerEntity != null) {
            AbstractTeam abstractTeam = ((Entity) livingEntity).getScoreboardTeam();
            AbstractTeam abstractTeam2 = clientPlayerEntity.getScoreboardTeam();

            if (abstractTeam != null) {
                AbstractTeam.VisibilityRule visibilityRule = abstractTeam.getNameTagVisibilityRule();
                return switch (visibilityRule) {
                    case ALWAYS -> bl;
                    case NEVER -> false;
                    case HIDE_FOR_OTHER_TEAMS -> abstractTeam2 == null ? bl
                            : abstractTeam.isEqual(abstractTeam2)
                            && (abstractTeam.shouldShowFriendlyInvisibles() || bl);
                    case HIDE_FOR_OWN_TEAM -> abstractTeam2 == null ? bl : !abstractTeam.isEqual(abstractTeam2) && bl;
                };
            }
        }

        return MinecraftClient.isHudEnabled() && livingEntity != minecraftClient.getCameraEntity() && bl
                && !((Entity) livingEntity).hasPassengers();
    }

    // Custom method to render the level nameplate
    @Unique
    private void playerex$renderLevel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        double d = this.dispatcher.getSquaredDistanceToCamera(entity);

        if (!(d > 4096.0D)) {
            boolean bl = !entity.isSneaky();
            float f = entity.getHeight() + ExAPI.getConfig().levelNameplateHeight();
            int i = 0;
            matrices.push();
            matrices.translate(0.0D, (double) f, 0.0D);
            matrices.multiply(this.dispatcher.getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            MinecraftClient mc = MinecraftClient.getInstance();
            float g = mc.options.getTextBackgroundOpacity(0.25F);
            int j = (int) (g * 255.0F) << 24;
            TextRenderer textRenderer = this.getTextRenderer();
            float h = (float) (-textRenderer.getWidth((StringVisitable) text) / 2);

            TextRenderer.TextLayerType textLayer = bl ? TextRenderer.TextLayerType.NORMAL
                    : TextRenderer.TextLayerType.SEE_THROUGH;

            textRenderer.draw(text, h, (float) i, 553648127, false, matrix4f, vertexConsumers, textLayer, j, light);

            if (bl) {
                textRenderer.draw((Text) text, h, (float) i, -1, false, matrix4f, vertexConsumers, textLayer, 0, light);
            }

            matrices.pop();
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void playerex$onRender(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        if (this.playerex$shouldRenderLevel(livingEntity) && ((ConfigImpl) ExAPI.getConfig()).levelNameplate()) {
            DataAttributesAPI.ifPresent(livingEntity, ExAPI.LEVEL, null, value -> {
                Text tag = Text.translatable("playerex.gui.text.nameplate", String.valueOf(Math.round(value))).formatted(Formatting.WHITE);
                this.playerex$renderLevel(livingEntity, tag, matrixStack, vertexConsumerProvider, i);
                return null;
            });
        }
    }
}
