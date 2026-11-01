package com.iiampanda.icbreed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BagsLayer extends RenderLayer<HorseRenderState, HorseModel> {
    public static final RenderStateDataKey<Integer> BAG_STYLE = RenderStateDataKey.create();

    private final ItemModelResolver items;
    private final ItemStackRenderState barrel = new ItemStackRenderState();

    public BagsLayer(RenderLayerParent<HorseRenderState, HorseModel> parent, ItemModelResolver items) {
        super(parent);
        this.items = items;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, HorseRenderState state, float yRot, float xRot) {
        Integer style = state.getData(BAG_STYLE);
        if (state.isBaby || style == null || style < 0) return;
        var root = getParentModel().root();
        if (!root.hasChild("body")) return;
        items.updateForTopItem(barrel, com.iiampanda.icbreed.traits.SaddleBags.itemFor(style), ItemDisplayContext.NONE, null, null, 0);
        if (barrel.isEmpty()) return;
        for (int side = -1; side <= 1; side += 2) {
            pose.pushPose();
            root.getChild("body").translateAndRotate(pose);

            pose.translate(side * (style == 0 ? 3.6F : 4.0F) / 16.0F, -5.0F / 16.0F, 0.0F);

            pose.rotateDegrees(Axis.ZP, -90.0F * side);
            pose.scale(0.5F, 0.5F, 0.5F);
            barrel.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
