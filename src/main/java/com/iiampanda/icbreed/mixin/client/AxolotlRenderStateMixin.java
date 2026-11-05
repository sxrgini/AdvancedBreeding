package com.iiampanda.icbreed.mixin.client;

import com.iiampanda.icbreed.client.AxolotlColorState;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AxolotlRenderState.class)
public abstract class AxolotlRenderStateMixin implements AxolotlColorState {
    @Unique
    private String icbreed$color;

    @Override
    public String icbreed$color() {
        return icbreed$color;
    }

    @Override
    public void icbreed$color(String id) {
        icbreed$color = id;
    }
}
