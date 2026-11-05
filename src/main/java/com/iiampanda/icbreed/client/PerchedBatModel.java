package com.iiampanda.icbreed.client;

import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BatRenderState;

public class PerchedBatModel extends BatModel {
    public static class State extends BatRenderState {
        public boolean flapping;
    }

    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightTip;
    private final ModelPart leftTip;

    public PerchedBatModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.rightWing = body.getChild("right_wing");
        this.leftWing = body.getChild("left_wing");
        this.rightTip = this.rightWing.getChild("right_wing_tip");
        this.leftTip = this.leftWing.getChild("left_wing_tip");
    }

    @Override
    public void setupAnim(BatRenderState state) {
        super.setupAnim(state);
        if (state instanceof State s && s.flapping) {
            float flap = (float) Math.cos(s.ageInTicks * 1.3F) * (float) Math.PI * 0.25F;
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
}
