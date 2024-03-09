/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.game.util.user.TeamUser;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.ExTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.HashSet;
import java.util.Set;

public class Team implements TablistGroup {

  public static Color parseColor(String colorName) {
    return switch (colorName.toUpperCase()) {
      case "AQUA" -> Color.AQUA;
      case "BLACK" -> Color.BLACK;
      case "BLUE" -> Color.BLUE;
      case "FUCHSIA" -> Color.FUCHSIA;
      case "GRAY" -> Color.GRAY;
      case "GREEN" -> Color.GREEN;
      case "LIME" -> Color.LIME;
      case "MAROON" -> Color.MAROON;
      case "NAVY" -> Color.NAVY;
      case "OLIVE" -> Color.OLIVE;
      case "ORANGE" -> Color.ORANGE;
      case "PURPLE" -> Color.PURPLE;
      case "RED" -> Color.RED;
      case "SILVER" -> Color.SILVER;
      case "TEAL" -> Color.TEAL;
      case "YELLOW" -> Color.YELLOW;
      default -> Color.WHITE;
    };
  }

  protected final Logger logger = LogManager.getLogger("team");

  private final DbTeam database;
  private final String name;
  private final Integer rank;
  private final String displayName;
  private final ExTextColor textColor;
  private final ChatColor chatColor;
  private final Color color;
  private final float ratio;
  private final boolean privateChat;
  private final Integer minSize;
  private Set<TeamUser> users = new HashSet<>();
  private Integer deaths = 0;
  private Integer kills = 0;

  public Team(String name, Integer rank, String displayName, ExTextColor textColor, Color color, float ratio,
              boolean privateChat, int minSize) {
    this.database = null;
    this.name = name;
    this.rank = rank;
    this.displayName = displayName;
    this.textColor = textColor;
    this.chatColor = de.timesnake.basic.bukkit.util.chat.ChatColor.translateFromExTextColor(textColor);
    this.color = color;
    this.ratio = ratio;
    this.privateChat = privateChat;
    this.minSize = minSize;

    this.logger.info("Loaded team '{}': {}", this.name, this);
  }

  public Team(DbTeam team) {
    this.database = team;

    team = team.toLocal();

    this.name = team.getName();
    this.rank = team.getRank();
    this.displayName = team.getPrefix();
    this.textColor = team.getChatColor() != null ? team.getChatColor() : ExTextColor.WHITE;
    this.chatColor = de.timesnake.basic.bukkit.util.chat.ChatColor.translateFromExTextColor(this.textColor);
    this.color = parseColor(team.getColorName());
    this.ratio = team.getRatio();
    this.privateChat = team.hasPrivateChat();
    this.minSize = team.getMinSize();

    this.logger.info("Loaded team '{}': {}", this.name, this);
  }

  public String getName() {
    return this.name;
  }

  public Integer getRank() {
    return this.rank;
  }

  @Override
  public int getTablistRank() {
    return this.rank;
  }

  @Override
  public String getTablistName() {
    return this.getDisplayName();
  }

  @Override
  public ExTextColor getTablistChatColor() {
    return this.textColor;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  @Override
  public String getTablistPrefix() {
    return "";
  }

  public ExTextColor getTextColor() {
    return this.textColor;
  }

  public String getTDColor() {
    return "§" + this.textColor.getToken();
  }

  @Override
  public ExTextColor getTablistPrefixChatColor() {
    return this.textColor;
  }

  @Deprecated
  public ChatColor getChatColor() {
    return chatColor;
  }

  public Color getColor() {
    return this.color;
  }

  public float getRatio() {
    return this.ratio;
  }

  public boolean hasPrivateChat() {
    return privateChat;
  }

  public int getMinSize() {
    return minSize;
  }

  public boolean hasMinSize() {
    return minSize != null;
  }

  public void addDeath() {
    this.deaths++;
  }

  public Integer getDeaths() {
    return this.deaths;
  }

  public void setDeaths(Integer deaths) {
    this.deaths = deaths;
  }

  public void addKill() {
    this.kills++;
  }

  public Integer getKills() {
    return this.kills;
  }

  public void setKills(Integer kills) {
    this.kills = kills;
  }

  public Set<TeamUser> getUsers() {
    return this.users;
  }

  public void setUsers(Set<TeamUser> users) {
    this.users = users;
  }

  public Set<TeamUser> getInGameUsers() {
    Set<TeamUser> users = new HashSet<>();
    for (TeamUser user : this.users) {
      if (user.getStatus().equals(Status.User.IN_GAME)) {
        users.add(user);
      }
    }
    return users;
  }

  public boolean addUser(TeamUser user) {
    if (user.getTeam().equals(this)) {
      if (this.users.contains(user)) {
        return false;
      } else {
        this.users.add(user);
        return true;
      }
    } else {
      return false;
    }
  }

  public boolean removeUser(TeamUser user) {
    return this.users.remove(user);
  }

  public boolean isEmpty() {
    return this.users.isEmpty();
  }

  public TablistGroupType getTeamType() {
    return TablistGroupType.GAME_TEAM;
  }

  public DbTeam getDatabase() {
    return database;
  }

  @Override
  public String toString() {
    return "Team{" +
        "name='" + name + '\'' +
        ", rank=" + rank +
        ", displayName='" + displayName + '\'' +
        ", textColor=" + textColor +
        ", color=" + color +
        ", ratio=" + ratio +
        ", privateChat=" + privateChat +
        ", minSize=" + minSize +
        '}';
  }
}
