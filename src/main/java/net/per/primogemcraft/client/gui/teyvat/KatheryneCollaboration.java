package net.per.primogemcraft.client.gui.teyvat;

import com.guoche.teyvatdelight.KatheryneMenu;
import com.guoche.teyvatdelight.KatheryneSnapshot;
import com.guoche.teyvatdelight.TeyvatDelight;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.collab.teyvatdelight.StellarShopNetwork;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.CurioGrade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class KatheryneCollaboration {
    private static final String TEXT = "gui.primogemcraft.stellar_shop.";
    private static final String TASK_TEXT = "gui.primogemcraft.stellar_tasks.";
    private static final ResourceLocation TASK_PAGE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "stellar_tasks");
    private static final ResourceLocation QUESTION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/event_question.png");
    private static final Map<ResourceLocation, Page> PAGES = new LinkedHashMap<>();
    private final KatheryneMenu menu;
    private StellarShopNetwork.Snapshot snapshot;
    private List<KatheryneSnapshot.Sale> shopRows = List.of();
    private List<KatheryneSnapshot.Sale> exchangeRows = List.of();
    private List<KatheryneSnapshot.Sale> taskRows = List.of();
    private Page page;
    private boolean buying;
    private long nextRefresh;

    static {
        registerPage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "stellar_shop"), Component.translatable(TEXT + "title"),
                KatheryneCollaboration::shopRows, KatheryneCollaboration::canBuy, KatheryneCollaboration::buy);
        registerPage(TASK_PAGE, Component.translatable(TASK_TEXT + "title"),
                collaboration -> collaboration.taskRows, KatheryneCollaboration::canSubmit, KatheryneCollaboration::submit);
    }

    public KatheryneCollaboration(KatheryneMenu menu) {
        this.menu = menu;
    }

    /** Registers a child page rendered by Katheryne's existing shop list and scrollbar. */
    public static void registerPage(ResourceLocation id, Component title,
                                    Function<KatheryneCollaboration, List<KatheryneSnapshot.Sale>> rows,
                                    BiPredicate<KatheryneCollaboration, Integer> available,
                                    BiConsumer<KatheryneCollaboration, Integer> purchase) {
        PAGES.put(id, new Page(title, rows, available, purchase));
    }

    /** Receives a snapshot for the current Katheryne window only. */
    public static void receive(StellarShopNetwork.Snapshot snapshot) {
        if (!(Minecraft.getInstance().screen instanceof Host host)) return;
        var collaboration = host.primogemcraft$collaboration();
        if (collaboration == null || collaboration.menu.containerId != snapshot.containerId()) return;
        collaboration.snapshot = snapshot;
        collaboration.shopRows = collaboration.createShopRows();
        collaboration.exchangeRows = collaboration.createExchangeRows();
        collaboration.taskRows = collaboration.createTaskRows();
        collaboration.buying = false;
    }

    public void home() {
        page = null;
        refresh();
    }

    public void refresh() {
        var now = Util.getMillis();
        if (buying || now < nextRefresh) return;
        nextRefresh = now + 1000;
        PacketDistributor.sendToServer(new StellarShopNetwork.Action(menu.containerId, -1, 0, 0));
    }

    public List<KatheryneSnapshot.Sale> rows() {
        if (page != null) return page.rows().apply(this);
        return exchangeRows;
    }

    public List<Component> pageTitles() {
        return PAGES.values().stream().map(Page::title).toList();
    }

    public void openPage(int index) {
        if (index >= 0 && index < PAGES.size()) page = new ArrayList<>(PAGES.values()).get(index);
    }

    /** Draws shop offer icons while leaving native Katheryne items to its renderer. */
    public boolean renderIcon(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (taskPage()) {
            for (var index = 0; index < taskRows.size(); index++) {
                if (taskRows.get(index).outputs().getFirst().icon() == stack
                        && CurioGrade.of(snapshot.details().tasks().get(index).reward()) != null) {
                    renderCurioIcon(graphics, x, y);
                    return true;
                }
            }
        }
        if (taskPage() && stack.is(Items.BOOK)) {
            graphics.blit(QUESTION, x, y, 0, 0, 16, 16, 16, 16);
            return true;
        }
        if (page == null || page.rows().apply(this) != shopRows) return false;
        for (var index = 0; index < shopRows.size(); index++) {
            if (shopRows.get(index).outputs().getFirst().icon() != stack) continue;
            if (index < 3) {
                renderCurioIcon(graphics, x, y);
            } else if (index >= 7) {
                graphics.renderItem(new ItemStack(Items.ENCHANTED_BOOK), x, y);
            } else graphics.blit(QUESTION, x, y, 0, 0, 16, 16, 16, 16);
            return true;
        }
        return false;
    }

    private static void renderCurioIcon(GuiGraphics graphics, int x, int y) {
        graphics.renderItem(new ItemStack(PGCItems.FRUIT_OF_THE_ALIEN_TREE.get()), x, y);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        graphics.blit(QUESTION, x + 8, y, 8, 8, 0, 0, 16, 16, 16, 16);
        graphics.pose().popPose();
    }

    public boolean available(int index) {
        var rows = rows();
        if (buying || index < 0 || index >= rows.size()) return false;
        var row = rows.get(index);
        return row.remaining() != 0 && (page == null ? canExchange(index) : page.available().test(this, index));
    }

    public void activate(int index) {
        if (!available(index)) return;
        if (page != null) page.purchase().accept(this, index);
        else exchange(index);
    }

    public boolean homePage() {
        return page == null;
    }

    public boolean taskPage() {
        return page == PAGES.get(TASK_PAGE);
    }

    public Component footer() {
        if (snapshot == null) return Component.translatable(TEXT + "loading");
        if (taskPage()) return Component.translatable(TASK_TEXT + "refresh", time(snapshot.details().eventSeconds()));
        if (homePage()) return Component.translatable(TEXT + "exchange_refresh", time(snapshot.details().eventSeconds()));
        return Component.translatable(TEXT + "refresh_in", time(snapshot.details().curioSeconds()), time(snapshot.details().eventSeconds()));
    }

    private static String time(int seconds) {
        return String.format(java.util.Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    public boolean canExchange(int index) {
        return !buying && snapshot != null && index >= 0 && index < snapshot.details().exchanges().size()
                && snapshot.details().exchanges().get(index).available();
    }

    public void exchange(int index) {
        if (!canExchange(index)) return;
        var exchange = snapshot.details().exchanges().get(index);
        buying = true;
        PacketDistributor.sendToServer(new StellarShopNetwork.Action(menu.containerId, 7 + index, exchange.cost() * 65L + exchange.reward(), snapshot.day()));
    }

    public KatheryneSnapshot presentation(KatheryneSnapshot original) {
        return new KatheryneSnapshot(original.menuId(), original.target(), original.completed(), original.secondsUntilRefresh(),
                original.immediateRefresh(), original.rewards(), rows(), original.daily());
    }

    private List<KatheryneSnapshot.Sale> shopRows() {
        return shopRows;
    }

    private List<KatheryneSnapshot.Sale> createExchangeRows() {
        var result = new ArrayList<KatheryneSnapshot.Sale>();
        for (var index = 0; index < snapshot.details().exchanges().size(); index++) {
            var exchange = snapshot.details().exchanges().get(index);
            var craft = index < 2 ? PGCItems.PRIMOGEM.get() : PGCItems.MORA.get();
            var teyvat = index < 2 ? TeyvatDelight.PRIMOGEM.get() : TeyvatDelight.MORA.get();
            var output = new ItemStack(index % 2 == 0 ? teyvat : craft);
            output.set(DataComponents.CUSTOM_NAME, Component.translatable(TEXT + "exchange_item_" + index));
            result.add(new KatheryneSnapshot.Sale(
                    List.of(new KatheryneSnapshot.StackAmount(output, exchange.reward())),
                    List.of(new KatheryneSnapshot.StackAmount(new ItemStack(index % 2 == 0 ? craft : teyvat), exchange.cost())),
                    exchange.remaining()));
        }
        return result;
    }

    private List<KatheryneSnapshot.Sale> createShopRows() {
        if (snapshot == null) return List.of();
        return snapshot.offers().stream().map(offer -> new KatheryneSnapshot.Sale(
                List.of(icon(offer.title())),
                List.of(new KatheryneSnapshot.StackAmount(new ItemStack(TeyvatDelight.PRIMOGEM.get()), offer.price())),
                offer.remaining())).toList();
    }

    private List<KatheryneSnapshot.Sale> createTaskRows() {
        return snapshot.details().tasks().stream().map(task -> {
            var reward = CurioGrade.of(task.reward()) != null ? new ItemStack(Items.BOOK) : task.reward().copyWithCount(1);
            reward.set(DataComponents.CUSTOM_NAME, task.completed()
                    ? Component.translatable(TASK_TEXT + "done", task.title()) : task.title());
            reward.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
            reward.remove(PGCDataComponents.CUSTOM_BAR.get());
            var ingredients = task.ingredients().stream().map(ingredient -> {
                var food = ingredient.copyWithCount(1);
                food.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                food.remove(PGCDataComponents.CUSTOM_BAR.get());
                return new KatheryneSnapshot.StackAmount(food, ingredient.getCount());
            }).toList();
            return new KatheryneSnapshot.Sale(
                    List.of(new KatheryneSnapshot.StackAmount(reward, task.reward().getCount())),
                    ingredients,
                    task.completed() ? 0 : 1);
        }).toList();
    }

    private boolean canSubmit(int index) {
        return snapshot != null && index >= 0 && index < snapshot.details().tasks().size()
                && snapshot.details().tasks().get(index).available();
    }

    private void submit(int index) {
        if (buying || !canSubmit(index)) return;
        buying = true;
        PacketDistributor.sendToServer(new StellarShopNetwork.Action(menu.containerId,
                StellarShopNetwork.TASK_FIRST_SLOT + index, snapshot.details().tasks().get(index).token(), snapshot.day()));
    }

    private static KatheryneSnapshot.StackAmount icon(Component name) {
        var stack = new ItemStack(Items.BOOK);
        stack.set(DataComponents.CUSTOM_NAME, name.copy().withStyle(ChatFormatting.WHITE));
        return new KatheryneSnapshot.StackAmount(stack, 1);
    }

    private void buy(int index) {
        if (snapshot == null || buying) return;
        buying = true;
        var slot = index >= 7 ? StellarShopNetwork.ENCHANT_FIRST_SLOT + index - 7 : index;
        PacketDistributor.sendToServer(new StellarShopNetwork.Action(menu.containerId, slot, snapshot.shopCycle(), snapshot.day()));
    }

    private boolean canBuy(int index) {
        return snapshot != null && index < snapshot.offers().size() && snapshot.offers().get(index).available()
                && snapshot.balance() >= snapshot.offers().get(index).price();
    }

    /** Implemented by the optional client mixin; retains state on the original screen. */
    public interface Host {
        KatheryneCollaboration primogemcraft$collaboration();
    }

    private record Page(Component title, Function<KatheryneCollaboration, List<KatheryneSnapshot.Sale>> rows,
                        BiPredicate<KatheryneCollaboration, Integer> available,
                        BiConsumer<KatheryneCollaboration, Integer> purchase) {
    }
}
