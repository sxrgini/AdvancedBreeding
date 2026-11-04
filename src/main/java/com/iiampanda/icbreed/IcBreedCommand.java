package com.iiampanda.icbreed;

import com.iiampanda.icbreed.genetics.Ages;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Genome;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

public final class IcBreedCommand {
    private static final double RANGE = 32.0;

    private IcBreedCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("advb")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("tame").executes(ctx -> tame(ctx.getSource())))
                .then(Commands.literal("xp")
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10_000_000))
                                        .executes(ctx -> addXp(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))))));
    }

    private static int addXp(CommandSourceStack src, int amount) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.translatable("advbreeding.cmd.player_only"));
            return 0;
        }
        net.minecraft.world.entity.Mob target = lookedAt(player);
        if (target == null) {
            src.sendFailure(Component.translatable("advbreeding.cmd.no_target"));
            return 0;
        }
        if (Genetics.isWild(target)) {
            src.sendFailure(Component.translatable("advbreeding.cmd.wild"));
            return 0;
        }
        Genome before = Genetics.getOrCreate(target);
        Genetics.addXp(target, amount);
        Genome after = com.iiampanda.icbreed.platform.Att.get(target, Genetics.GENOME);
        int level = after == null ? before.level() : after.level();
        src.sendSuccess(() -> Component.translatable("advbreeding.cmd.xp_added", amount, target.getDisplayName(), level), true);
        return 1;
    }

    private static int tame(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.translatable("advbreeding.cmd.player_only"));
            return 0;
        }
        net.minecraft.world.entity.Mob target = lookedAt(player);
        if (target == null) {
            src.sendFailure(Component.translatable("advbreeding.cmd.no_target"));
            return 0;
        }
        if (!Genetics.isWild(target)) {
            src.sendFailure(Component.translatable("advbreeding.cmd.not_wild"));
            return 0;
        }
        if (Ages.isBaby(target)) {
            src.sendFailure(Component.translatable("advbreeding.cmd.baby"));
            return 0;
        }
        com.iiampanda.icbreed.taming.TamingLogic.forceTame(player.serverLevel(), target, player);
        return 1;
    }

    private static net.minecraft.world.entity.Mob lookedAt(ServerPlayer player) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getLookAngle().scale(RANGE));
        net.minecraft.world.entity.Mob best = null;
        double bestDist = Double.MAX_VALUE;
        for (net.minecraft.world.entity.Mob a : player.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().expandTowards(player.getLookAngle().scale(RANGE)).inflate(1.0))) {
            if (!Genetics.supports(a)) continue;
            var hit = a.getBoundingBox().inflate(0.3).clip(from, to);
            if (hit.isEmpty()) continue;
            double dist = from.distanceToSqr(hit.get());
            if (dist < bestDist) {
                bestDist = dist;
                best = a;
            }
        }
        return best;
    }
}
