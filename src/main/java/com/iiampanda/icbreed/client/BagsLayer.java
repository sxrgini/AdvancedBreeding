package com.iiampanda.icbreed.client;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.mixin.client.HorseModelAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BagsLayer extends RenderLayer<Horse, HorseModel<Horse>> {
    public BagsLayer(RenderLayerParent<Horse, HorseModel<Horse>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, Horse horse, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        Integer style = com.iiampanda.icbreed.platform.Att.get(horse, Genetics.BAG_STYLE);
        if (com.iiampanda.icbreed.platform.Att.get(horse, Genetics.BAGS) == null || horse.isBaby()) return;
        int st = style == null ? 0 : style;
        ItemStack barrel = com.iiampanda.icbreed.traits.SaddleBags.itemFor(st);
        var body = ((HorseModelAccessor) getParentModel()).icbreed$body();
        for (int side = -1; side <= 1; side += 2) {
            pose.pushPose();
            body.translateAndRotate(pose);
            pose.translate(side * (st == 0 ? 3.6F : 4.0F) / 16.0F, -3.0F / 16.0F, 0.0F);
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0F * side));
            pose.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().getItemRenderer().renderStatic(barrel, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, horse.level(), horse.getId());
            pose.popPose();
        }
    }
}
