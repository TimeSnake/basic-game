/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.user;

import de.timesnake.basic.bukkit.core.main.BasicBukkit;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpectatorUser extends TeamUser {

  protected boolean glowingEnabled = true;
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

  @Override
  public TablistGroup getTablistGroup(TablistGroupType type) {
    if (type.equals(de.timesnake.basic.game.util.game.TablistGroupType.GAME_TEAM)
        && this.hasStatus(Status.User.SPECTATOR, Status.User.OUT_GAME)) {
      return null;
    }
    return super.getTablistGroup(type);
  }

  public void joinSpectator() {
    if (!this.hasStatus(Status.User.SPECTATOR)) {
      this.setStatus(Status.User.OUT_GAME);
      GameServer.getGameTablist().reloadEntry(this, true);
    }

    this.resetPlayerProperties();
    this.clearInventory();
    this.unlockAll();
    this.setCollitionWithEntites(false);
    this.setInvulnerable(true);
    this.lockInventory();
    this.lockInventoryItemMove();

    for (User user : Server.getUsers()) {
      if (user.hasStatus(Status.User.IN_GAME, Status.User.PRE_GAME, Status.User.ONLINE)) {
        user.hideUser(this);
      } else if (user.hasStatus(Status.User.OUT_GAME, Status.User.SPECTATOR)) {
        user.showUser(this);
      }
      this.showUser(user);
    }

    this.setGlowingEnabled(false);
    this.setGlowingEnabled(this.glowingEnabled);
    this.setAllowFlight(this.flyEnabled);
    this.setFlying(this.flyEnabled);
    this.setSpeedEnabled(this.speedEnabled);

    if (this.getTeam() != null && this.getTeam().hasPrivateChat()) {
      Chat teamChat = Server.getChat(this.getTeam().getName());
      if (teamChat != null) {
        teamChat.removeWriter(this);
        teamChat.removeListener(this);
      }
    }

    this.teleportToSpectatorSpawn();

    this.resetSideboard();
    this.setSideboard(GameServer.getSpectatorManager().getSpectatorSideboard());

    if (!GameServer.getSpectatorManager().loadSpectatorTools()) {
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
    this.setItem(SpectatorManager.SPEED.cloneWithId().enchant(this.speedEnabled));
    this.setItem(SpectatorManager.GLOWING.cloneWithId().enchant(this.glowingEnabled));
    this.setItem(SpectatorManager.FLYING.cloneWithId().enchant(this.flyEnabled));
  }

  public void openGameUserInventory() {
    this.openInventory(GameServer.getSpectatorManager().getGameUserInventory());
  }

  public void teleportToSpectatorSpawn() {
    this.teleport(GameServer.getSpectatorManager().getSpectatorSpawn());
  }

  public boolean hasGlowingEnabled() {
    return glowingEnabled && this.hasStatus(Status.User.OUT_GAME, Status.User.SPECTATOR);
  }

  public boolean hasSpeedEnabled() {
    return speedEnabled;
  }

  public void setGlowingEnabled(boolean glowingEnabled) {
    this.glowingEnabled = glowingEnabled;
    GameServer.getSpectatorManager().sendGlowUpdateToUser(this);
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

  public void leaveSpectatorAndRejoin(@Nullable ExLocation location, @NotNull Status.User newStatus) {
    this.glowingEnabled = false;
    this.speedEnabled = false;

    if (this.getStatus().equals(Status.User.SPECTATOR)) {
      return;
    }

    this.setStatus(newStatus);

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
    this.resetPlayerProperties();
    this.unlockAll();
    this.clearInventory();
    this.setCollitionWithEntites(true);

    this.setGameMode(GameServer.getSpectatorManager().getReJoinGameMode());

    if (location != null) {
      this.teleport(location);
    }

    this.hideSpectators();
    Server.getUsers().forEach(u -> u.showUser(this));
    GameServer.getSpectatorManager().updateGlowingPlayers();

    GameServer.getGameTablist().reloadEntry(this, true);

    this.setRejoinInventory();

    this.setSideboard(GameServer.getGameSideboard());
  }

  public void hideSpectators() {
    Server.runTaskSynchrony(() -> {
      if (!this.hasStatus(Status.User.OUT_GAME, Status.User.SPECTATOR)) {
        for (User user : Server.getUsers()) {
          if (user.hasStatus(Status.User.OUT_GAME, Status.User.SPECTATOR)) {
            this.hideUser(user);
          }
        }
      }
    }, BasicBukkit.getPlugin());
  }

  public void setRejoinInventory() {

  }
}
