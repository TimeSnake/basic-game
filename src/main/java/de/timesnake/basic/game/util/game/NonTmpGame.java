/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.database.util.game.DbNonTmpGame;
import de.timesnake.library.basic.util.Availability;
import de.timesnake.library.game.NonTmpGameInfo;
import org.bukkit.inventory.ItemStack;

public class NonTmpGame extends Game<NonTmpGameInfo> {

  public NonTmpGame(DbNonTmpGame database, boolean loadWorlds) {
    super(database, new NonTmpGameInfo(database.getInfo()), loadWorlds);
  }

  public boolean isCreationRequestable() {
    return getInfo().isCreationRequestable();
  }

  public boolean isOwnable() {
    return getInfo().isOwnable();
  }

  public String getName() {
    return getInfo().getName();
  }

  public String getDisplayName() {
    return getInfo().getDisplayName();
  }

  public Integer getMaxPlayers() {
    return getInfo().getMaxPlayers();
  }

  public String getHeadLine() {
    return getInfo().getHeadLine();
  }

  public ItemStack getItem() {
    return getInfo().getItem();
  }

  public Integer getSlot() {
    return getInfo().getSlot();
  }

  public boolean isEnabled() {
    return getInfo().isEnabled();
  }

  public Availability getMapAvailability() {
    return getInfo().getMapAvailability();
  }

  public Availability getKitAvailability() {
    return getInfo().getKitAvailability();
  }

  public String getTexturePackLink() {
    return getInfo().getTexturePackLink();
  }

  public Boolean hasTexturePack() {
    return getInfo().hasTexturePack();
  }
}