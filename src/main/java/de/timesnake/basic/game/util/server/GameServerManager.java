/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.server;

import de.timesnake.basic.bukkit.core.user.scoreboard.tablist.Tablist2;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.game.util.game.NonTmpGame;
import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.user.SpectatorManager;
import de.timesnake.basic.game.util.user.TeamUser;
import de.timesnake.database.core.game.DbNonTmpGame;
import de.timesnake.database.core.game.DbTmpGame;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import org.bukkit.entity.Player;

public abstract class GameServerManager<Game extends de.timesnake.basic.game.util.game.Game<?>> extends
    ServerManager {

  public static GameServerManager<?> getInstance() {
    return (GameServerManager<?>) ServerManager.getInstance();
  }

  protected Game game;

  private SpectatorManager spectatorManager;

  public final void onGameEnable() {

    DbGame dbGame = this.getDbGame();
    if (dbGame != null && dbGame.exists()) {
      this.game = this.loadGame(dbGame, false);
    } else {
      this.logger.warn("Could not load game");
    }

    this.spectatorManager = this.initSpectatorManager();
  }

  protected SpectatorManager initSpectatorManager() {
    return null;
  }

  protected Game loadGame(DbGame dbGame, boolean loadWorlds) {
    if (dbGame instanceof DbNonTmpGame) {
      return (Game) new NonTmpGame(((DbNonTmpGame) dbGame), loadWorlds);
    } else if (dbGame instanceof DbTmpGame) {
      return (Game) new TmpGame(((DbTmpGame) dbGame), loadWorlds);
    }
    return null;
  }

  public SpectatorManager getSpectatorManager() {
    return spectatorManager;
  }

  public Game getGame() {
    return this.game;
  }

  public final DbGame getDbGame() {
    String task = Server.getTask();
    if (task == null) {
      this.logger.warn("Task is null");
      return null;
    } else {
      return Database.getGames().getGame(task);
    }
  }

  @Override
  public User loadUser(Player player) {
    return new TeamUser(player);
  }

  public abstract Sideboard getGameSideboard();

  public abstract Tablist2 getGameTablist();

}
