package net.runelite.client.plugins.gotr;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.server.InvSlotPoint;
import net.runelite.client.server.TargetPoint;
import net.runelite.client.server.TargetPointMapper;

import java.awt.Rectangle;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public final class GotrInventoryMapper {
    private GotrInventoryMapper() {}

    // If you later prefer hard IDs, swap these name checks for ID sets.
    private static final Pattern TALISMAN_NAME = Pattern.compile("(?i)\\btalisman\\b");
    private static final String NAME_FRAGMENTS = "Guardian fragments";
    private static final String NAME_CELLS     = "Uncharged cell";
    private static final String NAME_ESSENCE   = "Guardian essence";
    private static final String NAME_POUCH     = "Colossal pouch";
    private static final String NAME_PEARLS    = "Abyssal pearls";

    private static final int ID_COLOSSAL_POUCH_NEW      = 26784;
    private static final int ID_COLOSSAL_POUCH_DEGRADED = 26786;

    public static GotrInvSummary capture(
            @NonNull Client client,
            @NonNull ItemManager itemManager,
            int pouchEssenceTracked
    ) {
        // Base counts via ItemContainer (fast)
        final ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);
        int empty = 28;
        int fragments = 0, cells = 0, essence = 0, pearls = 0;
        boolean colossalDegraded = false;

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
                else if (NAME_PEARLS.equalsIgnoreCase(name)) pearls += qty;

                int id = it.getId();
                if (isColossalPouchId(id)) {
                    colossalDegraded = isColossalDegradedId(id);
                }
            }
        }

        // Geometry via widget items (for clickable boxes)
        final Widget[] wis = getInventoryWidgetItems(client);
        InvSlotPoint colossal = null;
        List<InvSlotPoint> essenceSlots = new ArrayList<>();
        List<InvSlotPoint> talismanSlots = new ArrayList<>();

        for (Widget w: wis) {
            final int id = w.getItemId();
            if (id <= 0 || w.isHidden()) continue;

            // canvas bounds of this slot
            final java.awt.Rectangle rb = w.getBounds();          // canvas-space
            if (rb == null || rb.width <= 0 || rb.height <= 0) continue;

            // center in canvas
            final int cx = rb.x + rb.width / 2;
            final int cy = rb.y + rb.height / 2;

            // canvas → screen
            Integer sx = null, sy = null;
            try {
                java.awt.Point topLeft = client.getCanvas().getLocationOnScreen();
                if (topLeft != null) {
                    sx = topLeft.x + cx;
                    sy = topLeft.y + cy;
                }
            } catch (java.awt.IllegalComponentStateException ignored) {}

            if (sx == null || sy == null) continue;

            final ItemComposition comp = safeComp(itemManager, id);
            final String name = comp != null ? comp.getName() : "";
            final int qty = w.getItemQuantity();

            final InvSlotPoint sp = new InvSlotPoint(id, name, qty, sx, sy);

            if (NAME_ESSENCE.equalsIgnoreCase(name)) {
                if (essenceSlots.isEmpty()) essenceSlots.add(sp);
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
                .pearls(pearls)
                .pouchDegraded(colossalDegraded)
                .pouchEssence(pouchEssenceTracked)
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

    private static boolean isColossalPouchId(int id) {
        return id == ID_COLOSSAL_POUCH_NEW || id == ID_COLOSSAL_POUCH_DEGRADED;
    }
    private static boolean isColossalDegradedId(int id) {
        return id == ID_COLOSSAL_POUCH_DEGRADED;
    }
}
