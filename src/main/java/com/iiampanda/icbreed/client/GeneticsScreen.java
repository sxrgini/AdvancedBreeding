package com.iiampanda.icbreed.client;

import java.util.ArrayList;
import java.util.List;

import com.iiampanda.icbreed.NameHelper;
import com.iiampanda.icbreed.genetics.Bloodline;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Genome;
import com.iiampanda.icbreed.genetics.Pregnancy;
import com.iiampanda.icbreed.genetics.Genotype;
import com.iiampanda.icbreed.genetics.Sex;
import com.iiampanda.icbreed.genetics.Taming;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Stat;
import com.iiampanda.icbreed.genetics.Traits;
import com.iiampanda.icbreed.net.UpgradeTraitPayload;
import com.iiampanda.icbreed.traits.TraitDef;
import com.iiampanda.icbreed.traits.TraitTrees;
import com.iiampanda.icbreed.net.RenamePayload;
import com.iiampanda.icbreed.IcBreedConfig;
import net.minecraft.client.gui.components.EditBox;
import com.iiampanda.icbreed.net.OpenInventoryPayload;
import com.iiampanda.icbreed.platform.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class GeneticsScreen extends Screen {
    private static final int W = 256;
    private static final int H = 284;
    private static final int TEXT = 0xFF404040;
    private static final ResourceLocation XP_BG = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation XP_FG = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");
    private static final int XP_W = 182;
    private static final int XP_H = 5;

    private final LivingEntity entity;
    private final Genome openedGenome;
    private final boolean canOpenInventory;
    private final int openedPregnantTicks;

    private static final int TAME_COLOR_TOP = 0xFFD9A8FF;
    private static final int TAME_COLOR = 0xFFA255E8;
    private static final int TAME_COLOR_BOTTOM = 0xFF6B2FA6;

    private EditBox nameBox;
    private boolean editing;

    private enum Page { MAIN, TRAITS, STATS }

    private Page page = Page.MAIN;
    private double treeOffX;
    private double treeOffY;
    private boolean dragged;

    private static final int TAB_W = 28;
    private static final int TAB_H = 32;
    private static final int NODE = 26;
    private static final int COL_STEP = 50;
    private static final int ROW_STEP = 35;
    private static final ResourceLocation TAB_TOP = ResourceLocation.withDefaultNamespace("advancements/tab_right_top");
    private static final ResourceLocation TAB_TOP_SEL = ResourceLocation.withDefaultNamespace("advancements/tab_right_top_selected");
    private static final ResourceLocation TAB_MIDDLE = ResourceLocation.withDefaultNamespace("advancements/tab_right_middle");
    private static final ResourceLocation TAB_MIDDLE_SEL = ResourceLocation.withDefaultNamespace("advancements/tab_right_middle_selected");
    private static final ResourceLocation TAB_BOTTOM = ResourceLocation.withDefaultNamespace("advancements/tab_right_bottom");
    private static final ResourceLocation TAB_BOTTOM_SEL = ResourceLocation.withDefaultNamespace("advancements/tab_right_bottom_selected");
    private static final ResourceLocation FRAME_OFF = ResourceLocation.withDefaultNamespace("advancements/task_frame_unobtained");
    private static final ResourceLocation FRAME_ON = ResourceLocation.withDefaultNamespace("advancements/task_frame_obtained");
    private static final ResourceLocation FRAME_MAX = ResourceLocation.withDefaultNamespace("advancements/goal_frame_obtained");

    private int left;
    private int top;

    public GeneticsScreen(LivingEntity entity, Genome genome, boolean canOpenInventory, int pregnantTicks) {
        super(Component.translatable("advbreeding.gui.title"));
        this.entity = entity;
        this.openedGenome = genome;
        this.canOpenInventory = canOpenInventory;
        this.openedPregnantTicks = pregnantTicks;
    }

    @Override
    protected void init() {
        left = (this.width - W) / 2;
        top = (this.height - H) / 2;
        nameBox = new EditBox(this.font, left + 8, top + 29, 168, 12, Component.translatable("advbreeding.gui.rename_type"));
        nameBox.setMaxLength(32);
        nameBox.setHint(Component.translatable("advbreeding.gui.rename_type"));
        nameBox.setVisible(editing);
        this.addRenderableWidget(nameBox);
        stayButton = Button.builder(stayLabel(), b -> ClientPlayNetworking.send(new com.iiampanda.icbreed.net.StayPutPayload(entity.getId())))
                .bounds(left + 8, top + 119, 76, 12).build();
        this.addRenderableWidget(stayButton);
    }

    private Button stayButton;

    public static final java.util.Map<Integer, int[]> STAY = new java.util.concurrent.ConcurrentHashMap<>();

    private Component stayLabel() {
        int[] known = STAY.get(entity.getId());
        int mode = known != null ? known[0] : 0;
        return Component.translatable(mode == 1 ? "advbreeding.gui.stay_put" : mode == 2 ? "advbreeding.gui.follow" : "advbreeding.gui.wander");
    }

    private boolean isWild() {
        Taming t = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.TAMING);
        return t != null && !t.tamed();
    }

    private boolean overName(double mx, double my) {
        return mx >= left + 8 && mx < left + 176 && my >= top + 29 && my < top + 41;
    }

    private void startEditing() {
        editing = true;
        nameBox.setVisible(true);
        nameBox.setValue(entity.hasCustomName() ? NameHelper.rawName(entity).getString() : "");
        this.setFocused(nameBox);
        nameBox.setFocused(true);
    }

    private void stopEditing(boolean save) {
        if (!editing) return;
        editing = false;
        nameBox.setVisible(false);
        nameBox.setFocused(false);
        if (save) {
            String current = entity.hasCustomName() ? NameHelper.rawName(entity).getString() : "";
            String typed = nameBox.getValue().strip();
            if (!typed.equals(current)) ClientPlayNetworking.send(new RenamePayload(entity.getId(), typed));
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int tab = tabAt(mx, my);
            if (tab >= 0) {
                stopEditing(true);
                clickTab(tab);
                return true;
            }
            if (page == Page.TRAITS) {
                dragged = false;
                TraitDef hit = nodeAt(mx, my);
                if (hit != null) {
                    tryBuy(hit);
                    return true;
                }
                return true;
            }
            if (page == Page.STATS) return true;
            if (overName(mx, my)) {
                if (!editing && !isWild()) startEditing();
                return true;
            }
            if (editing) stopEditing(true);
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (page == Page.TRAITS && button == 0 && inViewport(mx, my)) {
            treeOffX += dx;
            treeOffY += dy;
            dragged = true;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    private void setPage(Page p) {
        page = p;
    }

    @Override
    public boolean keyPressed(int k, int scan, int mods) {
        if (editing) {
            if (k == 257 || k == 335) {
                stopEditing(true);
                return true;
            }
            if (k == 256) {
                stopEditing(false);
                return true;
            }
        }
        return super.keyPressed(k, scan, mods);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !editing;
    }

    @Override
    public void removed() {
        stopEditing(true);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        panel(ctx, left, top, W, H);
    }

    private Component baseName() {
        return NameHelper.rawName(entity);
    }

    private static Component sexSymbol(Sex sex) {
        return Component.literal(sex.symbol).withStyle(Style.EMPTY.withColor(darken(sex.color)));
    }

    private Component nameWithSymbol(String name, Sex sex) {
        return Component.literal(name + " ").append(sexSymbol(sex));
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        if (stayButton != null) {
            stayButton.visible = page == Page.MAIN && !isWild() && !editing;
            int[] st = STAY.get(entity.getId());
            stayButton.active = stayButton.visible && (st == null || st[1] == 1);
            stayButton.setMessage(stayLabel());
        }
        super.render(ctx, mouseX, mouseY, delta);

        Genome live = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.GENOME);
        Genome genome = live != null ? live : openedGenome;
        Pregnancy pregnancy = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.PREGNANCY);
        int pregnantTicks = openedPregnantTicks;
        if (pregnancy != null && this.minecraft != null && this.minecraft.level != null) {
            pregnantTicks = (int) pregnancy.ticksLeft(this.minecraft.level.getGameTime());
        } else if (live != null) {
            pregnantTicks = -1;
        }

        int level = genome.level();
        int barX = left + (W - XP_W) / 2;
        int barY = top + 16;
        ctx.blitSprite(XP_BG, barX, barY, XP_W, XP_H);
        float progress = 1.0F;
        int into = 0;
        int need = 0;
        if (level < Genome.MAX_LEVEL) {
            into = genome.xp - Genome.xpForLevel(level);
            need = Genome.xpForLevel(level + 1) - Genome.xpForLevel(level);
            progress = need <= 0 ? 0 : Math.min(1.0F, into / (float) need);
        }
        int filled = (int) (progress * XP_W);
        if (filled > 0) {
            ctx.blitSprite(XP_FG, XP_W, XP_H, 0, 0, barX, barY, filled, XP_H);
        }
        String lvl = String.valueOf(level);
        int lx = left + (W - this.font.width(lvl)) / 2;
        int ly = top + 5;
        ctx.drawString(this.font, lvl, lx + 1, ly, 0xFF000000, false);
        ctx.drawString(this.font, lvl, lx - 1, ly, 0xFF000000, false);
        ctx.drawString(this.font, lvl, lx, ly + 1, 0xFF000000, false);
        ctx.drawString(this.font, lvl, lx, ly - 1, 0xFF000000, false);
        ctx.drawString(this.font, lvl, lx, ly, 0xFF80FF20, false);
        boolean overBar = mouseX >= barX && mouseX < barX + XP_W && mouseY >= barY - 12 && mouseY <= barY + XP_H + 1;

        Integer growth = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.GROWTH);
        int tameY = barY + XP_H + 2;
        boolean overTame = false;
        if (growth != null) {
            ctx.blitSprite(XP_BG, barX, tameY, XP_W, XP_H);
            int tf = (int) (Math.max(0, Math.min(100, growth)) / 100.0 * XP_W);
            if (tf > 0) {
                ctx.fill(barX, tameY, barX + tf, tameY + 1, TAME_COLOR_TOP);
                ctx.fill(barX, tameY + 1, barX + tf, tameY + 4, TAME_COLOR);
                ctx.fill(barX, tameY + 4, barX + tf, tameY + 5, TAME_COLOR_BOTTOM);
            }
            overTame = mouseX >= barX && mouseX < barX + XP_W && mouseY >= tameY - 1 && mouseY <= tameY + XP_H + 1;
        }

        if (page != Page.MAIN) {
            List<Component> tip = page == Page.TRAITS
                    ? renderTraits(ctx, mouseX, mouseY, genome) : renderStats(ctx, mouseX, mouseY, genome);
            if (tip == null && overBar) {
                tip = new ArrayList<>();
                tip.add(Component.translatable("advbreeding.gui.xp_title", level, Genome.MAX_LEVEL).withStyle(ChatFormatting.GREEN));
                if (level < Genome.MAX_LEVEL) tip.add(Component.translatable("advbreeding.gui.xp_next", into, need).withStyle(ChatFormatting.GRAY));
            }
            List<Component> tabTip = renderTabs(ctx, mouseX, mouseY);
            if (tabTip != null) tip = tabTip;
            if (tip != null) ctx.renderComponentTooltip(this.font, tip, mouseX, mouseY);
            return;
        }

        if (!editing) {
            ctx.drawString(this.font, baseName().copy().append(" ").append(sexSymbol(genome.sex)), left + 8, top + 31, TEXT, false);
        }
        boolean overTitle = !editing && overName(mouseX, mouseY);

        int bx = left + 8, by = top + 43, bw = 76, bh = 74;
        inset(ctx, bx, by, bw, bh);
        float size = (float) Math.max(entity.getBbWidth() * 1.15, entity.getBbHeight());
        InventoryScreen.renderEntityInInventoryFollowsMouse(ctx, bx + 1, by + 1, bx + bw - 1, by + bh - 1,
                (int) Math.max(8, 52 / size), 0.0625F, mouseX, mouseY, entity);

        Bloodline bl = genome.bloodline;
        Component[] labels = {
                Component.translatable("advbreeding.gui.sex"),
                Component.translatable("advbreeding.gui.bloodline"),
                Component.translatable("advbreeding.gui.generation"),
                Component.translatable("advbreeding.gui.points"),
                Component.translatable("advbreeding.gui.pregnant"),
                Component.translatable("advbreeding.gui.status")
        };
        int ix = left + 92;
        int widest = 0;
        for (Component c : labels) widest = Math.max(widest, this.font.width(c));
        int vx = ix + widest + 8;
        int y = top + 43;
        ctx.drawString(this.font, labels[0], ix, y, TEXT, false);
        ctx.drawString(this.font, Component.literal(genome.sex.symbol + " ").append(Component.translatable("advbreeding.sex." + genome.sex.id)),
                vx, y, 0xFF000000 | darken(genome.sex.color), false);
        y += 12;
        ctx.drawString(this.font, labels[1], ix, y, TEXT, false);
        fitText(ctx, Component.translatable(bl.nameKey()), vx, y, 0xFF000000 | darken(bl.color), left + W - 8 - vx);
        y += 12;
        ctx.drawString(this.font, labels[2], ix, y, TEXT, false);
        ctx.drawString(this.font, String.valueOf(genome.generation), vx, y, TEXT, false);
        y += 12;
        ctx.drawString(this.font, labels[3], ix, y, TEXT, false);
        ctx.drawString(this.font, String.valueOf(Genetics.availablePoints(genome, com.iiampanda.icbreed.platform.Att.get(entity, Genetics.TRAITS), TraitTrees.forEntity(entity))), vx, y, TEXT, false);
        y += 12;
        Taming taming = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.TAMING);
        boolean wild = taming != null && !taming.tamed();
        boolean taming2 = wild && taming.orphan() && entity != null && com.iiampanda.icbreed.genetics.Ages.isBaby(entity);
        ctx.drawString(this.font, labels[5], ix, y, TEXT, false);
        Component statusText = Component.translatable(!wild ? "advbreeding.gui.status.tamed" : taming2 ? "advbreeding.gui.status.taming" : "advbreeding.gui.status.wild");
        fitText(ctx, statusText, vx, y, !wild ? 0xFF2E7D32 : taming2 ? 0xFF7A3E9D : 0xFF8A5A00, left + W - 8 - vx);
        boolean overStatus = mouseX >= ix && mouseX < left + W - 8 && mouseY >= y - 1 && mouseY < y + 10;
        y += 12;
        String pheno = Phenotypes.of(entity);
        if (pheno != null) {
            ctx.drawString(this.font, Component.translatable("advbreeding.gui.phenotype"), ix, y, TEXT, false);
            int avail = left + W - 8 - vx;
            int tw = this.font.width(pheno);
            if (tw > avail) {
                float sc = (float) avail / tw;
                ctx.pose().pushPose();
                ctx.pose().translate((float) vx, y + (1 - sc) * 4, 0.0F);
                ctx.pose().scale(sc, sc, 1.0F);
                ctx.drawString(this.font, pheno, 0, 0, TEXT, false);
                ctx.pose().popPose();
            } else {
                ctx.drawString(this.font, pheno, vx, y, TEXT, false);
            }
            y += 12;
        }
        if (pregnantTicks >= 0) {
            int secs = (pregnantTicks + 19) / 20;
            ctx.drawString(this.font, labels[4], ix, y, TEXT, false);
            ctx.drawString(this.font, String.format("%d:%02d", secs / 60, secs % 60), vx, y, 0xFF7A3E9D, false);
            y += 12;
        }
        if (entity instanceof net.minecraft.world.entity.animal.Cow && !((net.minecraft.world.entity.animal.Cow) entity).isBaby()
                && this.minecraft != null && this.minecraft.level != null) {
            Long ready = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.MILK_READY);
            long left2 = ready == null ? 0 : ready - this.minecraft.level.getGameTime();
            ctx.drawString(this.font, Component.translatable(entity instanceof net.minecraft.world.entity.animal.MushroomCow ? "advbreeding.gui.milk_stew" : "advbreeding.gui.milk"), ix, y, TEXT, false);
            if (left2 <= 0) {
                ctx.drawString(this.font, Component.translatable("advbreeding.gui.milk_ready"), vx, y, 0xFF2E7D32, false);
            } else {
                int secs = (int) ((left2 + 19) / 20);
                ctx.drawString(this.font, String.format("%d:%02d", secs / 60, secs % 60), vx, y, 0xFF7A3E9D, false);
            }
            y += 12;
        }
        if (genome.hasParents()) {
            Component parents = Component.translatable("advbreeding.gui.parents",
                    nameWithSymbol(genome.motherName.isEmpty() ? "?" : genome.motherName, Sex.FEMALE),
                    nameWithSymbol(genome.fatherName.isEmpty() ? "?" : genome.fatherName, Sex.MALE));
            int ly2 = y;
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(parents, left + W - 8 - ix)) {
                ctx.drawString(this.font, line, ix, ly2, TEXT, false);
                ly2 += 9;
            }
        }

        int gy = top + 133;
        ctx.drawString(this.font, Component.translatable("advbreeding.gui.genotypes").withStyle(ChatFormatting.BOLD), left + 8, gy, TEXT, false);
        gy += 12;
        List<Component> tooltip = null;
        for (Genotype g : Genotype.ALL) {
            int v = genome.get(g);
            int rowY = gy + g.ordinal() * 12;
            int color = valueColor(v);
            ctx.drawString(this.font, g.abbr, left + 8, rowY + 1, 0xFF000000 | darken(color), false);
            ctx.drawString(this.font, Component.translatable(g.nameKey()), left + 32, rowY + 1, TEXT, false);

            int sx0 = left + 108;
            for (int i = 0; i < Genotype.MAX; i++) {
                int sx = sx0 + i * 12;
                ctx.fill(sx, rowY, sx + 11, rowY + 9, 0xFF373737);
                ctx.fill(sx + 1, rowY + 1, sx + 10, rowY + 8, i < v ? 0xFF000000 | color : 0xFF8B8B8B);
            }

            ctx.fill(sx0 + Genotype.BASELINE * 12 - 1, rowY - 1, sx0 + Genotype.BASELINE * 12, rowY + 10, 0xFFFFFFFF);
            ctx.drawString(this.font, String.valueOf(v), sx0 + 10 * 12 + 4, rowY + 1, TEXT, false);

            if (mouseX >= left + 8 && mouseX <= sx0 + 10 * 12 + 14 && mouseY >= rowY - 1 && mouseY <= rowY + 10) {
                tooltip = new ArrayList<>();
                tooltip.add(Component.translatable(g.nameKey()).withStyle(ChatFormatting.BOLD));
                int d = v - Genotype.BASELINE;

                double per = v < Genotype.BASELINE ? -g.perLevel : g.perLevel;
                tooltip.add(Component.translatable(g.descKey()).append(" " + pct(per))
                        .withStyle(v < Genotype.BASELINE ? ChatFormatting.RED : ChatFormatting.GREEN));
                tooltip.add(Component.translatable("advbreeding.gui.trait_level", v, Genotype.MAX)
                        .append(Component.literal("  (" + (d >= 0 ? "+" : "") + d + ")")
                                .withStyle(d > 0 ? ChatFormatting.GREEN : d < 0 ? ChatFormatting.RED : ChatFormatting.GRAY)));
            }
        }

        if (overTitle) {
            tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(isWild() ? "advbreeding.gui.rename_wild" : "advbreeding.gui.rename_hint")
                    .withStyle(isWild() ? ChatFormatting.RED : ChatFormatting.GRAY));
        }
        if (overTame && growth != null) {
            tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(wild ? "advbreeding.gui.taming_progress" : "advbreeding.gui.growth_progress", growth)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (overStatus && wild) {
            tooltip = new ArrayList<>();
            long now = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0;
            tooltip.add(Component.translatable("advbreeding.gui.taming_title").withStyle(ChatFormatting.BOLD));
            if (!com.iiampanda.icbreed.genetics.Ages.isBaby(entity)) {
                tooltip.add(Component.translatable("advbreeding.gui.wild_adult").withStyle(ChatFormatting.GRAY));
            } else if (!taming.orphan()) {
                tooltip.add(Component.translatable("advbreeding.gui.wild_baby").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(req(true, Component.translatable("advbreeding.gui.req_parent")));
                tooltip.add(req(taming.started() || entity instanceof net.minecraft.world.entity.Leashable l && l.isLeashed(),
                        Component.translatable("advbreeding.gui.req_led")));
                tooltip.add(req(taming.pen(), Component.translatable("advbreeding.gui.req_pen")));
                tooltip.add(req(taming.fed(now), Component.translatable("advbreeding.gui.req_fed")));
                tooltip.add(req(taming.bed(), Component.translatable("advbreeding.gui.req_bed", IcBreedConfig.get().bedRadius)));
            }
        }
        if (overBar) {
            tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("advbreeding.gui.xp_title", level, Genome.MAX_LEVEL).withStyle(ChatFormatting.GREEN));
            if (level < Genome.MAX_LEVEL) {
                tooltip.add(Component.translatable("advbreeding.gui.xp_next", into, need).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("advbreeding.gui.xp_max").withStyle(ChatFormatting.GOLD));
            }
        }

        int ey = gy + Genotype.ALL.length * 12 + 4;
        ctx.drawString(this.font, Component.translatable("advbreeding.gui.bloodline_effects", Component.translatable(bl.nameKey())).withStyle(ChatFormatting.BOLD), left + 8, ey, TEXT, false);
        ey += 11;
        for (int li = 0; li < bl.mods.length; li++) {
            fitText(ctx, bl.lineComponent(li), left + 12, ey, TEXT, left + W - 8 - (left + 12));
            ey += 10;
        }

        List<Component> tabTip = renderTabs(ctx, mouseX, mouseY);
        if (tabTip != null) tooltip = tabTip;
        if (tooltip != null) ctx.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }

    private enum Tab { TRAITS, STATS, INVENTORY, BAGS }

    private List<Tab> tabs() {
        List<Tab> l = new ArrayList<>(List.of(Tab.TRAITS, Tab.STATS));
        if (canOpenInventory) l.add(Tab.INVENTORY);
        if (com.iiampanda.icbreed.platform.Att.get(entity, Genetics.BAGS) != null) l.add(Tab.BAGS);
        return l;
    }

    private int tabAt(double mx, double my) {
        int x = left + W - 4;
        if (mx < x || mx >= x + TAB_W) return -1;
        for (int i = 0; i < tabs().size(); i++) {
            int y = top + TAB_H * i + 8;
            if (my >= y && my < y + TAB_H) return i;
        }
        return -1;
    }

    private void clickTab(int index) {
        Tab t = tabs().get(index);
        switch (t) {
            case INVENTORY -> ClientPlayNetworking.send(new OpenInventoryPayload(entity.getId()));
            case BAGS -> ClientPlayNetworking.send(new com.iiampanda.icbreed.net.OpenBagsPayload(entity.getId()));
            case TRAITS -> setPage(page == Page.TRAITS ? Page.MAIN : Page.TRAITS);
            case STATS -> setPage(page == Page.STATS ? Page.MAIN : Page.STATS);
        }
    }

    private void fitText(GuiGraphics ctx, Component text, int x, int y, int color, int maxWidth) {
        int tw = this.font.width(text);
        if (tw <= maxWidth) {
            ctx.drawString(this.font, text, x, y, color, false);
            return;
        }
        float sc = (float) maxWidth / tw;
        ctx.pose().pushPose();
        ctx.pose().translate((float) x, y + (1 - sc) * 4, 0.0F);
        ctx.pose().scale(sc, sc, 1.0F);
        ctx.drawString(this.font, text, 0, 0, color, false);
        ctx.pose().popPose();
    }

    private List<Component> renderTabs(GuiGraphics ctx, int mouseX, int mouseY) {
        int x = left + W - 4;
        List<Component> tip = null;
        List<Tab> tabs = tabs();
        int count = tabs.size();
        for (int i = 0; i < count; i++) {
            Tab t = tabs.get(i);
            boolean selected = (t == Tab.TRAITS && page == Page.TRAITS) || (t == Tab.STATS && page == Page.STATS);
            ResourceLocation sprite = i == 0 ? (selected ? TAB_TOP_SEL : TAB_TOP)
                    : i == count - 1 ? (selected ? TAB_BOTTOM_SEL : TAB_BOTTOM)
                    : (selected ? TAB_MIDDLE_SEL : TAB_MIDDLE);
            int y = top + TAB_H * i + 8;
            ctx.blitSprite(sprite, x, y, TAB_W, TAB_H);
            net.minecraft.world.item.Item icon = switch (t) {
                case TRAITS -> net.minecraft.world.item.Items.OAK_SAPLING;
                case STATS -> net.minecraft.world.item.Items.BOOK;
                case INVENTORY -> net.minecraft.world.item.Items.CHEST;
                case BAGS -> {
                    Integer bagStyle = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.BAG_STYLE);
                    yield com.iiampanda.icbreed.traits.SaddleBags.itemFor(bagStyle == null ? 0 : bagStyle).getItem();
                }
            };
            ctx.renderItem(new net.minecraft.world.item.ItemStack(icon), x + 5, y + 6);
            if (mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < y + TAB_H) {
                tip = new ArrayList<>();
                tip.add(Component.translatable(switch (t) {
                    case TRAITS -> "advbreeding.gui.tab.traits";
                    case STATS -> "advbreeding.gui.tab.stats";
                    case INVENTORY -> "advbreeding.gui.inventory";
                    case BAGS -> "advbreeding.gui.bags";
                }));
                if (selected) tip.add(Component.translatable("advbreeding.gui.tab_hint").withStyle(ChatFormatting.GRAY));
            }
        }
        return tip;
    }

    private int vpX() { return left + 8; }
    private int vpY() { return top + 42; }
    private int vpW() { return W - 16; }
    private int vpH() { return H - 50; }

    private boolean inViewport(double mx, double my) {
        return mx >= vpX() && mx < vpX() + vpW() && my >= vpY() && my < vpY() + vpH();
    }

    private int nodeX(TraitDef d) {
        return (int) (vpX() + 8 + treeOffX + d.col() * COL_STEP);
    }

    private int nodeY(TraitDef d) {
        return (int) (vpY() + 4 + treeOffY + d.row() * ROW_STEP);
    }

    private TraitDef nodeAt(double mx, double my) {
        if (!inViewport(mx, my)) return null;
        TraitTrees.Tree tree = TraitTrees.forEntity(entity);
        for (TraitDef d : tree.nodes) {
            int x = nodeX(d), y = nodeY(d);
            if (mx >= x && mx < x + NODE && my >= y && my < y + NODE) return d;
        }
        return null;
    }

    private Traits traits() {
        Traits t = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.TRAITS);
        return t == null ? Traits.NONE : t;
    }

    private int points() {
        Genome g = com.iiampanda.icbreed.platform.Att.get(entity, Genetics.GENOME);
        return g == null ? 0 : Genetics.availablePoints(g, com.iiampanda.icbreed.platform.Att.get(entity, Genetics.TRAITS), TraitTrees.forEntity(entity));
    }

    private boolean canBuy(TraitDef d) {
        if (isWild()) return false;
        Traits t = traits();
        if (t.rank(d.id()) >= d.maxRank() || points() < d.cost()) return false;
        return d.missing(t::rank) == null;
    }

    private void tryBuy(TraitDef d) {
        if (dragged || !canBuy(d)) return;
        ClientPlayNetworking.send(new UpgradeTraitPayload(entity.getId(), d.id()));
    }

    private List<Component> renderTraits(GuiGraphics ctx, int mouseX, int mouseY, Genome genome) {
        TraitTrees.Tree tree = TraitTrees.forEntity(entity);
        Traits traits = traits();
        ctx.drawString(this.font, Component.translatable("advbreeding.gui.tab.traits").withStyle(ChatFormatting.BOLD), left + 8, top + 31, TEXT, false);
        Component pts = Component.translatable("advbreeding.gui.trait_points", points());
        ctx.drawString(this.font, pts, left + W - 8 - this.font.width(pts), top + 31, 0xFF2E7D32, false);

        int x0 = vpX(), y0 = vpY(), x1 = x0 + vpW(), y1 = y0 + vpH();
        inset(ctx, x0 - 1, y0 - 1, vpW() + 2, vpH() + 2);
        ctx.fill(x0, y0, x1, y1, 0xFF161616);
        ctx.enableScissor(x0, y0, x1, y1);

        for (TraitDef d : tree.nodes) {
            for (String pid : new String[] {d.parent(), d.parent2()}) {
                if (pid == null) continue;
                TraitDef p = tree.get(pid);
                if (p == null) continue;
                int color = traits.rank(d.id()) > 0 ? 0xFFFFFFFF : traits.rank(p.id()) > 0 ? 0xFF9A9A9A : 0xFF4A4A4A;
                int px = nodeX(p) + NODE / 2, py = nodeY(p) + NODE / 2;
                int cx = nodeX(d) + NODE / 2, cy = nodeY(d) + NODE / 2;
                int mid = (px + cx) / 2;
                ctx.fill(Math.min(px, mid), py - 1, Math.max(px, mid) + 1, py + 1, color);
                ctx.fill(mid - 1, Math.min(py, cy), mid + 1, Math.max(py, cy) + 1, color);
                ctx.fill(Math.min(mid, cx), cy - 1, Math.max(mid, cx) + 1, cy + 1, color);
            }
        }

        TraitDef hover = null;
        for (TraitDef d : tree.nodes) {
            int x = nodeX(d), y = nodeY(d);
            int rank = traits.rank(d.id());
            boolean unlocked = d.missing(traits::rank) == null;
            ResourceLocation frame = rank >= d.maxRank() ? FRAME_MAX : rank > 0 ? FRAME_ON : FRAME_OFF;
            ctx.blitSprite(frame, x, y, NODE, NODE);
            var effectIcon = effectIcon(d.id());
            if (effectIcon != null) ctx.blit(x + 4, y + 4, 0, 18, 18, this.minecraft.getMobEffectTextures().get(effectIcon));
            else ctx.renderItem(d.iconStack(), x + 5, y + 5);
            if (!unlocked) ctx.fill(x + 3, y + 3, x + NODE - 3, y + NODE - 3, 0x99000000);
            String r = rank + "/" + d.maxRank();
            ctx.drawString(this.font, r, x + (NODE - this.font.width(r)) / 2, y + NODE - 1, rank >= d.maxRank() ? 0xFFFFD84A : 0xFFE0E0E0, true);
            if (mouseX >= x && mouseX < x + NODE && mouseY >= y && mouseY < y + NODE && inViewport(mouseX, mouseY)) hover = d;
        }
        ctx.disableScissor();

        if (hover == null) return null;
        int rank = traits.rank(hover.id());
        List<Component> tip = new ArrayList<>();
        tip.add(hover.displayName().copy().withStyle(ChatFormatting.BOLD));
        tip.add(Component.translatable("advbreeding.gui.trait_rank", rank, hover.maxRank()).withStyle(ChatFormatting.GRAY));
        if (hover.cost() > 1) {
            tip.add(Component.translatable("advbreeding.gui.trait_cost", hover.cost()).withStyle(ChatFormatting.GRAY));
        }
        Component traitDesc = hover.id().equals("shoulder_pal") && entity instanceof net.minecraft.world.entity.ambient.Bat
                ? Component.translatable("advbreeding.trait.shoulder_pal.desc.bat") : hover.descriptionComponent();
        if (traitDesc != null) {
            tip.add(traitDesc.copy().withStyle(ChatFormatting.GREEN));
        }
        if (hover.id().equals("polly_pickup")) {
            tip.add(Component.translatable("advbreeding.gui.trait_per_level").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("advbreeding.trait.polly_pickup.per_level").withStyle(ChatFormatting.GREEN)));
        }
        if (!hover.mods().isEmpty() && hover.mods().get(0).stat() != Stat.PRODUCTION
                && !java.util.Set.of("hunters_mark", "rip_and_tear", "regen_aura", "axolotl_hat", "saturation_aura", "nine_lives", "cat_scratches", "shearable_feathers", "metal_scraps", "feral_side", "rideable", "fire_resistance_aura", "lava_walker", "captain_now", "buildable", "water_breather", "underwater_vision", "pollination", "increased_honey_production", "honey_block_production", "shoulder_pal", "elytra_boost", "bat_glider", "polly_pickup", "llama_thick_hide", "phenotype_mutation", "very_suspicious", "hoppy_aura", "poison_touch").contains(hover.id())) {
            tip.add(Component.translatable("advbreeding.gui.trait_per_rank").withStyle(ChatFormatting.DARK_GRAY));
            for (TraitDef.Mod m : hover.mods()) {
                if (m.stat() == Stat.PRODUCTION) continue;
                boolean good = isGood(m.stat(), m.perRank());
                tip.add(Component.literal(m.stat().format(m.perRank()) + " ")
                        .append(Component.translatable(m.stat().nameKey()))
                        .withStyle(good ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
        }
        if (isWild()) {
            tip.add(Component.translatable("advbreeding.gui.trait_wild").withStyle(ChatFormatting.RED));
        } else if (rank >= hover.maxRank()) {
            tip.add(Component.translatable("advbreeding.gui.trait_maxed").withStyle(ChatFormatting.GOLD));
        } else if (hover.missing(traits::rank) != null) {
            for (String pid : new String[] {hover.parent(), hover.parent2()}) {
                if (pid == null || traits.rank(pid) > 0) continue;
                TraitDef p = tree.get(pid);
                tip.add(Component.translatable("advbreeding.gui.trait_need_parent",
                        (p != null ? p.displayName() : Component.translatable(pid))).withStyle(ChatFormatting.RED));
            }
        } else if (points() < hover.cost()) {
            tip.add(Component.translatable("advbreeding.gui.trait_need_points").withStyle(ChatFormatting.RED));
        } else {
            tip.add(Component.translatable("advbreeding.gui.trait_click").withStyle(ChatFormatting.YELLOW));
        }
        return tip;
    }

    private static net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effectIcon(String id) {
        return switch (id) {
            case "regen_aura" -> net.minecraft.world.effect.MobEffects.REGENERATION;
            case "fire_resistance_aura" -> net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE;
            case "saturation_aura" -> net.minecraft.world.effect.MobEffects.SATURATION;
            case "hoppy_aura" -> net.minecraft.world.effect.MobEffects.JUMP;
            case "poison_touch" -> net.minecraft.world.effect.MobEffects.POISON;
            default -> null;
        };
    }

    private static boolean lowerIsBetter(Stat s) {
        return s == Stat.DAMAGE_TAKEN || s == Stat.ELEMENT_TAKEN || s == Stat.FALL_TAKEN || s == Stat.THREAT || s == Stat.CALMING || s == Stat.FOOD_USE || s == Stat.SAT_RATE;
    }

    private static boolean isGood(Stat s, double v) {
        return lowerIsBetter(s) ? v <= 0 : v >= 0;
    }

    private static final Stat[] CORE = {Stat.HEALTH_PCT, Stat.DAMAGE_PCT, Stat.SPEED_PCT, Stat.JUMP_PCT,
            Stat.DAMAGE_TAKEN, Stat.ELEMENT_TAKEN, Stat.GROWTH};

    private List<Component> renderStats(GuiGraphics ctx, int mouseX, int mouseY, Genome genome) {
        ctx.drawString(this.font, Component.translatable("advbreeding.gui.stats_title").withStyle(ChatFormatting.BOLD), left + 8, top + 31, TEXT, false);

        Effects genes = genome.genotypeEffects();
        Effects blood = genome.bloodlineEffects();
        Effects trait = traits().effects(TraitTrees.forEntity(entity));

        int nameX = left + 12;
        int[] colRight = {left + W - 12 - 3 * 34, left + W - 12 - 2 * 34, left + W - 12 - 34, left + W - 12};
        String[] heads = {"advbreeding.gui.col.genotype", "advbreeding.gui.col.bloodline", "advbreeding.gui.col.traits", "advbreeding.gui.col.total"};
        int hy = top + 44;
        inset(ctx, left + 8, hy - 3, W - 16, H - 52);
        for (int i = 0; i < 4; i++) {
            Component h = Component.translatable(heads[i]);
            ctx.drawString(this.font, h, colRight[i] - this.font.width(h), hy, 0xFF000000 | darken(0xFFFFFF), false);
        }
        ctx.fill(left + 10, hy + 10, left + W - 10, hy + 11, 0xFF555555);

        List<Component> tip = null;
        int y = hy + 15;
        for (Stat st : Stat.values()) {
            double g = st.get(genes), b = st.get(blood), t = st.get(trait);
            double total = g + b + t;
            boolean core = false;
            for (Stat c : CORE) if (c == st) core = true;
            if (!core && Math.abs(total) < 1e-9 && Math.abs(g) < 1e-9 && Math.abs(b) < 1e-9 && Math.abs(t) < 1e-9) continue;
            if (y > top + H - 14) break;

            Component label = Component.translatable(st.nameKey());
            String name = label.getString();
            String shown = name;
            while (shown.length() > 3 && this.font.width(shown) > colRight[0] - 34 - nameX) {
                shown = shown.substring(0, shown.length() - 1);
            }
            if (!shown.equals(name)) shown = shown.stripTrailing() + "\u2026";
            ctx.drawString(this.font, shown, nameX, y, TEXT, false);
            double[] vals = {g, b, t, total};
            for (int i = 0; i < 4; i++) {
                boolean zero = Math.abs(vals[i]) < 1e-9;
                String txt = zero ? "-" : st.format(vals[i]);
                int color = zero ? 0xFF8A8A8A : isGood(st, vals[i]) ? 0xFF2E7D32 : 0xFFB23A3A;
                ctx.drawString(this.font, txt, colRight[i] - this.font.width(txt), y, color, false);
            }
            if (mouseX >= left + 10 && mouseX < left + W - 10 && mouseY >= y - 1 && mouseY < y + 10) {
                tip = new ArrayList<>();
                tip.add(label.copy().withStyle(ChatFormatting.BOLD));
            }
            y += 11;
        }
        return tip;
    }

    private static String pct(double v) {
        double p = Math.abs(v) * 100.0;
        String num = Math.abs(p - Math.rint(p)) < 0.05 ? String.valueOf((long) Math.rint(p)) : String.format("%.1f", p);
        return (v < 0 ? "-" : "+") + num + "%";
    }

    private static Component req(boolean ok, Component text) {
        return Component.literal(ok ? "\u2714 " : "\u2718 ").append(text)
                .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private static int valueColor(int v) {
        if (v < Genotype.BASELINE) return 0xD04848;
        if (v == Genotype.BASELINE) return 0x7F9A7F;
        float t = (v - Genotype.BASELINE) / (float) (Genotype.MAX - Genotype.BASELINE);
        return lerp(0x4CC24C, 0xF2B91E, t);
    }

    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static int darken(int rgb) {
        int r = (int) (((rgb >> 16) & 255) * 0.62);
        int g = (int) (((rgb >> 8) & 255) * 0.62);
        int b = (int) ((rgb & 255) * 0.62);
        return (r << 16) | (g << 8) | b;
    }

    private static void panel(GuiGraphics ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, 0xFF000000);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC6C6C6);
        ctx.fill(x + 1, y + 1, x + w - 2, y + 3, 0xFFFFFFFF);
        ctx.fill(x + 1, y + 1, x + 3, y + h - 2, 0xFFFFFFFF);
        ctx.fill(x + 3, y + h - 3, x + w - 1, y + h - 1, 0xFF555555);
        ctx.fill(x + w - 3, y + 3, x + w - 1, y + h - 1, 0xFF555555);
    }

    private static void inset(GuiGraphics ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, 0xFF373737);
        ctx.fill(x + 1, y + 1, x + w, y + h, 0xFFFFFFFF);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF8B8B8B);
    }
}
