package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.permission.Group;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TeamUser extends User {

    private Team team;

    public TeamUser(Player player) {
        super(player);
        String teamName = this.getDatabase().getTeamName();
        if (teamName != null && GameServer.getGame() != null && GameServer.getGame().hasTeam(teamName)) {
            this.setTeam(GameServer.getGame().getTeam(teamName));

        }

        this.updateChatName();
    }

    @Override
    public void onQuit() {
        if (this.team != null) {
            this.team.removeUser(this);
        }
    }

    @Override
    protected void updateChatName() {
        Group group = this.getGroup();
        Team team = this.getTeam();
        ChatColor groupPrefixColor;

        if (this.getNick() == null) {
            String prefix = "";
            String suffix = "";
            if (this.getPrefix() != null) {
                prefix = this.getPrefix();
            }

            if (this.getSuffix() != null) {
                suffix = this.getSuffix();
            }

            groupPrefixColor = group.getPrefixColor();

            if (team == null) {
                super.chatName = groupPrefixColor.toString() + group.getPrefix() + "§r" + ChatColor.translateAlternateColorCodes('&', prefix) + "§r" + this.getPlayer().getName() + "§r" + ChatColor.translateAlternateColorCodes('&', suffix);
            } else {
                super.chatName = groupPrefixColor.toString() + group.getPrefix() + "§r" + ChatColor.translateAlternateColorCodes('&', prefix) + "§r" + team.getChatColor().toString() + this.getPlayer().getName() + "§r" + ChatColor.translateAlternateColorCodes('&', suffix);
            }
        } else {
            group = Server.getMemberGroup();
            groupPrefixColor = group.getPrefixColor();

            if (team == null) {
                super.chatName = "§r" + groupPrefixColor.toString() + group.getPrefix() + "§r" + ChatColor.translateAlternateColorCodes('&', this.getNick());
            } else {
                super.chatName = "§r" + groupPrefixColor.toString() + group.getPrefix() + "§r" + team.getChatColor().toString() + ChatColor.translateAlternateColorCodes('&', this.getNick());
            }
        }

    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team, boolean updateDatabase) {
        if (this.team == null || !this.team.equals(team)) {
            if (this.team != null) {
                this.team.removeUser(this);
            }

            this.team = team;
            if (this.team != null) {
                this.team.addUser(this);
            }

            if (updateDatabase) {
                if (this.team != null) {
                    this.getDatabase().setTeam(this.team.getName());
                } else {
                    this.getDatabase().setTeam(null);
                }
            }

            this.updateChatName();
        }
    }

    public void setTeam(Team team) {
        this.setTeam(team, true);
    }

    public boolean isTeamMate(TeamUser user) {
        return this.team != null && user.getTeam() != null && this.team.equals(user.getTeam());
    }

    @Override
    public TablistableGroup getTablistGroup(TablistGroupType type) {
        return Team.getTablistTeamType().equals(type) ? this.getTeam() : super.getTablistGroup(type);
    }

    public void updateTeam() {
        this.setTeam(GameServer.getGame().getTeam(this.getDatabase().getTeamName()));
    }
}
