package com.iiampanda.icbreed.mixin;

import net.minecraft.world.entity.animal.frog.Tadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tadpole.class)
public interface TadpoleAccessor {
    @Accessor("age")
    int icbreed$getAge();

    @Accessor("age")
    void icbreed$setAge(int age);
}
