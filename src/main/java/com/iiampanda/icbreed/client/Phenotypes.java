package com.iiampanda.icbreed.client;

import java.util.Locale;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;

public final class Phenotypes {
    private Phenotypes() {
    }

    public static String of(Entity e) {
        try {
            if (e instanceof Sheep s) return title(s.getColor().getName());
            if (e instanceof MushroomCow m) return title(m.getVariant().name());
            if (e instanceof Cow c) return holder(c.getVariant());
            if (e instanceof Pig p) return holder(p.getVariant());
            if (e instanceof net.minecraft.world.entity.animal.frog.Frog f) return holder(f.getVariant());
            if (e instanceof Chicken c) return holder(c.getVariant());
            if (e instanceof Cat c) return cat(holder(c.getVariant()));
            if (e instanceof net.minecraft.world.entity.animal.wolf.Wolf w) return holder(w.get(DataComponents.WOLF_VARIANT));
            if (e instanceof Horse h) {
                String coat = title(h.getVariant().name());
                String mark = switch (h.getMarkings()) {
                    case NONE -> "";
                    case WHITE -> " (White Stockings)";
                    case WHITE_FIELD -> " (White Field)";
                    case WHITE_DOTS -> " (White Dots)";
                    case BLACK_DOTS -> " (Black Dots)";
                };
                return coat + mark;
            }
            if (e instanceof Rabbit r) return switch (r.getVariant()) {
                case WHITE_SPLOTCHED -> "Black & White";
                case SALT -> "Salt & Pepper";
                case EVIL -> "Killer Bunny";
                default -> title(r.getVariant().name());
            };
            if (e instanceof Llama l) return title(l.getVariant().name());
            if (e instanceof Fox f) return title(f.getVariant().name());
            if (e instanceof Parrot p) return switch (p.getVariant()) {
                case RED_BLUE -> "Red";
                case YELLOW_BLUE -> "Yellow-Blue";
                default -> title(p.getVariant().name());
            };
            if (e instanceof Axolotl a) return switch (a.getVariant()) {
                case LUCY -> "Leucistic";
                case WILD -> "Wild";
                default -> title(a.getVariant().name());
            };
        } catch (RuntimeException ex) {
            return null;
        }
        return null;
    }

    private static String cat(String id) {
        return switch (id) {
            case "Black" -> "Tuxedo";
            case "All Black" -> "Black";
            default -> id;
        };
    }

    private static String holder(Holder<?> h) {
        if (h == null) return null;
        return h.unwrapKey().map(k -> title(k.identifier().getPath())).orElse(null);
    }

    private static String title(String id) {
        StringBuilder sb = new StringBuilder();
        for (String part : id.toLowerCase(Locale.ROOT).split("[_ ]")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
