package com.iiampanda.icbreed.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.iiampanda.icbreed.platform.AttPersist;
import com.iiampanda.icbreed.platform.AttStore;
import com.iiampanda.icbreed.platform.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class AttBlockEntityMixin implements AttStore {
    @Unique
    private final Map<AttachmentType<?>, Object> icbreed$att = new ConcurrentHashMap<>();

    @Override
    public Map<AttachmentType<?>, Object> icbreed$att() {
        return icbreed$att;
    }

    @Inject(method = "saveWithoutMetadata", at = @At("RETURN"))
    private void icbreed$save(CallbackInfoReturnable<CompoundTag> cir) {
        AttPersist.write(icbreed$att, cir.getReturnValue());
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void icbreed$load(CompoundTag tag, CallbackInfo ci) {
        AttPersist.read(icbreed$att, tag);
    }
}
