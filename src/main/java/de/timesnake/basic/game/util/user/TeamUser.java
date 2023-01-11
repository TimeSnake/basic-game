/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.user;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.server.GameServer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamUser extends User {

    private Team team;

    public TeamUser(Player player) {
        super(player);
        String teamName = this.getDatabase().getTeamName();
        if (teamName != null && GameServer.getGame() instanceof TmpGame
                && ((TmpGame) GameServer.getGame()).hasTeam(teamName)) {
            this.setTeam(((TmpGame) GameServer.getGame()).getTeam(teamName), false);
            this.updateChatName();
        }
    }

    @Override
    public void onQuit() {
        if (this.team != null) {
            this.team.removeUser(this);
        }
    }

    @Override
    protected @NotNull Component getPlayerChatName() {
        Team team = this.getTeam();

        if (team != null && (!(GameServer.getGame() instanceof TmpGame)
                || !((TmpGame) GameServer.getGame()).hideTeams())) {
            return Component.text(this.getPlayer().getName(), team.getTextColor());
        }
        return super.getPlayerChatName();
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.setTeam(team, true);
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

    public boolean isTeamMate(TeamUser user) {
        return this.team != null && user.getTeam() != null && this.team.equals(user.getTeam());
    }

    @Override
    public TablistableGroup getTablistGroup(TablistGroupType type) {
        return Team.getTablistTeamType().equals(type) ? this.getTeam()
                : super.getTablistGroup(type);
    }

    public void updateTeam() {
        this.setTeam(((TmpGame) GameServer.getGame()).getTeam(this.getDatabase().getTeamName()));
    }
}
