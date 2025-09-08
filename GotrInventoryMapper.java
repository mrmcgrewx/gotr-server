package net.runelite.client.plugins.gotr;

import lombok.NonNull;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.server.InvSlotPoint;

import java.awt.Rectangle;
import java.util.*;
import java.util.regex.Pattern;

public final class GotrInventoryMapper {
    private GotrInventoryMapper() {}

    // If you later prefer hard IDs, swap these name checks for ID sets.
    private static final Pattern TALISMAN_NAME = Pattern.compile("(?i)\\btalisman\\b");
    private static final String NAME_FRAGMENTS = "Guardian fragments";
    private static final String NAME_CELLS     = "Uncharged cells";
    private static final String NAME_ESSENCE   = "Guardian essence";
    private static final String NAME_POUCH     = "Colossal pouch";

    public static GotrInvSummary capture(
            @NonNull Client client,
            @NonNull ItemManager itemManager
    ) {
        // Base counts via ItemContainer (fast)
        final ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);
        int empty = 28;
        int fragments = 0, cells = 0, essence = 0;

        if (inv != null) {
            final Item[] items = inv.getItems();
            empty = 28 - (int)Arrays.stream(items).filter(it -> it != null && it.getId() > 0).count();

            for (Item it : items) {
                if (it == null || it.getId() <= 0) continue;
                ItemComposition comp = safeComp(itemManager, it.getId());
                String name = comp != null ? comp.getName() : "";
                int qty = it.getQuantity();

                if (NAME_FRAGMENTS.equalsIgnoreCase(name)) fragments += qty;
                else if (NAME_CELLS.equalsIgnoreCase(name)) cells += qty;
                else if (NAME_ESSENCE.equalsIgnoreCase(name)) essence += qty;
            }
        }

        // Geometry via widget items (for clickable boxes)
        final Widget[] wis = getInventoryWidgetItems(client);
        InvSlotPoint colossal = null;
        List<InvSlotPoint> essenceSlots = new ArrayList<>();
        List<InvSlotPoint> talismanSlots = new ArrayList<>();

        for (Widget w: wis) {
            final int id = w.getItemId();
            //final int slot = wi.getSlot();
            final int qty = w.getItemQuantity();
            final Point p = w.getCanvasLocation(); // This returns a point
            if (id <= 0 || p == null) continue;

            final int width = w.getWidth();
            final int height = w.getHeight();
            final Rectangle r = new Rectangle(p.getX(), p.getY(), width, height);

            final ItemComposition comp = safeComp(itemManager, id);
            final String name = comp != null ? comp.getName() : "";

            final InvSlotPoint sp = new InvSlotPoint(id, name, qty, r.x, r.y, r.width, r.height);

            if (NAME_ESSENCE.equalsIgnoreCase(name)) {
                essenceSlots.add(sp);
            }
            if (NAME_POUCH.equalsIgnoreCase(name)) {
                // If multiple for some reason, pick the first/closest later
                if (colossal == null) colossal = sp;
            }
            if (isTalisman(name)) {
                talismanSlots.add(sp);
            }
        }

        // Build the summary
        return GotrInvSummary.builder()
                .emptySlots(empty)
                .fragments(fragments)
                .unchargedCells(cells)
                .essence(essence)
                .colossalPouch(colossal)
                .essenceSlots(essenceSlots)
                .talismans(talismanSlots)
                .build();

    }

    private static Widget[] getInventoryWidgetItems(Client client) {
        final Widget inv = client.getWidget(WidgetInfo.INVENTORY); // ok even if deprecated
        if (inv == null) return new Widget[0];
        Widget[] children = inv.getChildren();
        if (children == null) children = new Widget[0];
        return children;
    }

    private static ItemComposition safeComp(ItemManager im, int id) {
        try { return im.getItemComposition(id); }
        catch (Exception ignored) { return null; }
    }

    private static boolean isTalisman(String name) {
        return name != null && TALISMAN_NAME.matcher(name).find();
    }
}
