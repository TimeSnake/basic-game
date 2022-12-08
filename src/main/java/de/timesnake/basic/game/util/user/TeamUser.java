/*
 * workspace.basic-game.main
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
        if (teamName != null && GameServer.getGame() instanceof TmpGame && ((TmpGame) GameServer.getGame()).hasTeam(teamName)) {
            this.setTeam(((TmpGame) GameServer.getGame()).getTeam(teamName));
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

        if (team != null && (!(GameServer.getGame() instanceof TmpGame) || !((TmpGame) GameServer.getGame()).hideTeams())) {
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
        return Team.getTablistTeamType().equals(type) ? this.getTeam() : super.getTablistGroup(type);
    }

    public void updateTeam() {
        this.setTeam(((TmpGame) GameServer.getGame()).getTeam(this.getDatabase().getTeamName()));
    }
}
