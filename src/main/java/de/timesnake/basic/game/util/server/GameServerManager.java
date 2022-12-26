/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.game.util.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.game.NonTmpGame;
import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.user.SpectatorManager;
import de.timesnake.basic.game.util.user.TeamUser;
import de.timesnake.database.core.game.DbNonTmpGame;
import de.timesnake.database.core.game.DbTmpGame;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import org.bukkit.entity.Player;

public abstract class GameServerManager<Game extends de.timesnake.basic.game.util.game.Game<?>> extends ServerManager {

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
            Server.printWarning(Plugin.BUKKIT, "Can not load game");
        }

        this.spectatorManager = this.loadSpectatorManager();
    }

    protected SpectatorManager loadSpectatorManager() {
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
            Server.printWarning(Plugin.BUKKIT, "Task is null");
            return null;
        } else {
            return Database.getGames().getGame(task);
        }
    }

    @Override
    public User loadUser(Player player) {
        return new TeamUser(player);
    }

}
