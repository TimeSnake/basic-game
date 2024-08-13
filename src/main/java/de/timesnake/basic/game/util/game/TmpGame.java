/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.library.basic.util.Availability;
import de.timesnake.library.basic.util.DiscordChannelType;
import de.timesnake.library.game.TmpGameInfo;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TmpGame extends Game<TmpGameInfo> {

  protected final java.util.Map<String, Team> teamsByName = new HashMap<>();
  protected final TreeSet<Team> teamsSortedByRank = new TreeSet<>(Comparator.comparingInt(Team::getRank));

  public TmpGame(DbTmpGame database, boolean loadWorlds) {
    super(database, new TmpGameInfo(database.getInfo()), loadWorlds);

    for (DbTeam dbTeam : database.getTeams()) {
      Team team = this.loadTeam(dbTeam);
      if (team != null) {
        this.teamsByName.put(team.getName(), team);
        this.teamsSortedByRank.add(team);
      }
    }
  }

  public boolean hasTeam(String name) {
    return this.teamsByName.containsKey(name);
  }

  public Team getTeam(String team) {
    return this.teamsByName.get(team);
  }

  public Collection<? extends Team> getTeams() {
    return new ArrayList<>(this.teamsByName.values());
  }

  public java.util.Map<String, ? extends Team> getTeamsByName() {
    return teamsByName;
  }

  public TreeSet<Team> getTeamsSortedByRank() {
    return teamsSortedByRank;
  }

  public List<Team> getTeamsSortedByRank(int amount) {
    return new ArrayList<>(teamsSortedByRank).stream().limit(amount).toList();
  }


  public Integer getAutoStartPlayerNumber() {
    return getInfo().getAutoStartPlayerNumber();
  }

  public Integer getMinPlayerNumber() {
    return getInfo().getMinPlayerNumber();
  }

  public List<Integer> getTeamSizes() {
    return getInfo().getTeamSizes();
  }

  public Availability getTeamMerge() {
    return getInfo().getTeamMerge();
  }

  public boolean isEqualTimeSizeRequired() {
    return getInfo().isEqualTimeSizeRequired();
  }

  public boolean showSelectedKits() {
    return getInfo().showSelectedKits();
  }

  public boolean hideTeams() {
    return getInfo().hideTeams();
  }

  public DiscordChannelType getDiscordType() {
    return getInfo().getDiscordType();
  }

  public List<String> getDescription() {
    return getInfo().getDescription();
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

  public String getTexturePackHash() {
    return getInfo().getTexturePackHash();
  }

  public Boolean hasTexturePack() {
    return getInfo().hasTexturePack();
  }
}
