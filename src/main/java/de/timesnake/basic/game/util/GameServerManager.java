package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.core.UserManager;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import org.bukkit.entity.Player;

public abstract class GameServerManager extends ServerManager {

    public static GameServerManager getInstance() {
        return (GameServerManager) ServerManager.getInstance();
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
        return new Game(dbGame, loadWorlds);
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
