package net.runelite.client.plugins.gotr;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import net.runelite.client.server.InvSlotPoint;
import net.runelite.client.server.InvSummary;

import java.util.List;

/** GOTR-specific inventory summary. All coordinates are CANVAS-relative. */
@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GotrInvSummary extends InvSummary {
    int fragments;           // Guardian fragments (total)
    int unchargedCells;      // Uncharged cells (total)
    int essence;             // Guardian essence (total)

    InvSlotPoint colossalPouch;       // where to click to empty/fill
    List<InvSlotPoint> essenceSlots;  // every slot containing guardian essence
    List<InvSlotPoint> talismans;     // any talismans present with their slots
}
