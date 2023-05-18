/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;

public class TablistGroupType extends
    de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType {

  public static final TablistGroupType GAME_TEAM = new TablistGroupType(Team.class);

  public TablistGroupType(Class<? extends TablistableGroup> groupClass) {
    super(groupClass);
  }
}
