/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.game.util.user;

import de.timesnake.library.basic.util.LogHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

    public static final Plugin LOUNGE = new Plugin("Lounge", "BLB", LogHelper.getLogger("Lounge", Level.INFO));
    public static final Plugin GAME = new Plugin("Game", "BLG", LogHelper.getLogger("Game", Level.INFO));

    protected Plugin(String name, String code, Logger logger) {
        super(name, code, logger);
    }
}
