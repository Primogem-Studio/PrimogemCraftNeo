package net.per.primogemcraft.client.gui.teyvat;

import com.guoche.teyvatdelight.KatheryneMenu;
import com.guoche.teyvatdelight.KatheryneSnapshot;
import com.guoche.teyvatdelight.client.katheryne.KatheryneScreen;
import com.guoche.teyvatdelight.entity.katheryne.KatheryneNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;

public final class KatheryneCollaborationScreen extends AbstractContainerScreen<KatheryneMenu> {
    private static final String TEXT = "gui.teyvatdelight.katheryne.";
    private static final ResourceLocation PORTRAIT = ResourceLocation.fromNamespaceAndPath("teyvatdelight", "textures/gui/katheryne_portrait.png");
    private static final int BACKGROUND = 0xC01C2527;
    private static final int BORDER = 0xFF779693;
    private static final int FOREGROUND = 0xFFE9F0E5;
    private static final int MUTED = 0xFFADC2BA;
    private static final int TRACK = 0xFF344143;
    private static final KatheryneListLayout LAYOUT = KatheryneListLayout.SHOP;
    private final KatheryneScreen parent;
    private final KatheryneCollaboration collaboration;
    private final List<Button> purchases = new ArrayList<>();
    private int scroll;
    private int bonusScroll;
    private boolean dragging;
    private ItemStack hoveredItem = ItemStack.EMPTY;
    private List<Component> hoveredText = List.of();
    private KatheryneSnapshot renderedSnapshot;

    private KatheryneCollaborationScreen(KatheryneScreen parent, Inventory inventory) {
        super(parent.getMenu(), inventory, parent.getTitle());
        this.parent = parent;
        collaboration = new KatheryneCollaboration(menu);
        imageWidth = 320;
        imageHeight = 210;
    }

    public static void addEntry(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof KatheryneScreen screen)) return;
        event.addListener(new TeyvatButton(screen.getGuiLeft() + screen.getXSize() - 88,
                Math.max(0, screen.getGuiTop() - 20), 80, 18,
                Component.translatable("gui.primogemcraft.teyvat_collaboration.title"), ignored -> {
                    var minecraft = Minecraft.getInstance();
                    if (minecraft.player != null && minecraft.player.containerMenu == screen.getMenu())
                        minecraft.setScreen(new KatheryneCollaborationScreen(screen, minecraft.player.getInventory()));
                }, () -> false));
    }

    public KatheryneCollaboration collaboration() {
        return collaboration;
    }

    @Override
    protected void init() {
        super.init();
        purchases.clear();
        dragging = false;
        addRenderableWidget(new TeyvatButton(leftPos + 232, Math.max(0, topPos - 20), 80, 18, CommonComponents.GUI_BACK,
                ignored -> minecraft.setScreen(parent), () -> false));
        var titles = collaboration.pageTitles();
        var tabWidth = 306 / titles.size();
        for (var index = 0; index < titles.size(); index++) {
            var page = index;
            addRenderableWidget(new TeyvatButton(leftPos + 8 + index * tabWidth, topPos + 24,
                    tabWidth - 4, 20, titles.get(index), ignored -> {
                        collaboration.openPage(page);
                        scroll = 0;
                    }, () -> collaboration.selectedPage(page)));
        }
        for (var row = 0; row < LAYOUT.visibleRows(); row++) {
            var selectedRow = row;
            purchases.add(addRenderableWidget(new TeyvatButton(leftPos + 263, topPos + LAYOUT.rowTop(row),
                    45, 19, Component.empty(), ignored -> collaboration.activate(scroll + selectedRow), () -> false)));
        }
        updateButtons();
    }

    private void updateButtons() {
        scroll = LAYOUT.clampScroll(scroll, collaboration.rows().size());
        for (var row = 0; row < purchases.size(); row++) {
            var button = purchases.get(row);
            var index = scroll + row;
            button.visible = index < collaboration.rows().size();
            button.active = collaboration.available(index);
            button.setX(leftPos + (collaboration.taskPage() ? 251 : 263));
            button.setWidth(collaboration.taskPage() ? 57 : 45);
            button.setMessage(Component.translatable(collaboration.taskPage() ? "gui.primogemcraft.stellar_tasks.submit"
                    : collaboration.exchangePage() ? "gui.primogemcraft.stellar_shop.exchange" : TEXT + "buy"));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        collaboration.refresh();
        updateButtons();
        hoveredItem = ItemStack.EMPTY;
        hoveredText = List.of();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderedSnapshot = menu.snapshot();
        if (!hoveredText.isEmpty())
            graphics.renderTooltip(font, hoveredText.stream().flatMap(line -> font.split(line, Math.min(260, width - 24)).stream()).toList(), mouseX, mouseY);
        else if (!hoveredItem.isEmpty()) graphics.renderTooltip(font, hoveredItem, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, BORDER);
        graphics.fill(leftPos, topPos + imageHeight - 2, leftPos + imageWidth, topPos + imageHeight, BORDER);
        graphics.fill(leftPos, topPos + 2, leftPos + 2, topPos + imageHeight - 2, BORDER);
        graphics.fill(leftPos + imageWidth - 2, topPos + 2, leftPos + imageWidth, topPos + imageHeight - 2, BORDER);
        graphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, BACKGROUND);
        graphics.fill(leftPos + 8, topPos + 47, leftPos + imageWidth - 8, topPos + 48, BORDER);
        var size = collaboration.rows().size();
        for (var row = 1; row < Math.min(LAYOUT.visibleRows(), size - scroll); row++) {
            var y = topPos + 49 + row * 24;
            graphics.fill(leftPos + 10, y, leftPos + 309, y + 1, TRACK);
        }
        if (LAYOUT.maxScroll(size) == 0) return;
        var thumb = LAYOUT.thumbHeight(size);
        graphics.fill(leftPos + 311, topPos + LAYOUT.top(), leftPos + 317, topPos + LAYOUT.top() + LAYOUT.height(), TRACK);
        var thumbTop = topPos + LAYOUT.thumbTop(scroll, size);
        graphics.fill(leftPos + 311, thumbTop, leftPos + 317, thumbTop + thumb, BORDER);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        renderHeader(graphics, mouseX, mouseY);
        var rows = collaboration.rows();
        for (var row = 0; row < LAYOUT.visibleRows() && scroll + row < rows.size(); row++) {
            var offer = rows.get(scroll + row);
            var y = LAYOUT.rowTop(row);
            var output = offer.outputs().getFirst();
            renderAmount(graphics, output, 11, y + 1, output.count() > 1, mouseX, mouseY);
            if (collaboration.taskPage()) {
                var ingredient = offer.prices().getFirst();
                var count = offer.remaining() == 0 ? ingredient.count() : Math.min(menu.countItem(ingredient.icon().getItem()), ingredient.count());
                var progress = count + "/" + ingredient.count();
                graphics.drawString(font, font.plainSubstrByWidth(output.icon().getHoverName().getString(), 206 - font.width(progress)),
                        32, y, offer.remaining() == 0 ? 0xFF82948C : FOREGROUND, false);
                graphics.drawString(font, progress, 244 - font.width(progress), y, collaboration.available(scroll + row) ? 0xFF90C79D : FOREGROUND, false);
                var requirements = Component.empty();
                for (var cost : offer.prices()) {
                    if (!requirements.getString().isEmpty()) requirements.append(" / ");
                    requirements.append(Component.literal(cost.count() + " x ").append(cost.icon().getHoverName()));
                }
                graphics.drawString(font, font.plainSubstrByWidth(requirements.getString(), 210), 32, y + 11, MUTED, false);
                if (inside(mouseX, mouseY, 10, y, 250, y + 23)) {
                    var lines = new ArrayList<Component>();
                    lines.add(output.icon().getHoverName());
                    lines.addAll(stackLines(offer.prices()));
                    lines.add(Component.translatable(TEXT + "reward_label").append(": ").append(stackLines(offer.outputs()).getFirst()));
                    hoveredText = lines;
                }
            } else {
                graphics.drawString(font, font.plainSubstrByWidth(output.icon().getHoverName().getString(), 146), 31, y, FOREGROUND, false);
                var stock = Component.translatable(TEXT + (offer.remaining() < 0 ? "unlimited" : offer.remaining() == 0 ? "sold_out" : "remaining"), offer.remaining());
                graphics.drawString(font, stock, 31, y + 11, MUTED, false);
                if (inside(mouseX, mouseY, 31, y, 180, y + 22)) hoveredText = stackLines(offer.outputs());
                var costX = 239 - (Math.min(3, offer.prices().size()) - 1) * 27;
                for (var index = 0; index < Math.min(3, offer.prices().size()); index++)
                    renderAmount(graphics, offer.prices().get(index), costX + index * 27, y, true, mouseX, mouseY);
                if (inside(mouseX, mouseY, 182, y, 261, y + 22) && hoveredItem.isEmpty()) hoveredText = stackLines(offer.prices());
            }
        }
        var footer = collaboration.footer();
        graphics.drawString(font, footer, collaboration.taskPage() ? imageWidth - 11 - font.width(footer) : 11, 196, FOREGROUND, false);
    }

    private void renderHeader(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.blit(PORTRAIT, 9, 5, 16, 16, 16.0F, 14.0F, 32, 32, 64, 64);
        graphics.drawString(font, font.plainSubstrByWidth(title.getString(), 50), 31, 9, FOREGROUND, false);
        var view = menu.commissionView();
        graphics.fill(83, 12, 127, 16, TRACK);
        var fill = view.claimable() > 0 ? 44 : (int) Math.min(44L, 44L * view.remainder() / Math.max(1, view.every()));
        graphics.fill(83, 12, 83 + fill, 16, view.claimable() > 0 ? -6641339 : -12093076);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        graphics.drawCenteredString(font, Component.literal(compact(view.claimable() > 0 ? view.every() : view.remainder()) + "/" + compact(view.every())),
                105, 9, FOREGROUND);
        graphics.pose().popPose();
        var capacity = bonusCapacity();
        bonusScroll = Math.clamp(bonusScroll, 0, Math.max(0, view.bonus().size() - capacity));
        for (var index = 0; index < Math.min(capacity, view.bonus().size() - bonusScroll); index++) {
            var reward = view.bonus().get(bonusScroll + index);
            renderAmount(graphics, new KatheryneSnapshot.StackAmount(item(reward.item()), reward.count()), 132 + index * 24, 5, true, mouseX, mouseY);
            if (view.claimable() > 0) {
                var x = 143 + index * 24;
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 300);
                graphics.fill(x - 2, 1, x + 4, 12, -2150352);
                graphics.fill(x, 3, x + 2, 7, -1);
                graphics.fill(x, 8, x + 2, 10, -1);
                graphics.pose().popPose();
            }
        }
        if (view.bonus().size() > capacity) {
            var track = capacity * 24 - 8;
            var thumb = Math.max(4, track * capacity / view.bonus().size());
            var offset = bonusScroll * (track - thumb) / (view.bonus().size() - capacity);
            graphics.fill(132, 22, 132 + track, 23, TRACK);
            graphics.fill(132 + offset, 22, 132 + offset + thumb, 23, BORDER);
        }
        for (var balance : balances()) {
            renderAmount(graphics, new KatheryneSnapshot.StackAmount(balance.icon(), balance.count()), balance.x(), 5, false, mouseX, mouseY);
            graphics.drawString(font, compact(balance.count()), balance.x() + 18, 9, FOREGROUND, false);
            if (inside(mouseX, mouseY, balance.x(), 5, balance.right(), 22))
                hoveredText = List.of(balance.icon().getHoverName().copy().append(": " + balance.count()));
        }
        if (inside(mouseX, mouseY, 83, 5, 127, 22) || inside(mouseX, mouseY, 132, 5, 132 + capacity * 24, 23)) {
            var lines = new ArrayList<Component>();
            lines.add(Component.translatable(TEXT + "bonus", view.claimable()));
            if (view.claimable() > 0) lines.add(Component.translatable(TEXT + "click_bonus"));
            for (var reward : view.bonus())
                lines.add(Component.literal(reward.count() + " x ").append(item(reward.item()).getHoverName()));
            hoveredText = lines;
        }
    }

    private List<Balance> balances() {
        var configured = menu.commissionView().balanceItems();
        if (configured == null) return List.of();
        var result = new ArrayList<Balance>();
        var right = imageWidth - 9;
        for (var index = Math.min(2, configured.size()) - 1; index >= 0; index--) {
            var icon = item(configured.get(index));
            if (icon.isEmpty()) continue;
            var count = menu.countItem(icon.getItem());
            var left = right - font.width(compact(count)) - 18;
            result.add(new Balance(icon, count, left, right));
            right = left - 8;
        }
        return result;
    }

    private int bonusCapacity() {
        var left = balances().stream().mapToInt(Balance::x).min().orElse(imageWidth - 9);
        return Math.max(1, (left - 6 - 132) / 24);
    }

    private static ItemStack item(String name) {
        var id = ResourceLocation.tryParse(name);
        return id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(id));
    }

    private static List<Component> stackLines(List<KatheryneSnapshot.StackAmount> stacks) {
        return stacks.stream().<Component>map(stack -> Component.literal(stack.count() + " x ").append(stack.icon().getHoverName())).toList();
    }

    private static String compact(int count) {
        return count >= 1000000 ? count / 1000000 + "m" : count >= 1000 ? count / 1000 + "k" : Integer.toString(count);
    }

    private boolean inside(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= leftPos + x1 && mouseX < leftPos + x2 && mouseY >= topPos + y1 && mouseY < topPos + y2;
    }

    private record Balance(ItemStack icon, int count, int x, int right) {
    }

    private void renderAmount(GuiGraphics graphics, KatheryneSnapshot.StackAmount amount, int x, int y, boolean count, int mouseX, int mouseY) {
        var stack = amount.icon();
        if (y < LAYOUT.top() || !collaboration.renderIcon(graphics, stack, x, y)) graphics.renderItem(stack, x, y);
        if (count) graphics.renderItemDecorations(font, stack, x, y, compact(amount.count()));
        if (mouseX >= leftPos + x && mouseX < leftPos + x + 16 && mouseY >= topPos + y && mouseY < topPos + y + 16)
            hoveredItem = stack;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (inside(mouseX, mouseY, 132, 5, 132 + bonusCapacity() * 24, 23)) {
            bonusScroll = Math.clamp(bonusScroll - (int) Math.signum(vertical), 0, Math.max(0, menu.commissionView().bonus().size() - bonusCapacity()));
            return true;
        }
        if (mouseX >= leftPos + 8 && mouseX < leftPos + 317 && mouseY >= topPos + LAYOUT.top()
                && mouseY < topPos + LAYOUT.top() + LAYOUT.height()) {
            scroll = LAYOUT.clampScroll(scroll - (int) Math.signum(vertical), collaboration.rows().size());
            updateButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && menu.commissionView().claimable() > 0 && renderedSnapshot == menu.snapshot()) {
            for (var index = 0; index < Math.min(bonusCapacity(), menu.commissionView().bonus().size() - bonusScroll); index++) {
                if (inside(mouseX, mouseY, 132 + index * 24, 3, 150 + index * 24, 22)) {
                    KatheryneNetwork.sendAction(menu.containerId, 3, 0, renderedSnapshot.revision(), "");
                    return true;
                }
            }
        }
        if (button == 0 && LAYOUT.maxScroll(collaboration.rows().size()) > 0
                && mouseX >= leftPos + 311 && mouseX < leftPos + 317
                && mouseY >= topPos + LAYOUT.top() && mouseY < topPos + LAYOUT.top() + LAYOUT.height()) {
            dragging = true;
            scroll = LAYOUT.scrollAt(mouseY - topPos, collaboration.rows().size());
            updateButtons();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && dragging) {
            scroll = LAYOUT.scrollAt(mouseY - topPos, collaboration.rows().size());
            updateButtons();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
