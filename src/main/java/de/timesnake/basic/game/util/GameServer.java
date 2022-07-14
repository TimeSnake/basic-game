package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;

public class GameServer extends Server {

    public static final String DISCORD_SPECTATOR = "Spectator";
    public static final String DISCORD_LOUNGE = "Lounge";

    public static Game<?> getGame() {
        return server.getGame();
    }

    private static final GameServerManager<?> server = GameServerManager.getInstance();

}
