/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.game.util.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.game.util.game.Game;
import de.timesnake.basic.game.util.user.SpectatorManager;

public class GameServer extends Server {

    public static final String DISCORD_SPECTATOR = "Spectator";
    public static final String DISCORD_LOUNGE = "Lounge";

    public static Game<?> getGame() {
        return server.getGame();
    }

    public static SpectatorManager getSpectatorManager() {
        return server.getSpectatorManager();
    }

    private static final GameServerManager<?> server = GameServerManager.getInstance();

}
