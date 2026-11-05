package com.iiampanda.icbreed.client;

import com.iiampanda.icbreed.genetics.Genetics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BatShoulderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bat.png");
    private final PerchedBatModel model;

    public BatShoulderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.model = new PerchedBatModel(models.bakeLayer(ModelLayers.BAT));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        int bits = com.iiampanda.icbreed.platform.Att.getOrElse(player, Genetics.BAT_SHOULDER, 0);
        if (bits == 0) return;
        boolean glide = (bits & 4) != 0;
        one(pose, buffers, light, player, ageInTicks, netHeadYaw, headPitch, (bits & 1) != 0, glide);
    }

    private void one(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float ageInTicks, float yaw, float pitch,
                     boolean left, boolean glide) {
        pose.pushPose();
        if (glide) pose.translate(0.0F, player.isCrouching() ? -2.3F : -2.45F, 0.0F);
        else pose.translate(left ? 0.4F : -0.4F, player.isCrouching() ? -1.3F : -1.5F, 0.0F);
        model.pose(glide, ageInTicks, yaw, pitch);
        model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
