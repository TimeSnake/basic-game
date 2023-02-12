/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.user.scoreboard.NameTagVisibility;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistablePlayer;
import de.timesnake.basic.bukkit.util.user.scoreboard.TagTablistableGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TagTablistableRemainTeam;
import de.timesnake.basic.game.util.user.TeamUser;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.ExTextColor;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public class Team implements TagTablistableGroup, TagTablistableRemainTeam {

    public static final int RANK_LENGTH = 6;

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

    public static TablistGroupType getTablistTeamType() {
        return TablistGroupType.GAME_TEAM;
    }

    private final DbTeam database;
    private final String name;
    private final Integer rank;
    private final String displayName;
    private final ExTextColor textColor;
    private final ChatColor chatColor;
    private final Color color;
    private final String tablistRank;
    private final float ratio;
    private final boolean privateChat;
    private Set<TeamUser> users = new HashSet<>();
    private Integer deaths = 0;
    private Integer kills = 0;

    public Team(String name, Integer rank, String displayName, ExTextColor textColor, Color color, float ratio, boolean privateChat) throws UnsupportedGroupRankException {
        this.database = null;
        this.name = name;
        this.rank = rank;
        this.displayName = displayName;
        this.textColor = textColor;
        this.chatColor = de.timesnake.basic.bukkit.util.chat.ChatColor.translateFromExTextColor(textColor);
        this.color = color;
        this.ratio = ratio;
        this.privateChat = privateChat;
        if (String.valueOf(this.rank).length() > RANK_LENGTH) {
            throw new UnsupportedGroupRankException(this.name, this.rank);
        } else {
            StringBuilder sb = new StringBuilder();

            sb.append("0".repeat(Math.max(0, RANK_LENGTH - String.valueOf(this.rank).length())));

            this.tablistRank = sb.append(this.rank).toString();
            Server.printText(Plugin.BUKKIT,
                    "Loaded team " + this.name + ": " + this.displayName + "; " + this.textColor.asHexString() + "; " + this.color.toString() + "; " + this.tablistRank + "; " + this.ratio, "Team");
        }
    }

    public Team(DbTeam team) throws UnsupportedGroupRankException {
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
        if (String.valueOf(this.rank).length() > 6) {
            throw new UnsupportedGroupRankException(this.name, this.rank);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("0".repeat(Math.max(0, 6 - String.valueOf(this.rank).length())));

        this.tablistRank = sb.append(this.rank).toString();
        Server.printText(Plugin.BUKKIT,
                "Loaded team " + this.name + ": " + this.displayName + "; " + this.textColor.toString() + "; " + this.color.toString() + "; " + this.tablistRank + "; " + this.ratio, "Team");
    }

    public String getName() {
        return this.name;
    }

    public Integer getRank() {
        return this.rank;
    }

    @Override
    public String getTablistRank() {
        return this.tablistRank;
    }

    @Override
    public String getTablistName() {
        return this.getDisplayName();
    }

    @Override
    public ChatColor getTablistChatColor() {
        return this.chatColor;
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

    @Override
    public ChatColor getTablistPrefixChatColor() {
        return this.chatColor;
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
    public NameTagVisibility isNameTagVisibleBy(TablistablePlayer player, TablistableGroup otherGroup) {
        return NameTagVisibility.ALWAYS;
    }

    @Override
    public NameTagVisibility isNameTagVisible(TablistablePlayer player) {
        return NameTagVisibility.ALWAYS;
    }
}
