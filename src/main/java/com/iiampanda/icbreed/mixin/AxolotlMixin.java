package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.AxolotlColors;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Axolotl.class)
public abstract class AxolotlMixin {
    @Inject(method = "usePlayerItem", at = @At("HEAD"))
    private void icbreed$bucketFed(Player player, InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        if (stack.is(Items.TROPICAL_FISH_BUCKET)) Genetics.fed((Axolotl) (Object) this, player.getUUID());
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void icbreed$rainbowSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                      net.minecraft.world.entity.EntitySpawnReason reason, net.minecraft.world.entity.SpawnGroupData data,
                                      CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir) {
        Axolotl self = (Axolotl) (Object) this;
        if (reason == net.minecraft.world.entity.EntitySpawnReason.NATURAL || reason == net.minecraft.world.entity.EntitySpawnReason.CHUNK_GENERATION) {
            AxolotlColors.rollWild(self);
        } else if (reason == net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE) {
            AxolotlColors.rollEgg(self);
        }
    }

    @Inject(method = "saveToBucketTag", at = @At("TAIL"))
    private void icbreed$saveColor(ItemStack stack, CallbackInfo ci) {
        String id = ((Axolotl) (Object) this).getAttached(Genetics.AXO_COLOR);
        if (id != null) {
            net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA, stack, tag -> tag.putString("icbreed_color", id));
        }
    }

    @Inject(method = "loadFromBucketTag", at = @At("TAIL"))
    private void icbreed$loadColor(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        String id = tag.getStringOr("icbreed_color", "");
        if (!id.isEmpty()) ((Axolotl) (Object) this).setAttached(Genetics.AXO_COLOR, id);
    }
}
