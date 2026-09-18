package net.per.primogemcraft.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.client.ChoiceParticles;
import net.per.primogemcraft.client.ChoicePreviewRenderer;
import net.per.primogemcraft.network.ChoiceSelectPayload;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.choice.*;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;

public class ChoiceScreen extends Screen {
    private static final int MIN_MOD_NAME_LENGTH = 3;
    private static final int CARD_UNITS_W = 280;
    private static final int CARD_UNITS_H = 560;
    private static final int CARD_UNITS_HALF = CARD_UNITS_H / 2;
    private static final int NAME_PLATE_UNITS_W = CARD_UNITS_W * 3 / 5;
    private static final int NAME_PLATE_UNITS_X = (CARD_UNITS_W - NAME_PLATE_UNITS_W) / 2;
    private static final int NAME_PLATE_UNITS_Y = 10;
    private static final int NAME_PLATE_UNITS_PAD = 3;
    private static final int NAME_PLATE_SHIFT_X = 1;
    private static final int NAME_PLATE_SHIFT_Y = -1;
    private static final int VISUAL_UNITS_SIZE = 120;
    private static final int VISUAL_UNITS_CENTER_Y = CARD_UNITS_HALF / 2;
    private static final int ICON_TILE = 16;
    private static final int ICON_UNITS_SIZE = 32;
    private static final int ICON_UNITS_GAP = 6;
    private static final int ICON_UNITS_TOP = VISUAL_UNITS_CENTER_Y + VISUAL_UNITS_SIZE / 2;
    private static final int TEXT_UNITS_INSET = 30;
    private static final int TEXT_UNITS_TOP = CARD_UNITS_HALF + 16;
    private static final int ICON_UNITS_Y = (ICON_UNITS_TOP + TEXT_UNITS_TOP - ICON_UNITS_SIZE) / 2;
    private static final int TEXT_UNITS_BOTTOM_MARGIN = 40;
    private static final int TEXT_UNITS_W = CARD_UNITS_W - TEXT_UNITS_INSET * 2;
    private static final int TEXT_UNITS_H = CARD_UNITS_H - TEXT_UNITS_BOTTOM_MARGIN - TEXT_UNITS_TOP;
    private static final int TEXT_UNITS_BORDER = 2;
    private static final int TEXT_UNITS_PAD = 8;
    private static final int BADGE_UNITS_Y = TEXT_UNITS_TOP - 30;
    private static final float BADGE_TEXT_SCALE = 2.4F;
    private static final int BADGE_COLOR = 0xFF9AA8BC;
    private static final int TEXT_UNITS_INNER_X = TEXT_UNITS_INSET + TEXT_UNITS_PAD + TEXT_UNITS_BORDER;
    private static final int TEXT_UNITS_INNER_Y = TEXT_UNITS_TOP + TEXT_UNITS_PAD + TEXT_UNITS_BORDER;
    private static final int TEXT_UNITS_INNER_W = TEXT_UNITS_W - (TEXT_UNITS_PAD + TEXT_UNITS_BORDER) * 2;
    private static final int TEXT_UNITS_INNER_H = TEXT_UNITS_H - (TEXT_UNITS_PAD + TEXT_UNITS_BORDER) * 2;
    private static final int SCROLL_TRACK_UNITS_W = 4;
    private static final int SCROLL_TRACK_UNITS_GAP = 4;
    private static final int SCROLL_TRACK_UNITS_X = TEXT_UNITS_INNER_X + TEXT_UNITS_INNER_W - SCROLL_TRACK_UNITS_W;
    private static final int SCROLL_TRACK_UNITS_Y = TEXT_UNITS_INNER_Y;
    private static final int SCROLL_TRACK_UNITS_H = TEXT_UNITS_INNER_H;
    private static final int SCROLL_THUMB_UNITS_MIN = 12;
    private static final int SCROLL_TEXT_UNITS_W = TEXT_UNITS_INNER_W - SCROLL_TRACK_UNITS_GAP - SCROLL_TRACK_UNITS_W;

    private static final float TITLE_TEXT_SCALE = 1.92F;
    private static final float BODY_TEXT_SCALE = 2.8125F;
    private static final float COUNT_TEXT_SCALE = 3.0F;
    private static final int COUNT_UNITS_INSET = 3;
    private static final float COUNT_DEPTH = 200.0F;

    private static final float EDGE_UNITS = 36.0F;
    private static final float EDGE_SHADE = 0.85F;
    private static final float ITEM_BRIGHTNESS = 1.3F;
    private static final float EDGE_FADE_UNITS = 8.0F;
    private static final float REVEAL_SECONDS = 0.35F;
    private static final float REVEAL_SOUND_VOLUME = 0.7F;
    private static final int CHIME_MIN_QUALITY = 2;
    private static final float GRANT_SOUND_PITCH = 1.0F;
    private static final float GRANT_SOUND_VOLUME = 2.0F;
    private static final int RESULT_TIMEOUT_TICKS = 100;
    private static final int FIRST_CARD = 0;
    private static final int EVERY_CARD = -1;
    private static final int REVEAL_INDEX = 0;
    private static final float FRAME_SECONDS_CAP = 0.2F;
    private static final float SECONDS_PER_TICK = 0.05F;

    private static final float ROW_FILL = 0.80F;
    private static final float CARD_GAP_FILL = 0.06F;
    private static final float CARD_HEIGHT_FILL = 0.98F;
    private static final int MAX_VISIBLE_CARDS = 4;
    private static final int ROW_SCROLL_BAR_HEIGHT = 4;
    private static final int ROW_SCROLL_BAR_GAP = 4;
    private static final int ROW_SCROLL_THUMB_MIN_WIDTH = 12;
    private static final float ROW_SCROLL_RESPONSE = 22.0F;
    private static final float ROW_SCROLL_SETTLE = 0.5F;
    private static final float TOP_BAND_FILL = 0.17F;
    private static final float BOTTOM_BAND_FILL = 0.09F;
    private static final float CARD_RISE = 0.04F;
    private static final float MIN_CARD_WIDTH = 24.0F;

    private static final float VISUAL_DEPTH = -130.0F;
    private static final float CONTENT_DEPTH = 2.0F;
    private static final float LABEL_DEPTH = 2.0F;
    private static final float HOVER_LIFT = 6.0F;
    private static final float REVEAL_GROW = 0.72F;
    private static final float HOVER_EASE = 0.2F;

    private static final String ELLIPSIS = "...";
    private static final int TOOLTIP_WRAP_MIN = 200;
    private static final int TOOLTIP_LINE_HEIGHT = 10;
    private static final int TOOLTIP_SINGLE_LINE_TRIM = 2;
    private static final int TOOLTIP_OFFSET_X = 12;
    private static final int TOOLTIP_OFFSET_Y = -12;
    private static final int TOOLTIP_FLIP_SHIFT = 24;
    private static final int TOOLTIP_SCREEN_MARGIN = 4;
    private static final int TOOLTIP_BOTTOM_MARGIN = 3;
    private static final int TOOLTIP_FRAME_PAD = TooltipRenderUtil.PADDING_TOP + 1;
    private static final int TOOLTIP_BOX_SPACING = 2;
    private static final int TOOLTIP_GAP = TOOLTIP_FRAME_PAD * 2 + TOOLTIP_BOX_SPACING;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int COUNT_COLOR = 0xFFFFFFFF;
    private static final int TITLE_COLOR = 0xFFFFE9B0;
    private static final int SUBTITLE_COLOR = 0xFFFFC0FF;
    private static final int HINT_COLOR = 0xFFBFD8FF;
    private static final int TEXT_COLOR = 0xFFE6ECF5;
    private static final String UNAVAILABLE_KEY = "gui.primogemcraft.choice.unavailable";
    private static final int FRAME_COLOR = 0x55B0B0B0;
    private static final int BAR_COLOR = 0x33FFFFFF;
    private static final int BAR_FILL_COLOR = 0x66FF4444;
    private static final int SCROLL_TRACK_COLOR = 0x33FFFFFF;
    private static final int SCROLL_THUMB_COLOR = 0x99DCE6F2;

    private static final float TEXT_DEPTH = 200.0F;
    private static final float TITLE_SCALE = 1.3F;
    private static final float SUBTITLE_SCALE = 0.8F;
    private static final float HINT_SCALE = 0.7F;
    private static final float TITLE_TOP = 0.02F;
    private static final float HEADING_PLATE_MAX_FILL = 0.85F;
    private static final int HEADING_PLATE_PAD_X = 6;
    private static final int HEADING_PLATE_PAD_Y = 1;
    private static final int HEADING_PLATE_SHIFT_X = 2;
    private static final int HEADING_PLATE_SHIFT_Y = 1;
    private static final int HEADING_LABEL_SHIFT_X = 1;
    private static final int HEADING_LABEL_SHIFT_Y = 2;
    private static final int SUBTITLE_GAP = 3;
    private static final float HINT_TOP = 0.955F;
    private static final int BAR_HEIGHT = 4;

    private static List<String> MOD_NAMES;

    private final ChoiceRequest request;
    private final List<ChoiceTextScroller> scrollers = new ArrayList<>();
    private final List<FormattedCharSequence> nameLabels = new ArrayList<>();
    private final List<float[]> cardPositions = new ArrayList<>();
    private final Map<KeyMapping, IKeyConflictContext> originalMovementContexts = new HashMap<>();
    private final Clock clock = new Clock();
    private FormattedCharSequence headingLabel = FormattedCharSequence.EMPTY;
    private float[] cardLifts = new float[0];
    private ChoiceCardSpin[] cardSpins = new ChoiceCardSpin[0];
    private boolean[] burstCards = new boolean[0];
    private boolean[] unavailable = new boolean[0];
    private int chimeIndex = -1;
    private boolean grantPlayed;
    private int spinDuration;
    private int spinTicks;
    private boolean spinning = true;
    private int chosen = -1;
    private int settle;
    private int awaitingResult;
    private long remaining;
    private boolean answered;
    private float unitScale;
    private float cardWidth;
    private float cardHeight;
    private float cardCenterY;
    private int wrapWidth = 1;
    private int nameWrapWidth = 1;
    private float namePlateH;
    private float nameBoxX;
    private float nameTextY;
    private int headingPlateLeft;
    private int headingPlateWidth;
    private int headingPlateH;
    private int headingBoxX;
    private int headingWrapWidth = 1;
    private float rowWidth;
    private float rowScroll;
    private float rowScrollTarget;
    private float rowScrollMax;
    private int rowBarY;
    private int timeBarY;

    public ChoiceScreen(ChoiceRequest request) {
        super(request.title());
        this.request = request;
    }

    @Override
    protected void init() {
        cardSpins = buildSpins();
        cardLifts = new float[request.size()];
        burstCards = new boolean[request.size()];
        unavailable = new boolean[request.size()];
        for (var index = 0; index < request.size(); index++) unavailable[index] = !request.option(index).enabled();
        chimeIndex = uniformChime() ? FIRST_CARD : EVERY_CARD;
        grantPlayed = false;
        spinDuration = Math.max(1, maxTicks());
        spinTicks = 0;
        spinning = true;
        chosen = -1;
        settle = 0;
        awaitingResult = 0;
        answered = false;
        remaining = Math.max(0L, request.expireTime() - gameTime());
        updateLayout();
        wrapHeading();
        claimMovementKeys();
    }

    @Override
    public void removed() {
        releaseMovementKeys();
        super.removed();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        var frame = frameSeconds();
        clock.advance(frame, chosen < 0 && clock.animationSeconds() < revealSeconds(), revealSeconds());
        for (var scroller : scrollers) scroller.advance(frame);
        advanceRowScroll(frame);
        updateCardPositions();
        updateHoverLifts(mouseX, mouseY);
        renderCards(graphics, mouseX, mouseY);
        renderRowScrollBar(graphics);
        renderTexts(graphics);
        renderHoveredTooltip(graphics, mouseX, mouseY);
    }

    private float frameSeconds() {
        return Mth.clamp(Minecraft.getInstance().getTimer().getGameTimeDeltaTicks(), 0.0F, FRAME_SECONDS_CAP / SECONDS_PER_TICK) * SECONDS_PER_TICK;
    }

    private float spinSeconds() {
        return spinDuration * SECONDS_PER_TICK;
    }

    private float revealSeconds() {
        return spinSeconds() + REVEAL_SECONDS;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void tick() {
        holdMovementKeys();

        if (awaitingResult > 0) {
            if (--awaitingResult == 0) dismiss();
            return;
        }

        if (chosen >= 0) {
            if (settle > 0) settle--;
            else answer(chosen);
            return;
        }

        if (spinning && ++spinTicks >= spinDuration) {
            spinTicks = spinDuration;
            spinning = false;
        }

        tickRevealBursts();

        if (spinning || isRevealing()) return;

        if (remaining <= 0L) {
            var index = timeoutIndex();
            if (index < 0) forceAnswer(index);
            else choose(index);
            return;
        }
        remaining--;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isRevealing()) {
            if (hoveredCard(mouseX, mouseY) >= 0) return true;
            playClickSound();
            onClose();
            return true;
        }
        if (spinning || chosen >= 0 || awaitingResult > 0) return true;
        for (var index = request.size() - 1; index >= 0; index--) {
            if (!inside(index, mouseX, mouseY)) continue;
            if (!isAvailable(index)) continue;
            playClickSound();
            choose(index);
            break;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!canHover() || scrollY == 0.0D) return true;
        if (!scrollCardText(scrollY, mouseX, mouseY)) scrollRow(scrollY);
        return true;
    }

    private boolean scrollCardText(double notches, double mouseX, double mouseY) {
        for (var index = 0; index < request.size(); index++) {
            var scroller = scrollers.get(index);
            if (scroller.isNotScrollable()) continue;
            if (!descriptionFrame(index).contains(mouseX, mouseY)) continue;
            scroller.scroll(notches);
            return true;
        }
        return false;
    }

    private void scrollRow(double notches) {
        if (rowScrollMax <= 0.0F) return;
        rowScrollTarget = Mth.clamp((float) (rowScrollTarget - notches * cardPitch()), 0.0F, rowScrollMax);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE || isRevealing()) return super.keyPressed(keyCode, scanCode, modifiers);
        if (spinning || chosen >= 0 || awaitingResult > 0) return true;
        if (keyCode >= 49 && keyCode < 49 + request.size()) {
            var index = keyCode - 49;
            if (isAvailable(index)) {
                playClickSound();
                choose(index);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void playRevealSound(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, pitch, REVEAL_SOUND_VOLUME));
    }

    private void playGrantSound() {
        if (grantPlayed) return;
        grantPlayed = true;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(PGCSounds.CHOICE_GRANT.get(), GRANT_SOUND_PITCH, GRANT_SOUND_VOLUME));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (awaitingResult > 0) return;
        if (answered) {
            super.onClose();
            return;
        }
        forceAnswer(dismissalIndex());
    }

    private int dismissalIndex() {
        if (isRevealing()) return REVEAL_INDEX;
        if (chosen >= 0) return chosen;
        return randomAvailable();
    }

    private int randomAvailable() {
        var pool = new ArrayList<Integer>();
        for (var index = 0; index < request.size(); index++) if (isAvailable(index)) pool.add(index);
        if (pool.isEmpty()) return fallbackIndex();
        var player = minecraft.player;
        return player == null ? pool.getFirst() : pool.get(player.getRandom().nextInt(pool.size()));
    }

    private int timeoutIndex() {
        var last = request.size() - 1;
        return isAvailable(last) ? last : fallbackIndex();
    }

    private int fallbackIndex() {
        var leave = request.leaveIndex();
        if (leave >= 0 && leave < request.size() && isAvailable(leave)) return leave;
        return ChoiceRegistry.NO_ANSWER;
    }

    private void holdMovementKeys() {
        var window = minecraft.getWindow().getWindow();
        for (var mapping : movementKeys()) {
            var key = mapping.getKey();
            if (key.getType() != InputConstants.Type.KEYSYM || key.equals(InputConstants.UNKNOWN)) continue;
            if (mapping.isDown()) continue;
            if (InputConstants.isKeyDown(window, key.getValue())) mapping.setDown(true);
        }
    }

    private void claimMovementKeys() {
        for (var mapping : movementKeys()) {
            originalMovementContexts.putIfAbsent(mapping, mapping.getKeyConflictContext());
            mapping.setKeyConflictContext(KeyConflictContext.UNIVERSAL);
        }
        holdMovementKeys();
    }

    private void releaseMovementKeys() {
        for (var entry : originalMovementContexts.entrySet()) entry.getKey().setKeyConflictContext(entry.getValue());
        originalMovementContexts.clear();
    }

    private List<KeyMapping> movementKeys() {
        var options = minecraft.options;
        return List.of(options.keyUp, options.keyDown, options.keyLeft, options.keyRight, options.keyJump, options.keyShift, options.keySprint);
    }

    public void update(ChoiceRequest replacement) {
        Minecraft.getInstance().setScreen(new ChoiceScreen(replacement));
    }

    public void choose(int index) {
        if (chosen >= 0 || awaitingResult > 0 || index < 0 || index >= request.size()) return;
        chosen = index;
        settle = request.settleTicks();
        scrollCardIntoView(index);
        wrapHeading();
    }

    public void answer(int index) {
        if (index < 0 || index >= request.size()) return;
        forceAnswer(index);
    }

    private void forceAnswer(int index) {
        if (answered) return;
        answered = true;
        PacketDistributor.sendToServer(new ChoiceSelectPayload(request.id(), index));
        if (isConfirming()) awaitingResult = RESULT_TIMEOUT_TICKS;
        else super.onClose();
    }

    public void result(int requestId, int index, boolean accepted) {
        if (requestId != request.id()) return;
        if (accepted) {
            dismiss();
            return;
        }
        awaitingResult = 0;
        answered = false;
        chosen = -1;
        settle = 0;
        if (index >= 0 && index < unavailable.length) unavailable[index] = true;
        wrapHeading();
    }

    private void dismiss() {
        awaitingResult = 0;
        super.onClose();
    }

    private boolean isAvailable(int index) {
        return index >= 0 && index < unavailable.length && !unavailable[index];
    }

    private boolean isConfirming() {
        return request.mode() == ChoiceMode.CONFIRM;
    }

    private ChoiceCardSpin[] buildSpins() {
        var spins = new ChoiceCardSpin[request.size()];
        for (var index = 0; index < spins.length; index++) {
            var card = request.option(index);
            var spin = card.spin();
            spins[index] = spin == null ? request.spin().cardSpin(card.item()) : spin;
        }
        return spins;
    }

    private int maxTicks() {
        var max = 1;
        for (var spin : cardSpins) max = Math.max(max, spin.ticks());
        return max;
    }

    private void updateLayout() {
        var size = request.size();
        var areaTop = height * TOP_BAND_FILL;
        var areaBottom = height * (1.0F - BOTTOM_BAND_FILL);

        var visible = Math.min(size, MAX_VISIBLE_CARDS);
        var widthLimit = width * ROW_FILL / (visible + (visible - 1) * CARD_GAP_FILL);
        var heightLimit = (areaBottom - areaTop) * CARD_HEIGHT_FILL * CARD_UNITS_W / CARD_UNITS_H;
        cardWidth = Math.max(MIN_CARD_WIDTH, Math.min(widthLimit, heightLimit));
        cardHeight = cardWidth * CARD_UNITS_H / CARD_UNITS_W;
        unitScale = cardWidth / CARD_UNITS_W;
        cardCenterY = (areaTop + areaBottom) * 0.5F - height * CARD_RISE;

        rowWidth = cardWidth * (size + (size - 1) * CARD_GAP_FILL);
        rowScrollMax = Math.max(0.0F, rowWidth - width * ROW_FILL);
        rowScrollTarget = Mth.clamp(rowScrollTarget, 0.0F, rowScrollMax);
        rowScroll = Mth.clamp(rowScroll, 0.0F, rowScrollMax);
        rowBarY = Math.round(cardCenterY + cardHeight * 0.5F + ROW_SCROLL_BAR_GAP);
        timeBarY = rowBarY + ROW_SCROLL_BAR_HEIGHT * 3;
        updateCardPositions();

        var pixelUnit = 1.0F / unitScale;
        var plateInsetSide = Math.round(NineSlicePlate.INSET_LEFT * pixelUnit);
        var plateInsetTop = Math.round(NineSlicePlate.INSET_TOP * pixelUnit);
        var plateInsetBottom = Math.round(NineSlicePlate.INSET_BOTTOM * pixelUnit);
        var nameGlyphUnits = font.lineHeight * TITLE_TEXT_SCALE;
        namePlateH = plateInsetTop + plateInsetBottom + NAME_PLATE_UNITS_PAD * 2 + nameGlyphUnits;
        nameBoxX = NAME_PLATE_UNITS_X + plateInsetSide + NAME_PLATE_UNITS_PAD + NAME_PLATE_SHIFT_X * pixelUnit;
        nameTextY = NAME_PLATE_UNITS_Y + plateInsetTop + NAME_PLATE_UNITS_PAD + NAME_PLATE_SHIFT_Y * pixelUnit;
        nameWrapWidth = Math.max(1, Mth.floor((NAME_PLATE_UNITS_W - (plateInsetSide + NAME_PLATE_UNITS_PAD) * 2) / TITLE_TEXT_SCALE));

        scrollers.clear();
        nameLabels.clear();
        wrapWidth = Math.max(1, Mth.floor(SCROLL_TEXT_UNITS_W / BODY_TEXT_SCALE));
        var wrapHeight = Math.max(1, Mth.floor(TEXT_UNITS_INNER_H / BODY_TEXT_SCALE));
        for (var card : request.options()) {
            var scroller = new ChoiceTextScroller();
            scroller.wrap(wrapWidth, wrapHeight, textOf(card));
            scrollers.add(scroller);
            nameLabels.add(Language.getInstance().getVisualOrder(ellipsized(card.displayTitle(), nameWrapWidth)));
        }
    }

    private void updateCardPositions() {
        var pitch = cardPitch();
        var left = rowRestLeft() - rowScroll;
        cardPositions.clear();
        for (var index = 0; index < request.size(); index++) {
            cardPositions.add(new float[]{left + cardWidth * 0.5F + index * pitch, cardCenterY});
        }
    }

    private float rowRestLeft() {
        return Math.max((width - width * ROW_FILL) * 0.5F, (width - rowWidth) * 0.5F);
    }

    private float cardPitch() {
        return cardWidth * (1.0F + CARD_GAP_FILL);
    }

    private void advanceRowScroll(float frameSeconds) {
        rowScroll = Mth.lerp(1.0F - (float) Math.exp(-ROW_SCROLL_RESPONSE * frameSeconds), rowScroll, rowScrollTarget);
        if (Math.abs(rowScrollTarget - rowScroll) < ROW_SCROLL_SETTLE) rowScroll = rowScrollTarget;
    }

    private void scrollCardIntoView(int index) {
        if (rowScrollMax <= 0.0F) return;

        var viewWidth = width * ROW_FILL;
        var viewLeft = (width - viewWidth) * 0.5F;
        var left = rowRestLeft() + index * cardPitch();
        rowScrollTarget = Mth.clamp(rowScrollTarget, left + cardWidth - viewLeft - viewWidth, left - viewLeft);
        rowScrollTarget = Mth.clamp(rowScrollTarget, 0.0F, rowScrollMax);
    }

    private void renderCards(GuiGraphics graphics, int mouseX, int mouseY) {
        RenderSystem.disableCull();
        for (var index = 0; index < request.size(); index++) renderCard(graphics, index, mouseX, mouseY);
        RenderSystem.enableCull();
    }

    private void renderRowScrollBar(GuiGraphics graphics) {
        if (rowScrollMax <= 0.0F) return;

        var track = Math.round(width * ROW_FILL);
        var left = (width - track) / 2;
        graphics.fill(left, rowBarY, left + track, rowBarY + ROW_SCROLL_BAR_HEIGHT, SCROLL_TRACK_COLOR);
        var thumbWidth = Math.clamp(Math.round(track * (track / rowWidth)), ROW_SCROLL_THUMB_MIN_WIDTH, track);
        var travel = track - thumbWidth;
        var thumbLeft = left + Math.round(travel * Mth.clamp(rowScroll / rowScrollMax, 0.0F, 1.0F));
        graphics.fill(thumbLeft, rowBarY, thumbLeft + thumbWidth, rowBarY + ROW_SCROLL_BAR_HEIGHT, SCROLL_THUMB_COLOR);
    }

    private void renderCard(GuiGraphics graphics, int index, int mouseX, int mouseY) {
        var reveal = chosen >= 0 && index == chosen ? 1.0F : Mth.clamp((clock.animationSeconds() - cardSeconds(index)) / REVEAL_SECONDS, 0.0F, 1.0F);
        var dimmed = chosen >= 0 && index != chosen;
        var available = isAvailable(index);
        var hovered = canHover() && available && inside(index, mouseX, mouseY);
        var position = cardPositions.get(index);
        var turn = cardTurn(index);
        var faceSide = (float) Math.cos(turn);
        var column = chosen >= 0 ? (chosen == index ? request.textures().chosen() : request.textures().dimmed())
                : !available ? request.textures().dimmed() : hovered ? request.textures().hovered() : request.textures().idle();

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(position[0], position[1], 0.0F);
        pose.scale(unitScale, unitScale, 1.0F);
        if (hovered) pose.translate(0.0F, -HOVER_LIFT * cardLifts[index], 0.0F);

        pose.pushPose();
        pose.translate(-CARD_UNITS_W * 0.5F, -CARD_UNITS_H * 0.5F, 0.0F);
        renderCardEdge(graphics, turn);
        pose.popPose();

        pose.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(turn)));
        pose.translate(-CARD_UNITS_W * 0.5F, -CARD_UNITS_H * 0.5F, EDGE_UNITS * 0.5F);

        renderCardFace(graphics, column, faceSide < 0.0F);
        if (!dimmed && reveal > 0.0F) {
            pose.translate(0.0F, 0.0F, CONTENT_DEPTH);
            renderCardContent(graphics, index, reveal);
        }
        pose.popPose();
    }

    private float cardTurn(int index) {
        var progress = cardProgress(index);
        var remaining = 1.0F - progress;
        var lean = (index - (request.size() - 1) / 2.0D) * request.spin().tilt() * remaining;
        return (float) (Math.toRadians(lean) + 2.0D * Math.PI * cardSpins[index].turns() * smootherstep(progress));
    }

    private float cardProgress(int index) {
        return Mth.clamp(clock.animationSeconds() / cardSeconds(index), 0.0F, 1.0F);
    }

    private float cardSeconds(int index) {
        return cardSpins[index].ticks() * SECONDS_PER_TICK;
    }

    private void updateHoverLifts(double mouseX, double mouseY) {
        var hoverable = canHover();
        var anyHovered = false;
        for (var index = 0; index < cardLifts.length; index++) {
            if (hoverable && isAvailable(index) && inside(index, mouseX, mouseY)) anyHovered = true;
        }
        for (var index = 0; index < cardLifts.length; index++) {
            var target = anyHovered && isAvailable(index) && inside(index, mouseX, mouseY) ? 1.0F : 0.0F;
            cardLifts[index] = Mth.clamp(Mth.lerp(HOVER_EASE, cardLifts[index], target), 0.0F, 1.0F);
        }
    }

    private static float smootherstep(float value) {
        return value * value * value * (value * (value * 6.0F - 15.0F) + 10.0F);
    }

    private void renderCardFace(GuiGraphics graphics, int column, boolean reversed) {
        var textures = request.textures();
        if (reversed) {
            graphics.blit(textures.backSheet(), 0, 0, CARD_UNITS_W, CARD_UNITS_H, 0.0F, 0.0F, textures.tileWidth(), textures.tileHeight(), textures.tileWidth(), textures.tileHeight());
            return;
        }
        graphics.blit(textures.sheet(), 0, 0, CARD_UNITS_W, CARD_UNITS_H, (float) textures.u(column), (float) textures.v(0), textures.tileWidth(), textures.tileHeight(), textures.tileWidth() * textures.columns(), textures.tileHeight());
    }

    private void renderCardOverlay(GuiGraphics graphics, int index, float reveal) {
        var overlay = request.option(index).overlay();
        if (overlay == null) return;
        var textures = request.textures();
        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, reveal);
        graphics.blit(overlay.sheet(), 0, 0, CARD_UNITS_W, CARD_UNITS_H, (float) textures.u(overlay.column()), (float) textures.v(0), textures.tileWidth(), textures.tileHeight(), textures.tileWidth() * overlay.columns(), textures.tileHeight());
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void renderCardEdge(GuiGraphics graphics, double turn) {
        var offset = (float) (EDGE_UNITS * Math.sin(turn));
        if (Math.abs(offset) < 0.5F) return;

        var half = CARD_UNITS_W * 0.5F;
        var projected = (float) (half * Math.cos(turn));
        var anchor = offset > 0.0F ? half - projected : half + projected;
        var reach = Math.abs(offset) * 0.5F;
        var left = Mth.floor(anchor - reach);
        var right = Mth.ceil(anchor + reach);
        var textures = request.textures();
        var slice = Mth.clamp(Math.round((right - left) * (float) textures.tileWidth() / CARD_UNITS_W), 1, textures.tileWidth());
        var alpha = Mth.clamp(Math.abs(offset) / EDGE_FADE_UNITS, 0.0F, 1.0F);
        RenderSystem.enableBlend();
        graphics.setColor(EDGE_SHADE, EDGE_SHADE, EDGE_SHADE, alpha);
        graphics.blit(textures.backSheet(), left, 0, right - left, CARD_UNITS_H, 0.0F, 0.0F, slice, textures.tileHeight(), textures.tileWidth(), textures.tileHeight());
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void renderNamePlate(GuiGraphics graphics, int index) {
        var label = nameLabels.get(index);
        if (font.width(label) <= 0) return;

        var pose = graphics.pose();
        pose.pushPose();
        pose.scale(1.0F / unitScale, 1.0F / unitScale, 1.0F);
        NineSlicePlate.draw(graphics, Math.round(NAME_PLATE_UNITS_X * unitScale) + NAME_PLATE_SHIFT_X,
                Math.round(NAME_PLATE_UNITS_Y * unitScale) + NAME_PLATE_SHIFT_Y,
                Math.round(NAME_PLATE_UNITS_W * unitScale), Math.round(namePlateH * unitScale), 1.0F);
        pose.popPose();

        pose.pushPose();
        pose.translate(nameBoxX, nameTextY, LABEL_DEPTH);
        pose.scale(TITLE_TEXT_SCALE, TITLE_TEXT_SCALE, 1.0F);
        graphics.drawString(font, label, (nameWrapWidth - font.width(label)) / 2, 0, NAME_COLOR, false);
        pose.popPose();
    }

    private void renderCardContent(GuiGraphics graphics, int index, float reveal) {
        var card = request.option(index);
        var pose = graphics.pose();

        renderCardOverlay(graphics, index, reveal);
        renderNamePlate(graphics, index);

        pose.pushPose();
        pose.translate(CARD_UNITS_W * 0.5F, VISUAL_UNITS_CENTER_Y, 0.0F);
        var grow = REVEAL_GROW + (1.0F - REVEAL_GROW) * reveal;
        pose.scale(grow, grow, 1.0F);
        var brightness = request.visual() == ChoiceVisual.ITEM_MODEL ? ITEM_BRIGHTNESS : 1.0F;
        graphics.setColor(brightness, brightness, brightness, reveal);
        switch (request.visual()) {
            case ITEM_MODEL -> ChoicePreviewRenderer.renderItem(graphics, card.item(), VISUAL_UNITS_SIZE, VISUAL_DEPTH);
            case SCENARIO_TEXTURE ->
                    ChoicePreviewRenderer.renderTexture(graphics, request.texture(), VISUAL_UNITS_SIZE, VISUAL_UNITS_SIZE);
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        renderItemCount(graphics, card.item());
        pose.popPose();

        var icons = card.icons();
        if (icons != null) renderIcons(graphics, icons, reveal);

        renderCardBadge(graphics, card);

        var scroller = scrollers.get(index);
        if (scroller.isEmpty()) return;
        renderDescriptionFrame(graphics);
        renderScrollBar(graphics, scroller);
        renderDescriptionText(graphics, scroller, index);
    }

    private void renderCardBadge(GuiGraphics graphics, ChoiceCard card) {
        if (!card.hasBadge()) return;

        var badge = card.badge();
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(CARD_UNITS_W * 0.5F, BADGE_UNITS_Y, LABEL_DEPTH);
        pose.scale(BADGE_TEXT_SCALE, BADGE_TEXT_SCALE, 1.0F);
        graphics.drawString(font, badge, -font.width(badge) / 2, 0, BADGE_COLOR, false);
        pose.popPose();
    }

    private void renderIcons(GuiGraphics graphics, ChoiceCardIcons icons, float reveal) {
        var span = icons.count() * ICON_UNITS_SIZE + (icons.count() - 1) * ICON_UNITS_GAP;
        var left = (CARD_UNITS_W - span) / 2;
        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, reveal);
        for (var index = 0; index < icons.count(); index++) {
            graphics.blit(icons.texture(), left + index * (ICON_UNITS_SIZE + ICON_UNITS_GAP), ICON_UNITS_Y, ICON_UNITS_SIZE, ICON_UNITS_SIZE, 0.0F, 0.0F, ICON_TILE, ICON_TILE, ICON_TILE, ICON_TILE);
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void renderDescriptionFrame(GuiGraphics graphics) {
        for (var inset = 0; inset < TEXT_UNITS_BORDER; inset++) {
            graphics.renderOutline(TEXT_UNITS_INSET + inset, TEXT_UNITS_TOP + inset, TEXT_UNITS_W - inset * 2, TEXT_UNITS_H - inset * 2, FRAME_COLOR);
        }
    }

    private void renderDescriptionText(GuiGraphics graphics, ChoiceTextScroller scroller, int index) {
        var box = descriptionText(index);
        graphics.enableScissor(box.left(), box.top(), box.right(), box.bottom());
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(TEXT_UNITS_INNER_X, TEXT_UNITS_INNER_Y, LABEL_DEPTH);
        pose.scale(BODY_TEXT_SCALE, BODY_TEXT_SCALE, 1.0F);
        scroller.render(graphics, 0, 0, wrapWidth, TEXT_COLOR);
        pose.popPose();
        graphics.disableScissor();
    }

    private void renderScrollBar(GuiGraphics graphics, ChoiceTextScroller scroller) {
        if (scroller.isNotScrollable()) return;

        graphics.fill(SCROLL_TRACK_UNITS_X, SCROLL_TRACK_UNITS_Y, SCROLL_TRACK_UNITS_X + SCROLL_TRACK_UNITS_W, SCROLL_TRACK_UNITS_Y + SCROLL_TRACK_UNITS_H, SCROLL_TRACK_COLOR);
        var thumbHeight = Math.max(SCROLL_THUMB_UNITS_MIN, Math.round(SCROLL_TRACK_UNITS_H * scroller.thumbFraction()));
        var travel = SCROLL_TRACK_UNITS_H - thumbHeight;
        var thumbTop = SCROLL_TRACK_UNITS_Y + Math.round(travel * scroller.progress());
        graphics.fill(SCROLL_TRACK_UNITS_X, thumbTop, SCROLL_TRACK_UNITS_X + SCROLL_TRACK_UNITS_W, thumbTop + thumbHeight, SCROLL_THUMB_COLOR);
    }

    private void renderItemCount(GuiGraphics graphics, ItemStack stack) {
        var count = stack.getCount();
        if (count < 2) return;

        var text = Integer.toString(count);
        var corner = VISUAL_UNITS_SIZE / 2;
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(corner, corner, COUNT_DEPTH);
        pose.scale(COUNT_TEXT_SCALE, COUNT_TEXT_SCALE, 1.0F);
        graphics.drawString(font, text, -font.width(text) - COUNT_UNITS_INSET, -font.lineHeight, COUNT_COLOR, true);
        pose.popPose();
    }

    private void renderHoveredTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!canHover()) return;
        var itemVisual = request.visual() == ChoiceVisual.ITEM_MODEL;
        for (var index = request.size() - 1; index >= 0; index--) {
            var card = request.option(index);
            if (!inside(index, mouseX, mouseY)) continue;
            var available = isAvailable(index);
            if (card.textTooltip() || !available) {
                renderTextTooltip(graphics, mouseX, mouseY, card, available);
                return;
            }
            if (!itemVisual) continue;
            var stack = card.item();
            if (stack.isEmpty()) continue;
            graphics.renderTooltip(font, stack, mouseX, mouseY);
            return;
        }
    }

    private void renderTextTooltip(GuiGraphics graphics, int mouseX, int mouseY, ChoiceCard card, boolean available) {
        var lines = tooltipLines(hoverTextOf(card, available));
        if (lines.isEmpty()) return;

        var textWidth = tooltipWidth(lines);
        var textHeight = tooltipHeight(lines.size());
        var item = itemTooltipBox(card, mouseX);
        if (item == null) {
            var left = tooltipLeft(mouseX, textWidth);
            var top = restingTop(mouseY + TOOLTIP_OFFSET_Y, textHeight);
            graphics.renderTooltip(font, lines, TooltipAnchor.at(left, top), mouseX, mouseY);
            return;
        }

        var boxes = tooltipBoxes(mouseX, mouseY, textWidth, textHeight, item);
        graphics.renderTooltip(font, lines, TooltipAnchor.at(boxes.textLeft(), boxes.textTop()), mouseX, mouseY);

        var resting = DefaultTooltipPositioner.INSTANCE.positionTooltip(width, height, mouseX, mouseY, item.width(), item.height());
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(boxes.itemLeft() - resting.x(), boxes.itemTop() - resting.y(), 0.0F);
        graphics.renderTooltip(font, card.item(), mouseX, mouseY);
        pose.popPose();
    }

    private TooltipBoxes tooltipBoxes(int mouseX, int mouseY, int textWidth, int textHeight, TooltipBox item) {
        var restTop = mouseY + TOOLTIP_OFFSET_Y;
        if (textWidth + TOOLTIP_GAP + item.width() > width - TOOLTIP_SCREEN_MARGIN * 2)
            return stackedTooltipBoxes(mouseX, restTop, textWidth, textHeight, item.height());

        var pairWidth = textWidth + TOOLTIP_GAP + item.width();
        var left = Math.max(Math.min(mouseX + TOOLTIP_OFFSET_X, width - TOOLTIP_SCREEN_MARGIN - pairWidth), TOOLTIP_SCREEN_MARGIN);
        var top = Math.max(restingTop(restTop, Math.max(textHeight, item.height())), TOOLTIP_SCREEN_MARGIN);
        return new TooltipBoxes(left, top, left + textWidth + TOOLTIP_GAP, top);
    }

    private TooltipBoxes stackedTooltipBoxes(int mouseX, int restTop, int textWidth, int textHeight, int itemHeight) {
        var left = tooltipLeft(mouseX, textWidth);
        var top = Math.max(restingTop(restTop, textHeight), TOOLTIP_SCREEN_MARGIN);
        var below = top + textHeight + TOOLTIP_GAP;
        var itemTop = below;
        if (below + itemHeight > height - TOOLTIP_BOTTOM_MARGIN) {
            var above = top - TOOLTIP_GAP - itemHeight;
            if (above >= TOOLTIP_SCREEN_MARGIN) itemTop = above;
        }
        return new TooltipBoxes(left, top, left, itemTop);
    }

    private TooltipBox itemTooltipBox(ChoiceCard card, int mouseX) {
        if (!card.itemTooltip() || card.item().isEmpty()) return null;
        var stack = card.item();
        var elements = ClientHooks.gatherTooltipComponents(stack, getTooltipFromItem(minecraft, stack), stack.getTooltipImage(), mouseX, width, height, font);
        if (elements.isEmpty()) return null;

        var boxWidth = 0;
        var boxHeight = 0;
        for (var element : elements) {
            boxWidth = Math.max(boxWidth, element.getWidth(font));
            boxHeight += element.getHeight();
        }
        return new TooltipBox(boxWidth, boxHeight - (elements.size() == 1 ? TOOLTIP_SINGLE_LINE_TRIM : 0));
    }

    private int tooltipWidth(List<FormattedCharSequence> lines) {
        var width = 0;
        for (var line : lines) width = Math.max(width, font.width(line));
        return width;
    }

    private static int tooltipHeight(int lines) {
        return lines * TOOLTIP_LINE_HEIGHT - (lines == 1 ? TOOLTIP_SINGLE_LINE_TRIM : 0);
    }

    private int tooltipLeft(int mouseX, int boxWidth) {
        var left = mouseX + TOOLTIP_OFFSET_X;
        if (left + boxWidth <= width) return left;
        return Math.max(left - TOOLTIP_FLIP_SHIFT - boxWidth, TOOLTIP_SCREEN_MARGIN);
    }

    private int restingTop(int top, int boxHeight) {
        return Math.min(top, height - boxHeight - TOOLTIP_BOTTOM_MARGIN);
    }

    private List<Component> hoverTextOf(ChoiceCard card, boolean available) {
        var content = new ArrayList<Component>();
        if (!available) {
            content.add(Component.translatable(UNAVAILABLE_KEY));
            content.addAll(card.unmet());
        }
        content.add(card.displayTitle());
        if (card.hasDescription()) {
            content.add(card.description());
        } else {
            content.addAll(tooltipOf(card.item()));
        }
        if (card.hasFootnote()) content.add(card.footnote());
        return content;
    }

    private List<FormattedCharSequence> tooltipLines(List<Component> content) {
        var lines = new ArrayList<FormattedCharSequence>();
        var wrap = Math.max(width / 2, TOOLTIP_WRAP_MIN);
        for (var text : content) lines.addAll(font.split(text, wrap));
        return lines;
    }

    private void tickRevealBursts() {
        for (var index = 0; index < request.size(); index++) {
            if (burstCards[index] || cardProgress(index) < 1.0F) continue;
            burstCards[index] = true;
            if (isRevealing()) playGrantSound();
            else if (chimes(index)) playRevealSound(request.option(index).revealPitch());
            var position = cardPositions.get(index);
            ChoiceParticles.reveal(position[0], position[1], width, height);
        }
    }

    private boolean chimes(int index) {
        if (request.option(index).revealQuality() < CHIME_MIN_QUALITY) return false;
        return chimeIndex == EVERY_CARD || chimeIndex == index;
    }

    private boolean uniformChime() {
        if (request.size() < 2) return false;
        var quality = request.option(FIRST_CARD).revealQuality();
        for (var index = 1; index < request.size(); index++) {
            if (cardSpins[index].ticks() != cardSpins[FIRST_CARD].ticks()) return false;
            if (request.option(index).revealQuality() != quality) return false;
        }
        return true;
    }

    private boolean canHover() {
        return chosen < 0 && !spinning;
    }

    private boolean isRevealing() {
        return request.mode() == ChoiceMode.REVEAL;
    }

    private void renderTexts(GuiGraphics graphics) {
        var pose = graphics.pose();
        var headingTop = Mth.floor(height * TITLE_TOP);
        pose.pushPose();
        pose.translate(0.0F, 0.0F, TEXT_DEPTH);
        renderHeading(graphics, headingTop + HEADING_PLATE_SHIFT_Y);

        if (chosen < 0) {
            pose.pushPose();
            pose.scale(SUBTITLE_SCALE, SUBTITLE_SCALE, 1.0F);
            var subtitleTop = headingTop + headingPlateH + SUBTITLE_GAP;
            graphics.drawString(font, request.subtitle(), centered(request.subtitle(), SUBTITLE_SCALE), Mth.floor(subtitleTop / SUBTITLE_SCALE), SUBTITLE_COLOR, true);
            pose.popPose();
            if (!isRevealing()) renderProgressBar(graphics);
        }

        var hint = hintText();
        pose.pushPose();
        pose.scale(HINT_SCALE, HINT_SCALE, 1.0F);
        graphics.drawString(font, hint, centered(hint, HINT_SCALE), Mth.floor(height * HINT_TOP / HINT_SCALE), HINT_COLOR, true);
        pose.popPose();
        pose.popPose();
    }

    private void renderProgressBar(GuiGraphics graphics) {
        var barWidth = Mth.floor(width * 0.5F);
        var ratio = Mth.clamp((float) remaining / ChoiceRegistry.DEFAULT_DURATION, 0.0F, 1.0F);
        var filled = (int) (barWidth * ratio);
        graphics.fill((width - barWidth) / 2, timeBarY, (width + barWidth) / 2, timeBarY + BAR_HEIGHT, BAR_COLOR);
        graphics.fill((width - barWidth) / 2, timeBarY, (width - barWidth) / 2 + filled, timeBarY + BAR_HEIGHT, BAR_FILL_COLOR);
    }

    private Component hintText() {
        if (spinning) return rotationLabel();
        if (isRevealing()) return Component.translatable("gui.primogemcraft.choice.reveal");
        return Component.translatable("gui.primogemcraft.choice.hint", request.size());
    }

    private void renderHeading(GuiGraphics graphics, int top) {
        if (font.width(headingLabel) <= 0) return;

        var labelTop = top + NineSlicePlate.INSET_TOP + HEADING_PLATE_PAD_Y + HEADING_LABEL_SHIFT_Y;
        NineSlicePlate.draw(graphics, headingPlateLeft, top, headingPlateWidth, headingPlateH, 1.0F);
        var pose = graphics.pose();
        pose.pushPose();
        pose.scale(TITLE_SCALE, TITLE_SCALE, 1.0F);
        var labelX = Mth.floor((headingBoxX + HEADING_LABEL_SHIFT_X) / TITLE_SCALE) + (headingWrapWidth - font.width(headingLabel)) / 2;
        graphics.drawString(font, headingLabel, labelX, Mth.floor(labelTop / TITLE_SCALE), TITLE_COLOR, false);
        pose.popPose();
    }

    private void wrapHeading() {
        var heading = headingText();
        var labelWidth = Mth.ceil(font.width(heading) * TITLE_SCALE);
        var labelHeight = Mth.ceil(font.lineHeight * TITLE_SCALE);
        var padX = NineSlicePlate.INSET_LEFT + NineSlicePlate.INSET_RIGHT + HEADING_PLATE_PAD_X * 2;
        headingPlateH = NineSlicePlate.INSET_TOP + NineSlicePlate.INSET_BOTTOM + HEADING_PLATE_PAD_Y * 2 + labelHeight;
        headingPlateWidth = Math.min(Mth.floor(width * HEADING_PLATE_MAX_FILL), labelWidth + padX);
        headingPlateLeft = Mth.floor((width - headingPlateWidth) * 0.5F) + HEADING_PLATE_SHIFT_X;
        headingBoxX = headingPlateLeft + NineSlicePlate.INSET_LEFT + HEADING_PLATE_PAD_X;
        headingWrapWidth = Math.max(1, Mth.floor(Math.max(1, headingPlateWidth - padX) / TITLE_SCALE));
        headingLabel = Language.getInstance().getVisualOrder(ellipsized(heading, headingWrapWidth));
    }

    private Component headingText() {
        return chosen >= 0 ? request.option(chosen).displayTitle() : request.title();
    }

    private Component rotationLabel() {
        var spin = cardSpins[longestCard()];
        return Component.translatable("gui.primogemcraft.choice.rotation", spin.turns(), Component.translatable(spin.speed().translationKey()));
    }

    private int longestCard() {
        var longest = 0;
        for (var index = 1; index < cardSpins.length; index++) {
            if (cardSpins[index].ticks() > cardSpins[longest].ticks()) longest = index;
        }
        return longest;
    }

    private List<FormattedText> textOf(ChoiceCard card) {
        var content = new ArrayList<FormattedText>();
        if (card.hasDescription()) {
            content.add(card.description());
        } else {
            content.addAll(tooltipOf(card.item()));
        }
        if (card.hasFootnote()) content.add(card.footnote());
        return content;
    }

    private List<Component> tooltipOf(ItemStack stack) {
        var content = new ArrayList<Component>();
        var minecraft = Minecraft.getInstance();
        if (stack.isEmpty() || minecraft.player == null) return content;
        var lines = stack.getTooltipLines(Item.TooltipContext.EMPTY, minecraft.player, TooltipFlag.NORMAL);
        for (var index = 1; index < lines.size(); index++) {
            var line = lines.get(index);
            if (isModSignature(line)) continue;
            content.add(line);
        }
        return content;
    }

    private boolean isModSignature(Component line) {
        var text = line.getString().toLowerCase(Locale.ROOT);
        if (text.isBlank()) return false;
        for (var name : modNames()) {
            if (text.contains(name)) return true;
        }
        return false;
    }

    private static List<String> modNames() {
        if (MOD_NAMES == null) {
            var names = new ArrayList<String>();
            for (var info : ModList.get().getMods()) {
                collectModName(names, info.getDisplayName());
                collectModName(names, info.getModId());
            }
            MOD_NAMES = List.copyOf(names);
        }
        return MOD_NAMES;
    }

    private static void collectModName(List<String> names, String name) {
        if (name == null) return;
        var normalized = name.toLowerCase(Locale.ROOT).trim();
        if (normalized.length() >= MIN_MOD_NAME_LENGTH) names.add(normalized);
    }

    private boolean inside(int index, double mouseX, double mouseY) {
        var position = cardPositions.get(index);
        return Math.abs(mouseX - position[0]) <= cardWidth * 0.5F && Math.abs(mouseY - position[1]) <= cardHeight * 0.5F;
    }

    private int hoveredCard(double mouseX, double mouseY) {
        for (var index = request.size() - 1; index >= 0; index--)
            if (inside(index, mouseX, mouseY)) return index;
        return -1;
    }

    private Region descriptionFrame(int index) {
        return cardRegion(index, TEXT_UNITS_INSET, TEXT_UNITS_TOP, TEXT_UNITS_W, TEXT_UNITS_H);
    }

    private Region descriptionText(int index) {
        return cardRegion(index, TEXT_UNITS_INNER_X, TEXT_UNITS_INNER_Y, TEXT_UNITS_INNER_W, TEXT_UNITS_INNER_H);
    }

    private Region cardRegion(int index, int unitsX, int unitsY, int unitsW, int unitsH) {
        var position = cardPositions.get(index);
        var lift = cardLifts[index] * HOVER_LIFT * unitScale;
        var left = Mth.floor(position[0] + (unitsX - CARD_UNITS_W * 0.5F) * unitScale);
        var top = Mth.floor(position[1] - lift + (unitsY - CARD_UNITS_H * 0.5F) * unitScale);
        return new Region(left, top, Mth.ceil(left + unitsW * unitScale), Mth.ceil(top + unitsH * unitScale));
    }

    private FormattedText ellipsized(FormattedText text, int width) {
        if (font.width(text) <= width) return text;
        var room = Math.max(0, width - font.width(ELLIPSIS));
        return FormattedText.composite(font.substrByWidth(text, room), Component.literal(ELLIPSIS));
    }

    private int centered(Component text, float scale) {
        return Mth.floor(width * 0.5F / scale - font.width(text) * 0.5F);
    }

    private long gameTime() {
        return minecraft != null && minecraft.level != null ? minecraft.level.getGameTime() : 0L;
    }

    static final class Clock {
        private float animationSeconds;

        void advance(float frameSeconds, boolean animating, float duration) {
            if (!animating) return;

            animationSeconds += frameSeconds;
            if (animationSeconds >= duration) animationSeconds = Math.round(duration / SECONDS_PER_TICK) * SECONDS_PER_TICK;
        }

        float animationSeconds() {
            return animationSeconds;
        }
    }

    private record TooltipAnchor(int left, int top) implements ClientTooltipPositioner {
        static TooltipAnchor at(int left, int top) {
            return new TooltipAnchor(left, top);
        }

        @Override
        public Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
            return new Vector2i(left, top);
        }
    }

    private record TooltipBoxes(int textLeft, int textTop, int itemLeft, int itemTop) {
    }

    private record TooltipBox(int width, int height) {
    }

    private record Region(int left, int top, int right, int bottom) {
        boolean contains(double x, double y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }
}
