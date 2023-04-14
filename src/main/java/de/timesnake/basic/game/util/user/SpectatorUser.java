/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.user;

import de.timesnake.basic.bukkit.core.main.BasicBukkit;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablist;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityEffect;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpectatorUser extends TeamUser {

    protected boolean glowingEnabled = false;
    protected boolean speedEnabled = false;
    protected boolean flyEnabled = true;

    public SpectatorUser(Player player) {
        super(player);

        this.hideSpectators();
    }

    @Override
    public void setStatus(Status.User status) {
        super.setStatus(status);
        GameServer.getSpectatorManager().updateSpectatorTools();
    }

    public void joinSpectator() {
        if (!this.getStatus().equals(Status.User.SPECTATOR)) {
            this.setStatus(Status.User.OUT_GAME);
        }

        this.setDefault();
        this.setCollitionWithEntites(false);
        this.setAllowFlight(true);
        this.setFlying(true);
        this.setInvulnerable(true);
        this.lockInventory();
        this.lockInventoryItemMove();

        this.glowingEnabled = true;
        this.speedEnabled = false;

        Server.runTaskLaterSynchrony(() -> {
            this.setAllowFlight(true);
            this.setFlying(true);
        }, 10, BasicBukkit.getPlugin());

        // show other spectators and hide for ingame users
        for (User user : Server.getUsers()) {
            if (Status.User.IN_GAME.equals(user.getStatus())
                    || Status.User.PRE_GAME.equals(user.getStatus())
                    || Status.User.ONLINE.equals(user.getStatus())) {

                user.hideUser(this);

                this.sendPacket(ExPacketPlayOutEntityEffect.wrap(user.getPlayer(),
                        ExPacketPlayOutEntityEffect.Effect.GLOWING, ((byte) 0), Integer.MAX_VALUE,
                        true, true, true));

            } else if (Status.User.OUT_GAME.equals(user.getStatus())
                    || Status.User.SPECTATOR.equals(user.getStatus())) {
                user.showUser(this);
            }
        }

        // remove from team chat
        if (this.getTeam() != null && this.getTeam().hasPrivateChat()) {
            Chat teamChat = Server.getChat(this.getTeam().getName());
            if (teamChat != null) {
                teamChat.removeWriter(this);
                teamChat.removeListener(this);
            }
        }

        if (GameServer.getGameTablist() instanceof TeamTablist tablist) {
            // set tablist team
            if (!(GameServer.getGame() instanceof TmpGame)
                    || !((TmpGame) GameServer.getGame()).hideTeams()
                    || this.getStatus().equals(Status.User.SPECTATOR)) {
                tablist.removeEntry(this);
                tablist.addRemainEntry(this);
            }
        }

        if (this.getTeam() == null) {
            this.teleportToSpectatorSpawn();
        }

        this.resetSideboard();
        this.setSideboard(GameServer.getSpectatorManager().getSpectatorSideboard());

        if (!GameServer.getSpectatorManager().loadTools()) {
            return;
        }

        // add to spectator chat
        Chat spectatorChat = GameServer.getSpectatorManager().getSpectatorChat();
        if (spectatorChat != null) {
            //remove from global chat
            Server.getGlobalChat().removeWriter(this);

            spectatorChat.addWriter(this);
            spectatorChat.addListener(this);
        }

        // set spec tools
        this.setSpectatorInventory();
    }

    public void setSpectatorInventory() {
        this.setItem(SpectatorManager.LEAVE_ITEM.cloneWithId());
        this.setItem(SpectatorManager.USER_INV.cloneWithId());
        this.setItem(SpectatorManager.SPEED.cloneWithId());
        this.setItem(SpectatorManager.GLOWING.cloneWithId().enchant());
        this.setItem(SpectatorManager.FLYING.cloneWithId().enchant());
    }

    public void openGameUserInventory() {
        this.openInventory(GameServer.getSpectatorManager().getGameUserInventory());
    }

    public void teleportToSpectatorSpawn() {
        this.teleport(GameServer.getSpectatorManager().getSpectatorSpawn());
    }

    public boolean hasGlowingEnabled() {
        return glowingEnabled;
    }

    public boolean hasSpeedEnabled() {
        return speedEnabled;
    }

    public void setGlowingEnabled(boolean glowingEnabled) {
        this.glowingEnabled = glowingEnabled;
    }

    public void setSpeedEnabled(boolean speedEnabled) {
        this.speedEnabled = speedEnabled;
        this.setFlySpeed(this.speedEnabled ? 0.4F : 0.2F);
        this.setWalkSpeed(this.speedEnabled ? 0.4F : 0.2F);
    }

    public boolean hasFlyEnabled() {
        return flyEnabled;
    }

    public void setFlyEnabled(boolean flyEnabled) {
        this.flyEnabled = flyEnabled;
        this.setAllowFlight(flyEnabled);
        this.setFlying(flyEnabled);
    }

    public void leaveSpectatorAndRejoin(@Nullable ExLocation location,
            @NotNull Status.User newStatus) {
        this.glowingEnabled = false;
        this.speedEnabled = false;

        if (this.getStatus().equals(Status.User.SPECTATOR)) {
            return;
        }

        this.setStatus(newStatus);

        GameServer.getGameTablist().addEntry(this);

        Chat spectatorChat = GameServer.getSpectatorManager().getSpectatorChat();
        if (spectatorChat != null) {
            spectatorChat.removeWriter(this);
            spectatorChat.removeListener(this);
            GameServer.getGlobalChat().addWriter(this);
        }

        this.rejoinGame(location, newStatus);
    }

    public void rejoinGame(@Nullable ExLocation location, @NotNull Status.User newStatus) {
        this.setStatus(newStatus);
        this.setDefault();

        this.setGameMode(GameServer.getSpectatorManager().getReJoinGameMode());

        if (location != null) {
            this.teleport(location);
        }

        this.hideSpectators();
        GameServer.getSpectatorManager().updateGlowingPlayers();

        this.setRejoinInventory();

        this.setSideboard(GameServer.getGameSideboard());
    }

    public void hideSpectators() {
        for (User user : Server.getUsers()) {
            user.showUser(this);

            if (user.getStatus().equals(Status.User.OUT_GAME)
                    || user.getStatus().equals(Status.User.SPECTATOR)) {
                this.hideUser(user);
            }
        }
    }

    public void setRejoinInventory() {

    }
}
