package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.exceptions.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableRemainTeam;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.library.basic.util.Status;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.HashSet;
import java.util.Set;

public class Team implements TablistableGroup, TablistableRemainTeam {

    public static final int RANK_LENGTH = 6;

    private final DbTeam database;

    private final String name;
    private final Integer rank;
    private final String displayName;
    private final ChatColor chatColor;
    private final Color color;
    private final String tablistRank;
    private final float ratio;
    private Set<TeamUser> users = new HashSet<>();
    private Integer deaths = 0;
    private Integer kills = 0;

    public Team(String name, Integer rank, String displayName, ChatColor chatColor, Color color, float ratio) throws UnsupportedGroupRankException {
        this.database = null;
        this.name = name;
        this.rank = rank;
        this.displayName = displayName;
        this.chatColor = chatColor;
        this.color = color;
        this.ratio = ratio;
        if (String.valueOf(this.rank).length() > RANK_LENGTH) {
            throw new UnsupportedGroupRankException(this.name, this.rank);
        } else {
            StringBuilder sb = new StringBuilder();

            sb.append("0".repeat(Math.max(0, RANK_LENGTH - String.valueOf(this.rank).length())));

            this.tablistRank = sb.append(this.rank).toString();
            Server.printText(Plugin.BUKKIT, "Loaded team " + this.name + ": " + this.displayName + "; " + this.chatColor.name() + "; " + this.color.toString() + "; " + this.tablistRank + "; " + this.ratio, "Team");
        }
    }

    public Team(DbTeam team) throws UnsupportedGroupRankException {
        this.database = team;

        team = team.toLocal();

        this.name = team.getName();
        this.rank = team.getRank();
        this.displayName = team.getPrefix();

        ChatColor chatColor = ChatColor.WHITE;
        try {
            chatColor = ChatColor.valueOf(team.getChatColorName().toUpperCase());
        } catch (IllegalArgumentException e) {
            Server.printWarning(Plugin.BUKKIT, "Can not load color of team " + this.name);
        }
        this.chatColor = chatColor;

        this.color = parseColor(team.getColorName());
        this.ratio = team.getRatio();
        if (String.valueOf(this.rank).length() > 6) {
            throw new UnsupportedGroupRankException(this.name, this.rank);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("0".repeat(Math.max(0, 6 - String.valueOf(this.rank).length())));

        this.tablistRank = sb.append(this.rank).toString();
        Server.printText(Plugin.BUKKIT, "Loaded team " + this.name + ": " + this.displayName + "; " + this.chatColor.name() + "; " + this.color.toString() + "; " + this.tablistRank + "; " + this.ratio, "Team");
    }

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

    public String getName() {
        return this.name;
    }

    public Integer getRank() {
        return this.rank;
    }

    public String getTablistRank() {
        return this.tablistRank;
    }

    public String getTablistName() {
        return this.getDisplayName();
    }

    public ChatColor getTablistChatColor() {
        return this.getChatColor();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getTablistPrefix() {
        return "";
    }

    public ChatColor getChatColor() {
        return this.chatColor;
    }

    public ChatColor getTablistPrefixChatColor() {
        return this.getChatColor();
    }

    public Color getColor() {
        return this.color;
    }

    public float getRatio() {
        return this.ratio;
    }

    public void addDeath() {
        this.deaths++;
    }

    public void setDeaths(Integer deaths) {
        this.deaths = deaths;
    }

    public Integer getDeaths() {
        return this.deaths;
    }

    public void addKill() {
        this.kills++;
    }

    public void setKills(Integer kills) {
        this.kills = kills;
    }

    public Integer getKills() {
        return this.kills;
    }

    public Set<TeamUser> getUsers() {
        return this.users;
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

    public void setUsers(Set<TeamUser> users) {
        this.users = users;
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

    public static TablistGroupType getTablistTeamType() {
        return TablistGroupType.GAME_TEAM;
    }

    public DbTeam getDatabase() {
        return database;
    }
}
