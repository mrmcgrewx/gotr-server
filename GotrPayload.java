package net.runelite.client.plugins.gotr;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.server.*;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.server.DialogSnapshot.buildDialogSnapshot;
import static net.runelite.client.server.MenuSnapshot.buildMenuSnapshot;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class GotrPayload extends Payload {
    private final List<TargetPoint> activeGuardians;
    private final List<TargetPoint> cellTiles;
    private final TargetPoint hugeGuardian;
    private final TargetPoint largeGuardian;
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
    private final Integer guardianEnergy;

    private final boolean isInMinigame;
    private final boolean hasAnyRunes;
    private final boolean hasAnyGuardianEssence;
    private final boolean hasAnyChargedCells;
    private final boolean hasAnyStones;
    private final boolean isFirstPortal;
    private final boolean rewardReceived;

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
            boolean isInMinigame,
            List<Widget> bankTiles,
            Widget bankCloseButton,
            Widget bankFirstSlot,
            GameObject bankChest,
            InvSummary inv,
            Set<GameObject> activeGuardians,
            Set<GroundObject> cellTiles,
            GameObject hugeGuardian,
            GameObject largeGuardian,
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

            boolean hasAnyRunes,
            boolean hasAnyGuardianEssence,
            boolean hasAnyChargedCells,
            boolean hasAnyStones,
            boolean isFirstPortal,
            boolean rewardReceived,
            Instant portalSpawnTime,
            Instant lastPortalDespawnTime,
            Instant nextGameStart,
            Instant gameStarted,
            Integer guardianEnergy,
            PlayerSnapshot player
    ) {
        TargetPoint rp;
        TargetPoint hg;

        this.seq = seq;
        this.ts = ts;
        this.loggedIn = loggedIn;
        this.inv = inv;

        this.bankTiles = bankTiles != null ? bankTiles.stream()
                .map(w -> TargetPointMapper.fromWidget(client, w, Objects.requireNonNull(w.getChild(1)).getText()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()) : null;

        this.bankCloseButton = bankCloseButton != null ? TargetPointMapper.fromWidget(client, bankCloseButton) : null;
        this.bankFirstSlot = bankFirstSlot != null ? TargetPointMapper.fromWidget(client, bankFirstSlot) : null;
        this.bankChest = bankChest != null ? TargetPointMapper.fromTileObject(client, bankChest) : null;

        this.isInMinigame = isInMinigame;

        if (isInMinigame) {
            this.activeGuardians = activeGuardians.isEmpty() ? null : activeGuardians.stream()
                    .map(go -> TargetPointMapper.fromTileObject(client, go, TargetPointMapper.safeObjectName(client, go.getId())))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            this.cellTiles = cellTiles.isEmpty() ? null : cellTiles.stream()
                    .map(go -> TargetPointMapper.fromTileObject(client, go))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            hg                        = TargetPointMapper.fromTileObject(client, hugeGuardian);
            this.largeGuardian        = TargetPointMapper.fromTileObject(client, largeGuardian);
            this.greatGuardian        = TargetPointMapper.fromNPC(client, greatGuardian);
            this.apprentice           = TargetPointMapper.fromNPC(client, apprentice);
            this.unchargedCellTable   = TargetPointMapper.fromTileObject(client, unchargedCellTable);
            this.depositPool          = TargetPointMapper.fromTileObject(client, depositPool);
            this.catalyticEssencePile = TargetPointMapper.fromTileObject(client, catalyticEssencePile);
            this.elementalEssencePile = TargetPointMapper.fromTileObject(client, elementalEssencePile);
            this.portal               = TargetPointMapper.fromTileObject(client, portal);
            rp = TargetPointMapper.fromTileObject(client, returnPortal);
            if (rp.getDistToPlayer() > 5) {
                rp = null;
                hg = null;
            }
            this.workbench            = TargetPointMapper.fromTileObject(client, workbench);
            this.rubbleTop            = TargetPointMapper.fromTileObject(client, rubbleTop);
            this.rubbleBottom         = TargetPointMapper.fromTileObject(client, rubbleBottom);

        } else {
            this.activeGuardians      = null;
            this.cellTiles            = null;
            hg                        = null;
            this.largeGuardian        = null;
            this.greatGuardian        = null;
            this.apprentice           = null;
            this.unchargedCellTable   = null;
            this.depositPool          = null;
            this.catalyticEssencePile = null;
            this.elementalEssencePile = null;
            this.portal               = null;
            rp                        = null;
            this.workbench            = null;
            this.rubbleTop            = null;
            this.rubbleBottom         = null;
        }

        this.hugeGuardian         = hg;
        this.returnPortal         = rp;
        this.barrier              = TargetPointMapper.fromTileObject(client, barrier);
        this.currentAltar         = TargetPointMapper.fromTileObject(client, currentAltar);
        this.altarPortal          = TargetPointMapper.fromTileObject(client, altarPortal);

        this.hasAnyRunes = hasAnyRunes;
        this.hasAnyGuardianEssence = hasAnyGuardianEssence;
        this.hasAnyChargedCells = hasAnyChargedCells;
        this.hasAnyStones = hasAnyStones;
        this.isFirstPortal = isFirstPortal;
        this.rewardReceived = rewardReceived;

        this.portalSpawnTime = portalSpawnTime;
        this.lastPortalDespawnTime = lastPortalDespawnTime;
        this.nextGameStart = nextGameStart;
        this.gameStarted = gameStarted;
        this.guardianEnergy = guardianEnergy;

        this.player = player;
        this.menu   = buildMenuSnapshot(client);
        this.dialog = buildDialogSnapshot(client);
    }
}
