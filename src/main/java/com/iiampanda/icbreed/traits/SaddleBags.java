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
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public final class SaddleBags {
    public static final int SLOTS = 27;

    private SaddleBags() {
    }

    public static int styleOf(ItemStack held) {
        if (held.is(net.minecraft.world.item.Items.BARREL)) return 0;
        if (held.is(net.minecraft.tags.ItemTags.SHULKER_BOXES)
                && held.getItem() instanceof net.minecraft.world.item.BlockItem bi
                && bi.getBlock() instanceof net.minecraft.world.level.block.ShulkerBoxBlock box) {
            var color = box.getColor();
            return color == null ? 1 : 2 + color.getId();
        }
        return -1;
    }

    public static ItemStack itemFor(int style) {
        if (style <= 0) return new ItemStack(net.minecraft.world.item.Items.BARREL);
        if (style == 1) return new ItemStack(net.minecraft.world.item.Items.SHULKER_BOX);
        return new ItemStack(net.minecraft.world.item.Items.DYED_SHULKER_BOX.pick(net.minecraft.world.item.DyeColor.byId(style - 2)));
    }

    public static boolean canFit(AbstractHorse horse, int style) {
        if (style < 0 || horse instanceof AbstractChestedHorse || horse.isBaby() || Genetics.isWild(horse)) return false;
        var t = horse.getAttached(Genetics.TRAITS);
        if (t == null || horse.getAttached(Genetics.BAGS) != null) return false;
        return t.rank(style == 0 ? "saddle_bags" : "shulker_bags") > 0;
    }

    public static void fit(AbstractHorse horse, int style, ItemStack held) {
        List<ItemStack> empty = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) empty.add(ItemStack.EMPTY);

        var contents = held.get(net.minecraft.core.component.DataComponents.CONTAINER);
        if (style > 0 && contents != null) {
            var items = net.minecraft.core.NonNullList.withSize(SLOTS, ItemStack.EMPTY);
            contents.copyInto(items);
            for (int i = 0; i < SLOTS; i++) empty.set(i, items.get(i));
        }
        horse.setAttached(Genetics.BAG_STYLE, style);
        horse.setAttached(Genetics.BAGS, empty);
        horse.playSound(SoundEvents.DONKEY_CHEST, 1.0F, (horse.getRandom().nextFloat() - horse.getRandom().nextFloat()) * 0.2F + 1.0F);
    }

    public static void open(ServerPlayer player, AbstractHorse horse) {
        List<ItemStack> saved = horse.getAttached(Genetics.BAGS);
        if (saved == null) return;
        SimpleContainer container = new SimpleContainer(SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                List<ItemStack> now = new ArrayList<>();
                for (int i = 0; i < SLOTS; i++) now.add(getItem(i).copy());
                horse.setAttached(Genetics.BAGS, now);
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
        player.getItemInHand(hand).hurtAndBreak(1, player, hand == net.minecraft.world.InteractionHand.MAIN_HAND
                ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
    }

    public static void dropAll(AbstractHorse horse) {
        List<ItemStack> saved = horse.getAttached(Genetics.BAGS);
        if (saved == null || !(horse.level() instanceof ServerLevel level)) return;
        horse.removeAttached(Genetics.BAGS);
        Integer style = horse.getAttached(Genetics.BAG_STYLE);
        horse.removeAttached(Genetics.BAG_STYLE);
        int st = style == null ? 0 : style;
        ItemStack drop = itemFor(st);
        if (st > 0) {
            drop.set(net.minecraft.core.component.DataComponents.CONTAINER,
                    net.minecraft.world.item.component.ItemContainerContents.fromItems(saved));
            horse.spawnAtLocation(level, drop);
        } else {
            horse.spawnAtLocation(level, drop);
            for (ItemStack s : saved) if (!s.isEmpty()) horse.spawnAtLocation(level, s);
        }
    }
}
