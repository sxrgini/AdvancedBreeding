package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.traits.MountJump;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.world.entity.animal.sniffer.Sniffer.class)
public abstract class SnifferJumpMixin implements PlayerRideableJumping {
    @Override
    public void onPlayerJump(int power) {
        MountJump.charge((Mob) (Object) this, power);
    }

    @Override
    public boolean canJump() {
        return MountJump.can((Mob) (Object) this);
    }

    @Override
    public void handleStartJump(int power) {
    }

    @Override
    public void handleStopJump() {
    }
}
