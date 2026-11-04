package com.iiampanda.icbreed.compat.vb;

import com.blackgear.vanillabackport.common.api.modules.mob_variant.VariantDataHolder;
import com.blackgear.vanillabackport.common.api.modules.mob_variant.VariantUtils;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.chicken.ChickenVariants;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.cow.CowVariants;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.pig.PigVariants;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.camel.CamelHusk;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.happy_ghast.HappyGhast;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus;
import com.iiampanda.icbreed.compat.Compat;
import java.util.Optional;
import net.minecraft.world.entity.Mob;

public final class VbCompat {
    private VbCompat() {
    }

    public static void init() {
        Compat.tree(m -> m instanceof HappyGhast ? "happy_ghast" : m instanceof AbstractNautilus ? "nautilus" : null);
        Compat.untameable(m -> m instanceof CamelHusk);
        Compat.inheritor(VbCompat::inheritVariant);
        Compat.phenotype(VbCompat::variantName);
        GhastDeck.register();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean inheritVariant(Mob baby, Mob a, Mob b, double chance) {
        Optional<VariantDataHolder<Object>> child = VariantDataHolder.getHolder(baby);
        Optional<VariantDataHolder<Object>> pa = VariantDataHolder.getHolder(a);
        Optional<VariantDataHolder<Object>> pb = VariantDataHolder.getHolder(b);
        if (child.isEmpty() || pa.isEmpty() || pb.isEmpty()) return false;
        Optional<Object> va = pa.get().getVariantData();
        Optional<Object> vb = pb.get().getVariantData();
        if (va.isEmpty() || vb.isEmpty()) return false;
        child.get().setVariantData(baby.getRandom().nextBoolean() ? va.get() : vb.get());
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String variantName(net.minecraft.world.entity.Entity e) {
        com.blackgear.platform.core.BuiltInCoreRegistry registry;
        if (e instanceof net.minecraft.world.entity.animal.Pig) registry = PigVariants.REGISTRIES;
        else if (e instanceof net.minecraft.world.entity.animal.Cow && !(e instanceof net.minecraft.world.entity.animal.MushroomCow)) registry = CowVariants.REGISTRIES;
        else if (e instanceof net.minecraft.world.entity.animal.Chicken) registry = ChickenVariants.REGISTRIES;
        else return null;
        Optional<VariantDataHolder<Object>> holder = VariantDataHolder.getHolder(e);
        if (holder.isEmpty()) return null;
        Optional<Object> data = holder.get().getVariantData();
        if (data.isEmpty()) return null;
        String id = VariantUtils.getID(registry, data.get());
        if (id == null) return null;
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        StringBuilder sb = new StringBuilder();
        for (String part : path.split("[_ ]")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
