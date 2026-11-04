package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.traits.TraitActions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public abstract class LootDropsMixin {
    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private void icbreed$drops(LootContext ctx, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> drops = cir.getReturnValue();
        drops.removeIf(s -> s.is(Items.NAME_TAG) && ctx.getRandom().nextDouble() >= IcBreedConfig.get().nameTagLootChance);
        if (!(ctx.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof net.minecraft.world.entity.Mob dead) || !Genetics.supports(dead)) return;
        Effects dfx = Genetics.effects(dead);
        if (dfx == null) return;
        TraitActions.extraDrops(dead, dfx, drops);
        boolean sheep = dead instanceof net.minecraft.world.entity.animal.Sheep;
        if (dead.isAlive() && sheep) {
            for (ItemStack s : drops) {
                if (s.is(net.minecraft.tags.ItemTags.WOOL)) {
                    Genetics.addXp(dead, Genetics.XP_PRODUCE);
                    break;
                }
            }
        }
        if (dead.isAlive() && dfx.productionRank > 0 && sheep
                && ctx.getRandom().nextDouble() < IcBreedConfig.get().productionChancePerRank * Math.min(3, dfx.productionRank)) {
            var extra = new java.util.ArrayList<ItemStack>();
            for (ItemStack s : drops) if (s.is(net.minecraft.tags.ItemTags.WOOL)) extra.add(s.copy());
            drops.addAll(extra);
        }
    }
}
