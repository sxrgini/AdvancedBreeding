package com.iiampanda.icbreed.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.iiampanda.icbreed.platform.AttPersist;
import com.iiampanda.icbreed.platform.AttStore;
import com.iiampanda.icbreed.platform.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class AttEntityMixin implements AttStore {
    @Unique
    private final Map<AttachmentType<?>, Object> icbreed$att = new ConcurrentHashMap<>();

    @Override
    public Map<AttachmentType<?>, Object> icbreed$att() {
        return icbreed$att;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void icbreed$save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        AttPersist.write(icbreed$att, tag);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void icbreed$load(CompoundTag tag, CallbackInfo ci) {
        AttPersist.read(icbreed$att, tag);
    }
}
