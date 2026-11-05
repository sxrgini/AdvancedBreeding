package com.iiampanda.icbreed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

/** Stand-alone bat pose for the shoulder / glider layer (vanilla BatModel needs a Bat entity to animate). */
public class PerchedBatModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightTip;
    private final ModelPart leftTip;

    public PerchedBatModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.rightWing = body.getChild("right_wing");
        this.leftWing = body.getChild("left_wing");
        this.rightTip = rightWing.getChild("right_wing_tip");
        this.leftTip = leftWing.getChild("left_wing_tip");
    }

    public void pose(boolean flapping, float ageInTicks, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.xRot = headPitch * ((float) Math.PI / 180F);
        head.yRot = headYaw * ((float) Math.PI / 180F);
        body.xRot = (float) Math.PI / 4F + (float) Math.cos(ageInTicks * 0.1F) * 0.15F;
        body.yRot = 0.0F;
        if (flapping) {
            float flap = (float) Math.cos(ageInTicks * 1.3F) * (float) Math.PI * 0.25F;
            rightWing.yRot = flap;
            leftWing.yRot = -flap;
            rightTip.yRot = flap * 0.5F;
            leftTip.yRot = -flap * 0.5F;
        } else {
            rightWing.xRot = -0.15707964F;
            leftWing.xRot = -0.15707964F;
            rightWing.yRot = -1.2566371F;
            leftWing.yRot = 1.2566371F;
            rightTip.yRot = -1.7278761F;
            leftTip.yRot = 1.7278761F;
        }
    }

    public void render(PoseStack pose, VertexConsumer vc, int light, int overlay) {
        root.render(pose, vc, light, overlay);
    }
}
