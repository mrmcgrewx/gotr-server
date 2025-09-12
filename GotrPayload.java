package net.runelite.client.plugins.gotr;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.server.*;

import java.awt.*;
import java.awt.Point;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.server.TargetPointMapper.mapCommon;
import static net.runelite.client.server.TargetPointMapper.safeObjectName;

@Data
@EqualsAndHashCode(callSuper = true)
public class GotrPayload extends Payload {
    private final List<Integer> inventoryTalismans;
    private final List<TargetPoint> activeGuardians;
    private final List<TargetPoint> cellTiles;
    private final List<TargetPoint> hugeGuardians;
    private final List<TargetPoint> largeGuardians;
    private final TargetPoint greatGuardian;
    private final TargetPoint apprentice;
    private final TargetPoint unchargedCellTable;
    private final TargetPoint depositPool;
    private final TargetPoint catalyticEssencePile;
    private final TargetPoint elementalEssencePile;
    private final TargetPoint portal;
    private final TargetPoint returnPortal;
    private final TargetPoint workbench;
    private final TargetPoint barrier;
    private final TargetPoint rubbleTop;
    private final TargetPoint rubbleBottom;
    private final TargetPoint currentAltar;
    private final TargetPoint altarPortal;
    private final int guardianEnergy;

    private final boolean isInMinigame;
    private final boolean hasAnyRunes;
    private final boolean hasAnyGuardianEssence;
    private final boolean isFirstPortal;

    private final Instant portalSpawnTime;
    private final Instant lastPortalDespawnTime;
    private final Instant nextGameStart;
    private final Instant gameStarted;

    private final List<TargetPoint> bankTiles;
    private final TargetPoint bankChest;
    private final TargetPoint bankCloseButton;
    private final TargetPoint bankFirstSlot;

    public GotrPayload(
            Client client,
            long seq,
            long ts,
            boolean loggedIn,
            List<Widget> bankTiles,
            Widget bankCloseButton,
            Widget bankFirstSlot,
            GameObject bankChest,
            InvSummary inv,
            Set<Integer> inventoryTalismans,
            Set<GameObject> activeGuardians,
            Set<GroundObject> cellTiles,
            Set<GameObject> hugeGuardians,
            Set<GameObject> largeGuardians,
            NPC greatGuardian,
            NPC apprentice,
            GameObject unchargedCellTable,
            GameObject depositPool,
            GameObject catalyticEssencePile,
            GameObject elementalEssencePile,
            GameObject portal,
            GameObject returnPortal,
            GameObject workbench,
            TileObject barrier,
            TileObject rubbleTop,
            TileObject rubbleBottom,
            GameObject currentAltar,
            GameObject altarPortal,
            boolean isInMinigame,
            boolean hasAnyRunes,
            boolean hasAnyGuardianEssence,
            boolean isFirstPortal,
            Instant portalSpawnTime,
            Instant lastPortalDespawnTime,
            Instant nextGameStart,
            Instant gameStarted,
            int guardianEnergy,
            PlayerSnapshot player
    ) {
        this.seq = seq;
        this.ts = ts;
        this.loggedIn = loggedIn;
        this.inv = inv;

        this.bankTiles = bankTiles != null ? bankTiles.stream()
                .map(w -> TargetPointMapper.fromWidget(client, w, w.getId(), Objects.requireNonNull(w.getChild(1)).getText()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()) : null;

        this.bankCloseButton = bankCloseButton != null ? TargetPointMapper.fromWidget(client, bankCloseButton, bankCloseButton.getId(), "Close") : null;

        this.bankFirstSlot = bankFirstSlot != null ? TargetPointMapper.fromWidget(client, bankFirstSlot, bankFirstSlot.getId(), "First Slot") : null;

        this.bankChest = bankChest != null ? TargetPointMapper.fromGameObject(client, bankChest) : null;

        this.inventoryTalismans = inventoryTalismans.isEmpty() ? null : new ArrayList<>(inventoryTalismans);

        this.activeGuardians = activeGuardians.isEmpty() ? null : activeGuardians.stream()
                .map(go -> fromActiveGuardian(client, go))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        this.cellTiles = cellTiles.isEmpty() ? null : cellTiles.stream()
                .map(go -> TargetPointMapper.fromGroundObject(client, go))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        this.hugeGuardians = hugeGuardians.isEmpty() ? null : hugeGuardians.stream()
                .map(hg -> TargetPointMapper.fromGameObject(client, hg))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        this.largeGuardians = largeGuardians.isEmpty() ? null : largeGuardians.stream()
                .map(lg -> TargetPointMapper.fromGameObject(client, lg))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        this.greatGuardian        = TargetPointMapper.fromNPC(client, greatGuardian);
        this.apprentice           = TargetPointMapper.fromNPC(client, apprentice);
        this.unchargedCellTable   = TargetPointMapper.fromGameObject(client, unchargedCellTable);
        this.depositPool          = TargetPointMapper.fromGameObject(client, depositPool);
        this.catalyticEssencePile = TargetPointMapper.fromGameObject(client, catalyticEssencePile);
        this.elementalEssencePile = TargetPointMapper.fromGameObject(client, elementalEssencePile);
        this.portal               = TargetPointMapper.fromGameObject(client, portal);
        this.returnPortal         = TargetPointMapper.fromGameObject(client, returnPortal);
        this.workbench            = TargetPointMapper.fromGameObject(client, workbench);
        this.barrier              = TargetPointMapper.fromTileObject(client, barrier);
        this.rubbleTop            = TargetPointMapper.fromTileObject(client, rubbleTop);
        this.rubbleBottom         = TargetPointMapper.fromTileObject(client, rubbleBottom);
        this.currentAltar         = TargetPointMapper.fromGameObject(client, currentAltar);
        this.altarPortal          = TargetPointMapper.fromGameObject(client, altarPortal);

        this.canvasW = client.getCanvasWidth();
        this.canvasH = client.getCanvasHeight();

        // Canvas top-left in SCREEN pixels; null if window not yet realized/visible
        Integer screenX = null, screenY = null;
        try {
            Point p = client.getCanvas().getLocationOnScreen();
            if (p != null) {
                screenX = p.x;
                screenY = p.y;
            }
        } catch (IllegalComponentStateException ex) {
            // Window not showing yet (e.g., minimized or not realized) → leave nulls
        }
        this.canvasScreenX = screenX;
        this.canvasScreenY = screenY;

        this.isInMinigame = isInMinigame;
        this.hasAnyRunes = hasAnyRunes;
        this.hasAnyGuardianEssence = hasAnyGuardianEssence;
        this.isFirstPortal = isFirstPortal;

        this.portalSpawnTime = portalSpawnTime;
        this.lastPortalDespawnTime = lastPortalDespawnTime;
        this.nextGameStart = nextGameStart;
        this.gameStarted = gameStarted;

        this.guardianEnergy = guardianEnergy;

        this.player = player;
    }

    static TargetPoint fromActiveGuardian(Client client, GameObject obj) {
        if (obj == null) return null;
        int id = obj.getId();
        String name = safeObjectName(client, id);
        return mapCommon(
                client, id, name,
                obj.getWorldLocation(),
                obj.getCanvasTilePoly(), // good bbox for tile objects
                null                     // no canvas fallback needed for GOs
        );
    }
}
