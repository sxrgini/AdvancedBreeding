package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.IcBreedConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class GuiOpen {
    private GuiOpen() {
    }

    public static boolean allowed(Player player, Entity entity) {
        String mode = IcBreedConfig.get().guiOpenMode;
        boolean sneaking = player.isShiftKeyDown();
        boolean empty = player.getMainHandItem().isEmpty();
        if ("KEYBIND".equals(mode)) return false;
        if ("SHIFT_RIGHT_CLICK_EMPTY_HAND".equals(mode)) return sneaking && empty;
        if ("RIGHT_CLICK_EMPTY_HAND".equals(mode)) {
            if (!empty) return false;
            return sneaking || !rideable(entity);
        }
        return sneaking;
    }

    private static boolean rideable(Entity entity) {
        if (entity instanceof AbstractHorse horse && horse.isTamed()) return true;
        return entity instanceof Mob mob && !mob.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
    }
}
