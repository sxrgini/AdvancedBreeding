package com.iiampanda.icbreed.client;

import com.iiampanda.icbreed.genetics.AxolotlColors;
import com.iiampanda.icbreed.genetics.Genetics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class AxolotlLegLayer extends RenderLayer<Axolotl, AxolotlModel<Axolotl>> {
    private final AxolotlModel<Axolotl> legs;

    public AxolotlLegLayer(RenderLayerParent<Axolotl, AxolotlModel<Axolotl>> parent, EntityRendererProvider.Context context) {
        super(parent);
        net.minecraft.client.model.geom.ModelPart root = context.bakeLayer(ModelLayers.AXOLOTL);
        this.legs = new AxolotlModel<>(root);
        root.getAllParts().forEach(part -> part.skipDraw = true);
        root.getChild("body").getChild("left_hind_leg").skipDraw = false;
        root.getChild("body").getChild("left_front_leg").skipDraw = false;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, Axolotl axolotl, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (axolotl.isBaby() || axolotl.isInvisible()) return;
        String key = com.iiampanda.icbreed.platform.Att.get(axolotl, Genetics.AXO_COLOR);
        if (!AxolotlColors.isChimera(key)) return;
        String second = key.substring(AxolotlColors.CHIMERA_PREFIX.length()).split(":")[1];
        getParentModel().copyPropertiesTo(legs);
        legs.setupAnim(axolotl, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        legs.renderToBuffer(pose, buffers.getBuffer(RenderType.entityCutoutNoCullZOffset(AxolotlColors.plainTexture(second))),
                light, LivingEntityRenderer.getOverlayCoords(axolotl, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
