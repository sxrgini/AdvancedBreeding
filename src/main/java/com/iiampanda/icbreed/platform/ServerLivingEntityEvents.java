package com.iiampanda.icbreed.platform;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class ServerLivingEntityEvents {
    public static final Ev<AllowDeath> ALLOW_DEATH = new Ev<>();
    public static final Ev<AfterDeath> AFTER_DEATH = new Ev<>();
    public static final Ev<AfterDamage> AFTER_DAMAGE = new Ev<>();

    private ServerLivingEntityEvents() {
    }

    public interface AllowDeath {
        boolean allowDeath(LivingEntity entity, DamageSource source, float amount);
    }

    public interface AfterDeath {
        void afterDeath(LivingEntity entity, DamageSource source);
    }

    public interface AfterDamage {
        void afterDamage(LivingEntity entity, DamageSource source, float baseDamage, float damageTaken, boolean blocked);
    }
}
