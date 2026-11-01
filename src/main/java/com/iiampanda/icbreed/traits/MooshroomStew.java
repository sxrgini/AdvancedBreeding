package com.iiampanda.icbreed.traits;

import java.util.List;

import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.platform.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public final class MooshroomStew {
    private static final List<Item> FLOWERS = List.of(Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID, Items.ALLIUM, Items.AZURE_BLUET,
            Items.RED_TULIP, Items.ORANGE_TULIP, Items.WHITE_TULIP, Items.PINK_TULIP, Items.OXEYE_DAISY, Items.CORNFLOWER, Items.LILY_OF_THE_VALLEY);

    private MooshroomStew() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (player.isShiftKeyDown() || player.isSpectator()) return InteractionResult.PASS;
            if (!(entity instanceof MushroomCow cow) || cow.isBaby()) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);
            if (!held.is(Items.BOWL)) return InteractionResult.PASS;
            long now = level.getGameTime();
            Long ready = com.iiampanda.icbreed.platform.Att.get(cow, Genetics.MILK_READY);
            if (ready != null && ready > now) {
                int secs = (int) ((ready - now + 19) / 20);
                player.sendOverlayMessage(Component.translatable("advbreeding.msg.stew_wait", String.format("%d:%02d", secs / 60, secs % 60)));
                return InteractionResult.FAIL;
            }
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Effects fx = Genetics.effects(cow);
            int rank = fx == null ? 0 : (int) Math.min(3, fx.stewRank);
            long cooldown = Math.max(2400L, IcBreedConfig.get().milkCooldownSeconds * 20L - rank * 1200L);
            com.iiampanda.icbreed.platform.Att.set(cow, Genetics.MILK_READY, now + cooldown);
            Item flower = FLOWERS.get(cow.getRandom().nextInt(FLOWERS.size()));
            SuspiciousEffectHolder holder = SuspiciousEffectHolder.tryGet(flower);
            ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
            if (holder != null) stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, holder.getSuspiciousEffects());
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, stew, false));
            level.playSound(null, cow, SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY, SoundSource.NEUTRAL, 1.0F, 1.0F);
            Genetics.addXp(cow, Genetics.XP_PRODUCE);
            return InteractionResult.SUCCESS;
        });
    }
}
