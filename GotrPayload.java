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
    private List<TargetPoint> activeGuardians;
    private List<TargetPoint> cellTiles;
    private TargetPoint hugeGuardian;
    private TargetPoint largeGuardian;
    private TargetPoint greatGuardian;
    private TargetPoint apprentice;
    private TargetPoint unchargedCellTable;
    private TargetPoint depositPool;
    private TargetPoint catalyticEssencePile;
    private TargetPoint elementalEssencePile;
    private TargetPoint portal;
    private TargetPoint returnPortal;
    private TargetPoint workbench;
    private TargetPoint barrier;
    private TargetPoint rubbleTop;
    private TargetPoint rubbleBottom;
    private TargetPoint currentAltar;
    private TargetPoint altarPortal;
    private Integer guardianEnergy;
    private PointBalance pointBalance;

    private boolean isInMinigame;
    private boolean hasAnyRunes;
    private boolean hasAnyGuardianEssence;
    private boolean hasAnyChargedCells;
    private boolean hasAnyStones;
    private boolean isFirstPortal;
    private boolean rewardReceived;

    private Instant portalSpawnTime;
    private Instant lastPortalDespawnTime;
    private Instant nextGameStart;
    private Instant gameStarted;

    private List<TargetPoint> bankTiles;
    private TargetPoint bankChest;
    private TargetPoint bankCloseButton;
    private TargetPoint bankFirstSlot;

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
            PlayerSnapshot player,
            PointBalance pointBalance
    ) {
        this.seq = seq;
        this.ts = ts;
        this.loggedIn = loggedIn;
        this.inv = inv;

        this.player = player;
        this.menu   = buildMenuSnapshot(client);
        this.dialog = buildDialogSnapshot(client);

        this.isInMinigame = isInMinigame;
        this.barrier              = TargetPointMapper.fromTileObject(client, barrier);

        this.hasAnyRunes = hasAnyRunes;
        this.hasAnyGuardianEssence = hasAnyGuardianEssence;
        this.hasAnyChargedCells = hasAnyChargedCells;
        this.hasAnyStones = hasAnyStones;

        this.portalSpawnTime = portalSpawnTime;
        this.lastPortalDespawnTime = lastPortalDespawnTime;
        this.nextGameStart = nextGameStart;
        this.gameStarted = gameStarted;
        this.guardianEnergy = guardianEnergy;
        this.pointBalance = pointBalance;


        this.activeGuardians = activeGuardians.isEmpty() ? null : activeGuardians.stream()
                .map(go -> TargetPointMapper.fromTileObject(client, go, TargetPointMapper.safeObjectName(client, go.getId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.cellTiles = cellTiles.isEmpty() ? null : cellTiles.stream()
                .map(go -> TargetPointMapper.fromTileObject(client, go))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.hugeGuardian         = TargetPointMapper.fromTileObject(client, hugeGuardian);
        this.largeGuardian        = TargetPointMapper.fromTileObject(client, largeGuardian);
        this.greatGuardian        = TargetPointMapper.fromNPC(client, greatGuardian);
        this.apprentice           = TargetPointMapper.fromNPC(client, apprentice);
        this.unchargedCellTable   = TargetPointMapper.fromTileObject(client, unchargedCellTable);
        this.depositPool          = TargetPointMapper.fromTileObject(client, depositPool);
        this.catalyticEssencePile = TargetPointMapper.fromTileObject(client, catalyticEssencePile);
        this.elementalEssencePile = TargetPointMapper.fromTileObject(client, elementalEssencePile);

        this.portal               = TargetPointMapper.fromTileObject(client, portal);
        this.returnPortal         = TargetPointMapper.fromTileObject(client, returnPortal);
        if (this.returnPortal.getDistToPlayer() > 5) {
            this.returnPortal = null;
            this.hugeGuardian = null;
        }

        this.workbench            = TargetPointMapper.fromTileObject(client, workbench);
        this.rubbleTop            = TargetPointMapper.fromTileObject(client, rubbleTop);
        this.rubbleBottom         = TargetPointMapper.fromTileObject(client, rubbleBottom);

        this.currentAltar         = TargetPointMapper.fromTileObject(client, currentAltar);
        this.altarPortal          = TargetPointMapper.fromTileObject(client, altarPortal);

        this.isFirstPortal = isFirstPortal;
        this.rewardReceived = rewardReceived;

        this.bankTiles = bankTiles != null ? bankTiles.stream()
                .map(w -> TargetPointMapper.fromWidget(client, w, Objects.requireNonNull(w.getChild(1)).getText()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()) : null;

        this.bankCloseButton = bankCloseButton != null ? TargetPointMapper.fromWidget(client, bankCloseButton) : null;
        this.bankFirstSlot = bankFirstSlot != null ? TargetPointMapper.fromWidget(client, bankFirstSlot) : null;
        this.bankChest = bankChest != null ? TargetPointMapper.fromTileObject(client, bankChest) : null;
    }
}
