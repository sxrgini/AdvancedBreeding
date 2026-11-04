package com.iiampanda.icbreed.traits;

import java.util.ArrayList;
import java.util.List;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public final class SaddleBags {
    public static final int SLOTS = 27;

    private SaddleBags() {
    }

    public static int styleOf(ItemStack held) {
        if (held.is(net.minecraft.world.item.Items.BARREL)) return 0;
        if (held.getItem() instanceof net.minecraft.world.item.BlockItem bi
                && bi.getBlock() instanceof net.minecraft.world.level.block.ShulkerBoxBlock box) {
            var color = box.getColor();
            return color == null ? 1 : 2 + color.getId();
        }
        return -1;
    }

    public static ItemStack itemFor(int style) {
        if (style <= 0) return new ItemStack(net.minecraft.world.item.Items.BARREL);
        if (style == 1) return new ItemStack(net.minecraft.world.item.Items.SHULKER_BOX);
        return new ItemStack(net.minecraft.world.level.block.ShulkerBoxBlock.getBlockByColor(net.minecraft.world.item.DyeColor.byId(style - 2)).asItem());
    }

    public static boolean canFit(AbstractHorse horse, int style) {
        if (style < 0 || horse instanceof AbstractChestedHorse || horse.isBaby() || Genetics.isWild(horse)) return false;
        var t = com.iiampanda.icbreed.platform.Att.get(horse, Genetics.TRAITS);
        if (t == null || com.iiampanda.icbreed.platform.Att.get(horse, Genetics.BAGS) != null) return false;
        return t.rank(style == 0 ? "saddle_bags" : "shulker_bags") > 0;
    }

    public static void fit(AbstractHorse horse, int style, ItemStack held) {
        List<ItemStack> empty = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) empty.add(ItemStack.EMPTY);

        net.minecraft.nbt.CompoundTag contents = style > 0 ? held.getTagElement("BlockEntityTag") : null;
        if (style > 0 && contents != null) {
            var items = net.minecraft.core.NonNullList.withSize(SLOTS, ItemStack.EMPTY);
            net.minecraft.world.ContainerHelper.loadAllItems(contents, items);
            for (int i = 0; i < SLOTS; i++) empty.set(i, items.get(i));
        }
        com.iiampanda.icbreed.platform.Att.set(horse, Genetics.BAG_STYLE, style);
        com.iiampanda.icbreed.platform.Att.set(horse, Genetics.BAGS, empty);
        horse.playSound(SoundEvents.DONKEY_CHEST, 1.0F, (horse.getRandom().nextFloat() - horse.getRandom().nextFloat()) * 0.2F + 1.0F);
    }

    public static void open(ServerPlayer player, AbstractHorse horse) {
        List<ItemStack> saved = com.iiampanda.icbreed.platform.Att.get(horse, Genetics.BAGS);
        if (saved == null) return;
        SimpleContainer container = new SimpleContainer(SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                List<ItemStack> now = new ArrayList<>();
                for (int i = 0; i < SLOTS; i++) now.add(getItem(i).copy());
                com.iiampanda.icbreed.platform.Att.set(horse, Genetics.BAGS, now);
            }

            @Override
            public boolean stillValid(net.minecraft.world.entity.player.Player p) {
                return horse.isAlive() && p.distanceToSqr(horse) <= 64;
            }
        };
        for (int i = 0; i < SLOTS && i < saved.size(); i++) container.setItem(i, saved.get(i).copy());
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, container),
                Component.translatable("advbreeding.gui.bags_title", horse.getDisplayName())));
    }

    public static void remove(AbstractHorse horse, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        dropAll(horse);
        horse.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
        player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
    }

    public static void dropAll(AbstractHorse horse) {
        List<ItemStack> saved = com.iiampanda.icbreed.platform.Att.get(horse, Genetics.BAGS);
        if (saved == null || !(horse.level() instanceof ServerLevel level)) return;
        com.iiampanda.icbreed.platform.Att.remove(horse, Genetics.BAGS);
        Integer style = com.iiampanda.icbreed.platform.Att.get(horse, Genetics.BAG_STYLE);
        com.iiampanda.icbreed.platform.Att.remove(horse, Genetics.BAG_STYLE);
        int st = style == null ? 0 : style;
        ItemStack drop = itemFor(st);
        if (st > 0) {
            var items = net.minecraft.core.NonNullList.withSize(SLOTS, ItemStack.EMPTY);
            for (int i = 0; i < SLOTS && i < saved.size(); i++) items.set(i, saved.get(i));
            net.minecraft.nbt.CompoundTag data = net.minecraft.world.ContainerHelper.saveAllItems(new net.minecraft.nbt.CompoundTag(), items);
            net.minecraft.world.item.BlockItem.setBlockEntityData(drop, net.minecraft.world.level.block.entity.BlockEntityType.SHULKER_BOX, data);
            horse.spawnAtLocation(drop);
        } else {
            horse.spawnAtLocation(drop);
            for (ItemStack s : saved) if (!s.isEmpty()) horse.spawnAtLocation(s);
        }
    }
}
