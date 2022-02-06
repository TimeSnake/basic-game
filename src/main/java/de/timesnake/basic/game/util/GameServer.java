package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;

public class GameServer extends Server {

    private static final GameServerManager server = GameServerManager.getInstance();

    public static Game getGame() {
        return server.getGame();
    }

}
