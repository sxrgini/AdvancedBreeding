package com.iiampanda.icbreed.client;

import com.iiampanda.icbreed.genetics.AxolotlColors;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.axolotl.AdultAxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class AxolotlLegLayer extends RenderLayer<AxolotlRenderState, EntityModel<AxolotlRenderState>> {
    private final AdultAxolotlModel legs;

    public AxolotlLegLayer(RenderLayerParent<AxolotlRenderState, EntityModel<AxolotlRenderState>> parent, EntityRendererProvider.Context context) {
        super(parent);
        this.legs = new AdultAxolotlModel(context.bakeLayer(ModelLayers.AXOLOTL));
        for (var part : legs.root().getAllParts()) part.skipDraw = true;
        legs.root().getChild("body").getChild("left_hind_leg").skipDraw = false;
        legs.root().getChild("body").getChild("left_front_leg").skipDraw = false;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AxolotlRenderState state, float yRot, float xRot) {
        if (state.isBaby || state.isInvisible) return;
        String key = ((AxolotlColorState) state).icbreed$color();
        if (!AxolotlColors.isChimera(key)) return;
        String second = key.substring(AxolotlColors.CHIMERA_PREFIX.length()).split(":")[1];
        collector.order(1).submitModel(legs, state, pose, RenderTypes.entityCutoutZOffset(AxolotlColors.plainTexture(second)),
                light, LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1, null, state.outlineColor);
    }
}
