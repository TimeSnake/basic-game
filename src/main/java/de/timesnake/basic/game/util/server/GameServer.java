/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.server;

import de.timesnake.basic.bukkit.core.user.scoreboard.tablist.Tablist2;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.game.util.game.Game;
import de.timesnake.basic.game.util.user.SpectatorManager;
import org.bukkit.Sound;

public class GameServer extends Server {

  public static final String DISCORD_SPECTATOR = "Spectator";
  public static final String DISCORD_LOUNGE = "Lounge";

  public static final Sound KILL_SOUND = Sound.ENTITY_PLAYER_LEVELUP;

  public static final Sound END_SOUND = Sound.BLOCK_BEACON_ACTIVATE;

  public static Game<?> getGame() {
    return server.getGame();
  }

  public static SpectatorManager getSpectatorManager() {
    return server.getSpectatorManager();
  }

  public static Sideboard getGameSideboard() {
    return server.getGameSideboard();
  }

  public static Tablist2 getGameTablist() {
    return server.getGameTablist();
  }

  private static final GameServerManager<?> server = GameServerManager.getInstance();

}
