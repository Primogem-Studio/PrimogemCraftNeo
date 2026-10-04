package net.per.primogemcraft.mixin;

import com.guoche.teyvatdelight.KatheryneMenu;
import com.guoche.teyvatdelight.KatheryneSnapshot;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.client.gui.teyvat.KatheryneCollaboration;
import net.per.primogemcraft.client.gui.teyvat.KatheryneListLayout;
import net.per.primogemcraft.client.gui.teyvat.TeyvatButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "com.guoche.teyvatdelight.KatheryneScreen", remap = false)
public abstract class KatheryneScreenMixin extends AbstractContainerScreen<KatheryneMenu> implements KatheryneCollaboration.Host {
    @Shadow private int tab;
    @Shadow private int scroll;
    @Shadow @Final private List<Button> tabs;
    @Shadow @Final private List<Button> purchases;
    @Shadow private Button submit;
    @Unique private KatheryneCollaboration primogemcraft$pages;
    @Unique private final List<Button> primogemcraft$purchases = new ArrayList<>();
    @Unique private final List<Button> primogemcraft$pageButtons = new ArrayList<>();
    @Unique private Button primogemcraft$previousPage;
    @Unique private Button primogemcraft$nextPage;
    @Unique private int primogemcraft$pageOffset;

    protected KatheryneScreenMixin(KatheryneMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Shadow
    private void updateButtons() {
    }

    @Override
    public KatheryneCollaboration primogemcraft$collaboration() {
        return primogemcraft$pages;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void primogemcraft$resetWidgets(CallbackInfo callback) {
        tabs.clear();
        purchases.clear();
        primogemcraft$purchases.clear();
        primogemcraft$pageButtons.clear();
        primogemcraft$previousPage = null;
        primogemcraft$nextPage = null;
        if (primogemcraft$pages == null) primogemcraft$pages = new KatheryneCollaboration(menu);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void primogemcraft$addTab(CallbackInfo callback) {
        for (var index = 0; index < tabs.size(); index++) {
            var original = tabs.get(index);
            original.setX(leftPos + 8 + index * 67);
            original.setWidth(63);
        }
        tabs.add(addRenderableWidget(new TeyvatButton(leftPos + 209, topPos + 24, 63, 20,
                Component.translatable("gui.primogemcraft.teyvat_collaboration.title"), ignored -> {
                    tab = 3;
                    scroll = 0;
                    primogemcraft$pages.home();
                    updateButtons();
                }, () -> tab == 3)));
        for (var row = 0; row < purchases.size(); row++) {
            var selectedRow = row;
            var original = purchases.get(row);
            var button = new TeyvatButton(original.getX(), original.getY(), original.getWidth(), original.getHeight(),
                    original.getMessage(), ignored -> {
                        if (tab != 3) return;
                        primogemcraft$pages.activate(scroll + selectedRow);
                        updateButtons();
                    }, () -> false);
            button.visible = false;
            primogemcraft$purchases.add(addRenderableWidget(button));
        }
        for (var column = 0; column < 3; column++) {
            var selectedColumn = column;
            var button = new TeyvatButton(leftPos + 8 + column * 67, topPos + 53, 63, 20,
                    Component.empty(), ignored -> {
                        if (!primogemcraft$exchangeLayout()) return;
                        primogemcraft$pages.openPage(primogemcraft$pageOffset + selectedColumn);
                        scroll = 0;
                        updateButtons();
                    }, () -> false);
            button.visible = false;
            primogemcraft$pageButtons.add(addRenderableWidget(button));
        }
        primogemcraft$previousPage = addRenderableWidget(new TeyvatButton(leftPos + 209, topPos + 53, 29, 20,
                Component.translatable("gui.primogemcraft.stellar_shop.previous_page"),
                ignored -> {
                    primogemcraft$pageOffset = Math.max(0, primogemcraft$pageOffset - 3);
                    updateButtons();
                }, () -> false));
        primogemcraft$nextPage = addRenderableWidget(new TeyvatButton(leftPos + 243, topPos + 53, 29, 20,
                Component.translatable("gui.primogemcraft.stellar_shop.next_page"),
                ignored -> {
                    primogemcraft$pageOffset += 3;
                    updateButtons();
                }, () -> false));
        primogemcraft$previousPage.visible = false;
        primogemcraft$nextPage.visible = false;
        updateButtons();
    }

    @Inject(method = "listSize", at = @At("HEAD"), cancellable = true)
    private void primogemcraft$listSize(CallbackInfoReturnable<Integer> callback) {
        if (tab == 3) callback.setReturnValue(primogemcraft$pages.rows().size());
    }

    @Inject(method = "updateButtons", at = @At("HEAD"), cancellable = true)
    private void primogemcraft$updateButtons(CallbackInfo callback) {
        if (tab != 3 || primogemcraft$pages == null) return;
        primogemcraft$pages.refresh();
        var rows = primogemcraft$pages.rows();
        var layout = primogemcraft$listLayout();
        scroll = layout.clampScroll(scroll, rows.size());
        for (var button : tabs) button.active = true;
        if (submit != null) submit.visible = false;
        for (var button : purchases) button.visible = false;
        var titles = primogemcraft$pages.pageTitles();
        primogemcraft$pageOffset = Math.min(primogemcraft$pageOffset, Math.max(0, (titles.size() - 1) / 3 * 3));
        for (var column = 0; column < primogemcraft$pageButtons.size(); column++) {
            var button = primogemcraft$pageButtons.get(column);
            var index = primogemcraft$pageOffset + column;
            button.visible = primogemcraft$pages.homePage() && index < titles.size();
            if (button.visible) button.setMessage(titles.get(index));
        }
        if (primogemcraft$previousPage != null) {
            primogemcraft$previousPage.visible = primogemcraft$pages.homePage() && titles.size() > 3;
            primogemcraft$nextPage.visible = primogemcraft$previousPage.visible;
            primogemcraft$previousPage.active = primogemcraft$pageOffset > 0;
            primogemcraft$nextPage.active = primogemcraft$pageOffset + 3 < titles.size();
        }
        for (var row = 0; row < primogemcraft$purchases.size(); row++) {
            var button = primogemcraft$purchases.get(row);
            var index = scroll + row;
            button.visible = row < layout.visibleRows() && index < rows.size();
            button.setY(topPos + layout.rowTop(row));
            button.active = button.visible && primogemcraft$pages.available(index);
            button.setMessage(Component.translatable(primogemcraft$pages.homePage()
                    ? "gui.primogemcraft.stellar_shop.exchange" : "gui.teyvatdelight.katheryne.buy"));
        }
        callback.cancel();
    }

    @Inject(method = "updateButtons", at = @At("TAIL"))
    private void primogemcraft$restoreButtons(CallbackInfo callback) {
        if (tab == 3) return;
        for (var button : primogemcraft$purchases) button.visible = false;
        for (var button : primogemcraft$pageButtons) button.visible = false;
        if (primogemcraft$previousPage != null) {
            primogemcraft$previousPage.visible = false;
            primogemcraft$nextPage.visible = false;
        }
    }

    @Inject(method = "renderLabels", at = @At("TAIL"))
    private void primogemcraft$exchangeSection(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo callback) {
        if (tab != 3 || !primogemcraft$pages.homePage()) return;
        var heading = Component.translatable("gui.primogemcraft.stellar_shop.exchange_title");
        graphics.drawString(font, Language.getInstance().getVisualOrder(font.substrByWidth(heading, imageWidth - 16)),
                8, 83, 0xFFE9F0EE, false);
    }

    @WrapOperation(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"))
    private void primogemcraft$offerIcon(GuiGraphics graphics, ItemStack stack, int x, int y, Operation<Void> original) {
        if (tab != 3 || !primogemcraft$pages.renderIcon(graphics, stack, x, y)) original.call(graphics, stack, x, y);
    }

    @ModifyExpressionValue(method = "renderLabels", at = @At(value = "FIELD", target = "Lcom/guoche/teyvatdelight/KatheryneScreen;tab:I"))
    private int primogemcraft$shopLayout(int original) {
        return original == 3 ? 1 : original;
    }

    @ModifyExpressionValue(method = "renderLabels", at = @At(value = "INVOKE", target = "Lcom/guoche/teyvatdelight/KatheryneMenu;snapshot()Lcom/guoche/teyvatdelight/KatheryneSnapshot;"))
    private KatheryneSnapshot primogemcraft$shopData(KatheryneSnapshot original) {
        return tab == 3 ? primogemcraft$pages.presentation(original) : original;
    }

    @Inject(method = "outputsForRow", at = @At("HEAD"), cancellable = true)
    private void primogemcraft$outputs(int index, CallbackInfoReturnable<List<KatheryneSnapshot.StackAmount>> callback) {
        if (tab == 3) callback.setReturnValue(primogemcraft$pages.rows().get(index).outputs());
    }

    @Inject(method = "pricesForRow", at = @At("HEAD"), cancellable = true)
    private void primogemcraft$prices(int index, CallbackInfoReturnable<List<KatheryneSnapshot.StackAmount>> callback) {
        if (tab == 3) callback.setReturnValue(primogemcraft$pages.rows().get(index).prices());
    }

    @ModifyArg(method = "renderLabels", index = 1,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/guoche/teyvatdelight/KatheryneMenu;getShopSecondsUntilRefresh()I")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"))
    private Component primogemcraft$footer(Component original) {
        return tab == 3 ? primogemcraft$pages.footer() : original;
    }

    @Unique
    private boolean primogemcraft$exchangeLayout() {
        return tab == 3 && primogemcraft$pages != null && primogemcraft$pages.homePage();
    }

    @Unique
    private KatheryneListLayout primogemcraft$listLayout() {
        return primogemcraft$exchangeLayout() ? KatheryneListLayout.EXCHANGE : KatheryneListLayout.SHOP;
    }

    @ModifyConstant(method = {"render", "renderBg", "renderLabels", "mouseClicked", "mouseDragged"}, constant = @Constant(intValue = 51))
    private int primogemcraft$listTop(int original) {
        return tab == 3 ? primogemcraft$listLayout().top() : original;
    }

    @ModifyConstant(method = {"render", "renderBg", "mouseScrolled", "mouseClicked", "mouseDragged"}, constant = @Constant(intValue = 5))
    private int primogemcraft$visibleRows(int original) {
        return tab == 3 ? primogemcraft$listLayout().visibleRows() : original;
    }

    @ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 5),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/guoche/teyvatdelight/KatheryneMenu;snapshot()Lcom/guoche/teyvatdelight/KatheryneSnapshot;"),
                    to = @At(value = "INVOKE", target = "Lcom/guoche/teyvatdelight/KatheryneScreen;listSize()I")))
    private int primogemcraft$labelRows(int original) {
        return tab == 3 ? primogemcraft$listLayout().visibleRows() : original;
    }

    @ModifyConstant(method = "renderBg", constant = @Constant(intValue = 119))
    private int primogemcraft$trackHeight(int original) {
        return tab == 3 ? primogemcraft$listLayout().height() : original;
    }

    @ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 49))
    private int primogemcraft$wheelTop(int original) {
        return tab == 3 ? primogemcraft$listLayout().top() - 2 : original;
    }

    @Inject(method = "scrollFromMouse", at = @At("HEAD"), cancellable = true)
    private void primogemcraft$dragScroll(double mouseY, CallbackInfo callback) {
        if (tab != 3) return;
        scroll = primogemcraft$listLayout().scrollAt(mouseY - topPos, primogemcraft$pages.rows().size());
        updateButtons();
        callback.cancel();
    }

    @ModifyConstant(method = "renderLabels", constant = {@Constant(intValue = -1445659), @Constant(intValue = -5389638)})
    private int primogemcraft$textColor(int original) {
        return tab == 3 ? original == -5389638 ? 0xFFB6C9C3 : 0xFFE9F0EE : original;
    }
}
