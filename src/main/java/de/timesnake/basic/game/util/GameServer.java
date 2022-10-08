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

public class GameServer extends Server {

    public static final String DISCORD_SPECTATOR = "Spectator";
    public static final String DISCORD_LOUNGE = "Lounge";

    public static Game<?> getGame() {
        return server.getGame();
    }

    private static final GameServerManager<?> server = GameServerManager.getInstance();

}
