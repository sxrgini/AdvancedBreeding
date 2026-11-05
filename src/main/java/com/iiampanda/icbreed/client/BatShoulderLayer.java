package com.iiampanda.icbreed.client;

import com.iiampanda.icbreed.genetics.Genetics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class BatShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/bat/bat.png");
    private final PerchedBatModel model;

    public BatShoulderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet models) {
        super(parent);
        this.model = new PerchedBatModel(models.bakeLayer(ModelLayers.BAT));
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity e = level.getEntity(state.id);
        if (e == null) return;
        int bits = com.iiampanda.icbreed.platform.Att.getOrElse(e, Genetics.BAT_SHOULDER, 0);
        if (bits == 0) return;
        boolean glide = (bits & 4) != 0;
        if ((bits & 1) != 0) one(pose, collector, light, state, yRot, xRot, true, glide);
        else one(pose, collector, light, state, yRot, xRot, false, glide);
    }

    private void one(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState player, float yRot, float xRot,
                     boolean left, boolean glide) {
        pose.pushPose();
        if (glide) pose.translate(0.0F, player.isCrouching ? -2.3F : -2.45F, 0.0F);
        else pose.translate(left ? 0.4F : -0.4F, player.isCrouching ? -1.3F : -1.5F, 0.0F);
        PerchedBatModel.State s = new PerchedBatModel.State();
        s.flapping = glide;
        s.ageInTicks = player.ageInTicks;
        s.yRot = yRot;
        s.xRot = xRot;
        collector.submitModel(this.model, s, pose, TEXTURE, light, OverlayTexture.NO_OVERLAY, player.outlineColor, null);
        pose.popPose();
    }
}
