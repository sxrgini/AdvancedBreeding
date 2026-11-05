package com.iiampanda.icbreed.genetics;

import java.util.ArrayList;
import java.util.List;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.mixin.PhenoAxolotlAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public final class AxolotlColors {
    public static final List<String> RAINBOW = List.of("advbred", "advborange", "advbyellow", "advbgreen", "advbblue", "advbindigo", "advbviolet");
    public static final String CHIMERA_PREFIX = "chimera:";

    private AxolotlColors() {
    }

    public static String keyOf(Axolotl a) {
        String custom = com.iiampanda.icbreed.platform.Att.get(a, Genetics.AXO_COLOR);
        return custom != null ? custom : a.getVariant().getSerializedName();
    }

    public static List<String> pool() {
        List<String> out = new ArrayList<>();
        for (Axolotl.Variant v : Axolotl.Variant.values()) out.add(v.getSerializedName());
        out.addAll(RAINBOW);
        return out;
    }

    public static boolean isChimera(String key) {
        return key != null && key.startsWith(CHIMERA_PREFIX);
    }

    public static String randomChimera(RandomSource random) {
        List<String> colors = pool();
        String first = colors.remove(random.nextInt(colors.size()));
        String second = colors.get(random.nextInt(colors.size()));
        return CHIMERA_PREFIX + first + ":" + second;
    }

    private static String[] sides(String key) {
        if (isChimera(key)) return key.substring(CHIMERA_PREFIX.length()).split(":");
        return new String[] {key, key};
    }

    private static String chimera(String a, String b) {
        return CHIMERA_PREFIX + a + ":" + b;
    }

    private static String pick(RandomSource random, java.util.Collection<String> excluded) {
        List<String> options = pool();
        options.removeAll(excluded);
        return options.get(random.nextInt(options.size()));
    }

    public static String chimeraFromParents(RandomSource random, String parentX, String parentY) {
        String a = sides(parentX)[random.nextInt(2)];
        String b = sides(parentY)[random.nextInt(2)];
        if (a.equals(b)) b = pick(random, List.of(a));
        return chimera(a, b);
    }

    public static String chimeraDivergent(RandomSource random, String parentX, String parentY) {
        java.util.Set<String> parents = new java.util.HashSet<>(List.of(sides(parentX)));
        parents.addAll(List.of(sides(parentY)));
        String[] split = chimeraFromParents(random, parentX, parentY).substring(CHIMERA_PREFIX.length()).split(":");
        if (random.nextBoolean()) {
            String a = pick(random, parents);
            java.util.Set<String> excluded = new java.util.HashSet<>(parents);
            excluded.add(a);
            return chimera(a, pick(random, excluded));
        }
        String fresh = pick(random, parents);
        return random.nextBoolean() ? chimera(fresh, split[1]) : chimera(split[0], fresh);
    }

    public static void apply(Axolotl axolotl, String key) {
        if (RAINBOW.contains(key) || isChimera(key)) {
            com.iiampanda.icbreed.platform.Att.set(axolotl, Genetics.AXO_COLOR, key);
            return;
        }
        com.iiampanda.icbreed.platform.Att.remove(axolotl, Genetics.AXO_COLOR);
        for (Axolotl.Variant v : Axolotl.Variant.values()) {
            if (v.getSerializedName().equals(key)) {
                ((PhenoAxolotlAccessor) (Object) axolotl).icbreed$set(v);
                return;
            }
        }
    }

    public static void rollWild(Axolotl axolotl) {
        if (axolotl.getVariant() == Axolotl.Variant.BLUE) return;
        int commonVariants = 0;
        for (Axolotl.Variant v : Axolotl.Variant.values()) if (v != Axolotl.Variant.BLUE) commonVariants++;
        int pick = axolotl.getRandom().nextInt(commonVariants + RAINBOW.size());
        if (pick < RAINBOW.size()) com.iiampanda.icbreed.platform.Att.set(axolotl, Genetics.AXO_COLOR, RAINBOW.get(pick));
    }

    public static void rollEgg(Axolotl axolotl) {
        if (axolotl.getRandom().nextDouble() < com.iiampanda.icbreed.IcBreedConfig.get().axolotlChimeraEggChance) {
            com.iiampanda.icbreed.platform.Att.set(axolotl, Genetics.AXO_COLOR, randomChimera(axolotl.getRandom()));
        }
    }

    public static Identifier texture(String key, boolean baby) {
        String suffix = baby ? "_baby" : "";
        String path;
        if (isChimera(key)) {
            String[] parts = key.substring(CHIMERA_PREFIX.length()).split(":");
            path = "textures/entity/axolotl/chimera/" + parts[0] + "__" + parts[1] + suffix + ".png";
        } else {
            path = "textures/entity/axolotl/" + key + suffix + ".png";
        }
        return Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, path);
    }

    public static Identifier plainTexture(String key) {
        if (key.startsWith("advb")) return Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "textures/entity/axolotl/" + key + ".png");
        return Identifier.withDefaultNamespace("textures/entity/axolotl/axolotl_" + key + ".png");
    }

    public static String displayName(String key) {
        if (isChimera(key)) {
            String[] parts = key.substring(CHIMERA_PREFIX.length()).split(":");
            return "Chimera (" + title(parts[0]) + " / " + title(parts[1]) + ")";
        }
        return title(key);
    }

    private static String title(String id) {
        if (id.startsWith("advb")) id = id.substring(4);
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
