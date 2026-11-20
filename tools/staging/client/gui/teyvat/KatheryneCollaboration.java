package net.per.primogemcraft.client.gui.teyvat;

import com.guoche.teyvatdelight.entity.katheryne.KatheryneMenu;
import com.guoche.teyvatdelight.entity.katheryne.KatheryneSnapshot;
import com.guoche.teyvatdelight.registry.ModItems;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.collab.teyvatdelight.StellarShopNetwork;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.compat.component.VanillaComponents;
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
    private static final Identifier TASK_PAGE = Identifier.fromNamespaceAndPath(MOD_ID, "stellar_tasks");
    private static final Identifier EXCHANGE_PAGE = Identifier.fromNamespaceAndPath(MOD_ID, "currency_exchange");
    private static final Identifier QUESTION = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/event_question.png");
    private static final Map<Identifier, Page> PAGES = new LinkedHashMap<>();
    private final KatheryneMenu menu;
    private StellarShopNetwork.Snapshot snapshot;
    private List<KatheryneSnapshot.Sale> shopRows = List.of();
    private List<KatheryneSnapshot.Sale> taskRows = List.of();
    private List<KatheryneSnapshot.Sale> exchangeRows = List.of();
    private Page page;
    private boolean buying;
    private long nextRefresh;

    static {
        registerPage(EXCHANGE_PAGE, Text.translatable(TEXT + "exchange_title"),
                collaboration -> collaboration.exchangeRows, KatheryneCollaboration::canExchange, KatheryneCollaboration::exchange);
        registerPage(Identifier.fromNamespaceAndPath(MOD_ID, "stellar_shop"), Text.translatable(TEXT + "title"),
                KatheryneCollaboration::shopRows, KatheryneCollaboration::canBuy, KatheryneCollaboration::buy);
        registerPage(TASK_PAGE, Text.translatable(TASK_TEXT + "title"),
                collaboration -> collaboration.taskRows, KatheryneCollaboration::canSubmit, KatheryneCollaboration::submit);
    }

    public KatheryneCollaboration(KatheryneMenu menu) {
        this.menu = menu;
        page = PAGES.values().iterator().next();
    }

    /** Registers a child page in the collaboration window. */
    public static void registerPage(Identifier id, Text title,
                                    Function<KatheryneCollaboration, List<KatheryneSnapshot.Sale>> rows,
                                    BiPredicate<KatheryneCollaboration, Integer> available,
                                    BiConsumer<KatheryneCollaboration, Integer> purchase) {
        PAGES.put(id, new Page(title, rows, available, purchase));
    }

    /** Receives a snapshot for the current Katheryne window only. */
    public static void receive(StellarShopNetwork.Snapshot snapshot) {
        if (!(Minecraft.getInstance().screen instanceof KatheryneCollaborationScreen screen)) return;
        var collaboration = screen.collaboration();
        if (collaboration.menu.containerId != snapshot.containerId()) return;
        collaboration.snapshot = snapshot;
        collaboration.shopRows = collaboration.createShopRows();
        collaboration.taskRows = collaboration.createTaskRows();
        collaboration.exchangeRows = collaboration.createExchangeRows();
        collaboration.buying = false;
    }

    public void refresh() {
        var now = Util.getMillis();
        if (buying || now < nextRefresh) return;
        nextRefresh = now + 1000;
        PayloadRegistrar.sendToServer(new StellarShopNetwork.Action(menu.containerId, -1, 0, 0));
    }

    public List<KatheryneSnapshot.Sale> rows() {
        return page.rows().apply(this);
    }

    public List<Text> pageTitles() {
        return PAGES.values().stream().map(Page::title).toList();
    }

    public void openPage(int index) {
        if (index >= 0 && index < PAGES.size()) page = new ArrayList<>(PAGES.values()).get(index);
    }

    public boolean selectedPage(int index) {
        return index >= 0 && index < PAGES.size() && page == new ArrayList<>(PAGES.values()).get(index);
    }

    /** Draws shop offer icons while leaving native Katheryne items to its renderer. */
    public boolean renderIcon(DrawContext graphics, ItemStack stack, int x, int y) {
        if (taskPage()) {
            for (var index = 0; index < taskRows.size(); index++) {
                if (taskRows.get(index).outputs().get(0).icon() == stack
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
        if (page.rows().apply(this) != shopRows) return false;
        for (var index = 0; index < shopRows.size(); index++) {
            if (shopRows.get(index).outputs().get(0).icon() != stack) continue;
            if (index < 3) {
                renderCurioIcon(graphics, x, y);
            } else if (index >= 7) {
                graphics.renderItem(new ItemStack(Items.ENCHANTED_BOOK), x, y);
            } else graphics.blit(QUESTION, x, y, 0, 0, 16, 16, 16, 16);
            return true;
        }
        return false;
    }

    private static void renderCurioIcon(DrawContext graphics, int x, int y) {
        graphics.renderItem(new ItemStack(PGCItems.FRUIT_OF_THE_ALIEN_TREE.get()), x, y);
        graphics.getPositionMatrix().pushPose();
        graphics.getPositionMatrix().translate(0, 0, 200);
        graphics.blit(QUESTION, x + 8, y, 8, 8, 0, 0, 16, 16, 16, 16);
        graphics.getPositionMatrix().popPose();
    }

    public boolean available(int index) {
        var rows = rows();
        if (buying || index < 0 || index >= rows.size()) return false;
        var row = rows.get(index);
        return row.remaining() != 0 && page.available().test(this, index);
    }

    public void activate(int index) {
        if (!available(index)) return;
        page.purchase().accept(this, index);
    }

    public boolean taskPage() {
        return page == PAGES.get(TASK_PAGE);
    }

    public boolean exchangePage() {
        return page == PAGES.get(EXCHANGE_PAGE);
    }

    public Text footer() {
        if (snapshot == null) return Text.translatable(TEXT + "loading");
        if (taskPage()) return Text.translatable(TASK_TEXT + "refresh", time(snapshot.details().eventSeconds()));
        if (exchangePage()) return Text.translatable(TEXT + "exchange_refresh", time(snapshot.details().eventSeconds()));
        return Text.translatable(TEXT + "refresh_in", time(snapshot.details().curioSeconds()), time(snapshot.details().eventSeconds()));
    }

    private static String time(int seconds) {
        return String.format(java.util.Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private List<KatheryneSnapshot.Sale> shopRows() {
        return shopRows;
    }

    private boolean canExchange(int index) {
        return snapshot != null && index >= 0 && index < snapshot.details().exchanges().size()
                && snapshot.details().exchanges().get(index).available();
    }

    private void exchange(int index) {
        if (buying || !canExchange(index)) return;
        buying = true;
        PayloadRegistrar.sendToServer(new StellarShopNetwork.Action(menu.containerId, 7 + index, snapshot.details().exchangeRevision(), snapshot.day()));
    }

    private List<KatheryneSnapshot.Sale> createExchangeRows() {
        var result = new ArrayList<KatheryneSnapshot.Sale>();
        for (var index = 0; index < snapshot.details().exchanges().size(); index++) {
            var exchange = snapshot.details().exchanges().get(index);
            var craft = index < 2 ? PGCItems.PRIMOGEM.get() : PGCItems.MORA.get();
            var teyvat = index < 2 ? ModItems.PRIMOGEM.get() : ModItems.MORA.get();
            var output = new ItemStack(index % 2 == 0 ? teyvat : craft);
            VanillaComponents.setCustomName(output, Text.translatable(TEXT + "exchange_item_" + index));
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
                List.of(new KatheryneSnapshot.StackAmount(new ItemStack(ModItems.PRIMOGEM.get()), offer.price())),
                offer.remaining())).toList();
    }

    private List<KatheryneSnapshot.Sale> createTaskRows() {
        return snapshot.details().tasks().stream().map(task -> {
            var reward = CurioGrade.of(task.reward()) != null ? new ItemStack(Items.BOOK) : task.reward().copyWithCount(1);
            VanillaComponents.setCustomName(reward, task.completed()
                    ? Text.translatable(TASK_TEXT + "done", task.title()) : task.title());
            reward.getOrCreateNbt().putBoolean("HideFlags", true);
            StackComponents.remove(reward, PGCDataComponents.CUSTOM_BAR);
            var ingredients = task.ingredients().stream().map(ingredient -> {
                var food = ingredient.copyWithCount(1);
                food.getOrCreateNbt().putBoolean("HideFlags", true);
                StackComponents.remove(food, PGCDataComponents.CUSTOM_BAR);
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
        PayloadRegistrar.sendToServer(new StellarShopNetwork.Action(menu.containerId,
                StellarShopNetwork.TASK_FIRST_SLOT + index, snapshot.details().tasks().get(index).token(), snapshot.day()));
    }

    private static KatheryneSnapshot.StackAmount icon(Text name) {
        var stack = new ItemStack(Items.BOOK);
        VanillaComponents.setCustomName(stack, name.copy().withStyle(ChatFormatting.WHITE));
        return new KatheryneSnapshot.StackAmount(stack, 1);
    }

    private void buy(int index) {
        if (snapshot == null || buying) return;
        buying = true;
        var slot = index >= 7 ? StellarShopNetwork.ENCHANT_FIRST_SLOT + index - 7 : index;
        PayloadRegistrar.sendToServer(new StellarShopNetwork.Action(menu.containerId, slot, snapshot.shopCycle(), snapshot.day()));
    }

    private boolean canBuy(int index) {
        return snapshot != null && index < snapshot.offers().size() && snapshot.offers().get(index).available()
                && snapshot.balance() >= snapshot.offers().get(index).price();
    }

    private record Page(Text title, Function<KatheryneCollaboration, List<KatheryneSnapshot.Sale>> rows,
                        BiPredicate<KatheryneCollaboration, Integer> available,
                        BiConsumer<KatheryneCollaboration, Integer> purchase) {
    }
}
