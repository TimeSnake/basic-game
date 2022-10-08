/*
 * basic-game.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.core.UserManager;
import de.timesnake.database.core.game.DbNonTmpGame;
import de.timesnake.database.core.game.DbTmpGame;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import org.bukkit.entity.Player;

public abstract class GameServerManager<Game extends de.timesnake.basic.game.util.Game<?>> extends ServerManager {

    public static GameServerManager<?> getInstance() {
        return (GameServerManager<?>) ServerManager.getInstance();
    }

    protected Game game;

    private UserManager userManager;

    public final void onGameEnable() {
        this.userManager = new UserManager();

        DbGame dbGame = this.getDbGame();
        if (dbGame != null && dbGame.exists()) {
            this.game = this.loadGame(dbGame, false);
        } else {
            Server.printError(Plugin.BUKKIT, "Can not load game");
        }
    }

    protected Game loadGame(DbGame dbGame, boolean loadWorlds) {
        if (dbGame instanceof DbNonTmpGame) {
            return (Game) new NonTmpGame(((DbNonTmpGame) dbGame), loadWorlds);
        } else if (dbGame instanceof DbTmpGame) {
            return (Game) new TmpGame(((DbTmpGame) dbGame), loadWorlds);
        }
        return null;
    }

    public Game getGame() {
        return this.game;
    }

    public final DbGame getDbGame() {
        String task = Server.getTask();
        if (task == null) {
            Server.printError(Plugin.BUKKIT, "Task is null");
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
