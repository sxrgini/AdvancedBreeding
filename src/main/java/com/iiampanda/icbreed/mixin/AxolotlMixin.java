package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Axolotl.class)
public abstract class AxolotlMixin {
    @Inject(method = "usePlayerItem", at = @At("HEAD"))
    private void icbreed$bucketFed(Player player, InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        if (stack.is(Items.TROPICAL_FISH_BUCKET)) Genetics.fed((Axolotl) (Object) this, player.getUUID());
    }
}
