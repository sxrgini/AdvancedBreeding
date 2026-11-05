package com.iiampanda.icbreed.genetics;

import com.iiampanda.icbreed.mixin.TadpoleAccessor;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;

public final class Ages {
    private Ages() {
    }

    public static boolean supportsType(net.minecraft.world.entity.Entity e) {
        return e instanceof Animal || e instanceof Dolphin || e instanceof Tadpole || e instanceof Bat;
    }

    public static boolean supports(net.minecraft.world.entity.Entity e) {
        return supportsType(e) && com.iiampanda.icbreed.config.ModdedMobs.enabled(e);
    }

    public static boolean isBaby(LivingEntity e) {
        return e instanceof Tadpole || e.isBaby();
    }

    public static int getAge(Mob m) {
        if (m instanceof AgeableMob a) return a.getAge();
        if (m instanceof Bat) return com.iiampanda.icbreed.platform.Att.getOrElse(m, Genetics.BAT_AGE, 0);
        if (m instanceof Tadpole t) return ((TadpoleAccessor) (Object) t).icbreed$getAge() - Tadpole.ticksToBeFrog;
        return 0;
    }

    public static void setAge(Mob m, int age) {
        if (m instanceof AgeableMob a) a.setAge(age);
        else if (m instanceof Bat) {
            if (age == 0) com.iiampanda.icbreed.platform.Att.remove(m, Genetics.BAT_AGE);
            else com.iiampanda.icbreed.platform.Att.set(m, Genetics.BAT_AGE, age);
        }
        else if (m instanceof Tadpole t) ((TadpoleAccessor) (Object) t).icbreed$setAge(age + Tadpole.ticksToBeFrog);
    }

    public static boolean isFood(Mob m, ItemStack s) {
        if (m instanceof Animal a) return a.isFood(s);
        if (m instanceof Dolphin) return isFish(s);
        if (m instanceof Tadpole) return s.is(ItemTags.FROG_FOOD);
        if (m instanceof Bat) return isBatFood(s);
        return false;
    }

    public static boolean isBatFood(ItemStack s) {
        return s.is(Items.MELON_SLICE) || s.is(Items.APPLE) || s.is(Items.SWEET_BERRIES);
    }

    public static boolean isFish(ItemStack s) {
        return s.is(Items.COD) || s.is(Items.SALMON);
    }
}
