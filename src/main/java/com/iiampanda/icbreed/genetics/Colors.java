package com.iiampanda.icbreed.genetics;

import java.util.ArrayList;
import java.util.List;

import com.iiampanda.icbreed.mixin.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

public final class Colors {

    private Colors() {
    }

    private static double chance = com.iiampanda.icbreed.IcBreedConfig.get().phenotypeMutationChance;

    private static double chanceOf(Mob a, Mob b) {
        Effects x = Genetics.effects(a);
        Effects y = Genetics.effects(b);
        double bonus = Math.max(x == null ? 0 : x.phenoMutation, y == null ? 0 : y.phenoMutation);
        return Math.min(1.0, com.iiampanda.icbreed.IcBreedConfig.get().phenotypeMutationChance + bonus);
    }

    private static <T> T choose(RandomSource r, T a, T b, List<T> pool) {
        if (r.nextDouble() >= chance) return r.nextBoolean() ? a : b;
        List<T> c = new ArrayList<>(pool);
        c.remove(a);
        c.remove(b);
        if (c.isEmpty()) return r.nextBoolean() ? a : b;
        return c.get(r.nextInt(c.size()));
    }

    private static <T> List<Holder<T>> pool(Level level, ResourceKey<? extends Registry<T>> key) {
        List<Holder<T>> out = new ArrayList<>();
        level.registryAccess().lookupOrThrow(key).listElements().forEach(out::add);
        return out;
    }

    private static <E extends Enum<E>> List<E> all(E[] values) {
        return new ArrayList<>(List.of(values));
    }

    public static void inherit(Mob baby, Mob a, Mob b) {
        RandomSource r = baby.getRandom();
        Level lv = baby.level();
        chance = chanceOf(a, b);
        if (baby instanceof Sheep c && a instanceof Sheep x && b instanceof Sheep y) {
            c.setColor(choose(r, x.getColor(), y.getColor(), all(DyeColor.values())));
        } else if (baby instanceof MushroomCow c && a instanceof MushroomCow x && b instanceof MushroomCow y) {
            ((PhenoMushroomCowAccessor) (Object) c).icbreed$set(choose(r, x.getVariant(), y.getVariant(), all(MushroomCow.Variant.values())));
        } else if (baby instanceof Cow c && a instanceof Cow x && b instanceof Cow y) {
            c.setVariant(choose(r, x.getVariant(), y.getVariant(), pool(lv, Registries.COW_VARIANT)));
        } else if (baby instanceof Pig c && a instanceof Pig x && b instanceof Pig y) {
            ((PhenoPigAccessor) (Object) c).icbreed$set(choose(r, x.getVariant(), y.getVariant(), pool(lv, Registries.PIG_VARIANT)));
        } else if (baby instanceof Chicken c && a instanceof Chicken x && b instanceof Chicken y) {
            c.setVariant(choose(r, x.getVariant(), y.getVariant(), pool(lv, Registries.CHICKEN_VARIANT)));
        } else if (baby instanceof Cat c && a instanceof Cat x && b instanceof Cat y) {
            ((PhenoCatAccessor) (Object) c).icbreed$set(choose(r, x.getVariant(), y.getVariant(), pool(lv, Registries.CAT_VARIANT)));
        } else if (baby instanceof Wolf c && a instanceof Wolf x && b instanceof Wolf y) {
            ((PhenoWolfAccessor) (Object) c).icbreed$set(choose(r, x.get(DataComponents.WOLF_VARIANT), y.get(DataComponents.WOLF_VARIANT), pool(lv, Registries.WOLF_VARIANT)));
        } else if (baby instanceof Rabbit c && a instanceof Rabbit x && b instanceof Rabbit y) {
            List<Rabbit.Variant> pool = all(Rabbit.Variant.values());
            pool.remove(Rabbit.Variant.EVIL);
            ((PhenoRabbitAccessor) (Object) c).icbreed$set(choose(r, x.getVariant(), y.getVariant(), pool));
        } else if (baby instanceof Llama c && a instanceof Llama x && b instanceof Llama y) {
            ((PhenoLlamaAccessor) (Object) c).icbreed$set(choose(r, x.getVariant(), y.getVariant(), all(Llama.Variant.values())));
        } else if (baby instanceof Fox c && a instanceof Fox x && b instanceof Fox y) {
            ((PhenoFoxAccessor) (Object) c).icbreed$set(choose(r, x.getVariant(), y.getVariant(), all(Fox.Variant.values())));
        } else if (baby instanceof Parrot c && a instanceof Parrot x && b instanceof Parrot y) {
            ((ParrotAccessor) (Object) c).icbreed$setVariant(choose(r, x.getVariant(), y.getVariant(), all(Parrot.Variant.values())));
        } else if (baby instanceof Axolotl c && a instanceof Axolotl x && b instanceof Axolotl y) {
            ((PhenoAxolotlAccessor) (Object) c).icbreed$set(choose(r, x.getVariant(), y.getVariant(), all(Axolotl.Variant.values())));
        } else if (baby instanceof Horse c && a instanceof Horse x && b instanceof Horse y) {
            Horse src = r.nextBoolean() ? x : y;
            net.minecraft.world.entity.animal.equine.Variant coat = src.getVariant();
            Markings marks = src.getMarkings();
            if (r.nextDouble() < chance) {
                List<net.minecraft.world.entity.animal.equine.Variant> coats = all(net.minecraft.world.entity.animal.equine.Variant.values());
                coats.remove(x.getVariant());
                coats.remove(y.getVariant());
                if (!coats.isEmpty()) {
                    coat = coats.get(r.nextInt(coats.size()));
                    Markings[] ms = Markings.values();
                    marks = ms[r.nextInt(ms.length)];
                }
            }
            ((PhenoHorseAccessor) (Object) c).icbreed$set(coat, marks);
        } else if (baby instanceof Tadpole t && a instanceof Frog x && b instanceof Frog y) {
            Holder<net.minecraft.world.entity.animal.frog.FrogVariant> h = choose(r, x.getVariant(), y.getVariant(), pool(lv, Registries.FROG_VARIANT));
            h.unwrapKey().ifPresent(k -> com.iiampanda.icbreed.platform.Att.set(t, Genetics.FROG_VARIANT, k.identifier().toString()));
        }
    }

    public static void applyFrog(Tadpole src, Frog frog) {
        String id = com.iiampanda.icbreed.platform.Att.get(src, Genetics.FROG_VARIANT);
        if (id == null) return;
        Identifier parsed = Identifier.tryParse(id);
        if (parsed == null) return;
        frog.level().registryAccess().lookupOrThrow(Registries.FROG_VARIANT)
                .get(ResourceKey.create(Registries.FROG_VARIANT, parsed))
                .ifPresent(h -> ((PhenoFrogAccessor) (Object) frog).icbreed$set(h));
    }
}
