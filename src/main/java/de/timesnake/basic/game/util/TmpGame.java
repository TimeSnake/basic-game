/*
 * basic-game.main
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

package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.exceptions.UnsupportedGroupRankException;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.game.TmpGameInfo;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TmpGame extends Game<TmpGameInfo> {

    protected final java.util.Map<String, Team> teamsByName = new HashMap<>();
    protected final LinkedHashMap<Integer, Team> teamsSortedByRank = new LinkedHashMap<>();

    public TmpGame(DbTmpGame database, boolean loadWorlds) {
        super(database, new TmpGameInfo(database.getInfo()), loadWorlds);

        for (DbTeam dbTeam : database.getTeams()) {
            Team team;
            try {
                team = this.loadTeam(dbTeam);
            } catch (UnsupportedGroupRankException e) {
                Server.printError(Plugin.BUKKIT, de.timesnake.basic.bukkit.util.chat.ChatColor.WARNING + e.getMessage());
                continue;
            }

            if (team != null) {
                this.teamsByName.put(team.getName(), team);
                this.teamsSortedByRank.put(team.getRank(), team);
            }
        }
        this.teamsSortedByRank.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(java.util.Map.Entry::getKey);

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

    public LinkedHashMap<Integer, ? extends Team> getTeamsSortedByRank() {
        return teamsSortedByRank;
    }

    public LinkedHashMap<Integer, ? extends Team> getTeamsSortedByRank(int amount) {
        LinkedHashMap<Integer, Team> teams = new LinkedHashMap<>();
        for (java.util.Map.Entry<Integer, Team> entry : teamsSortedByRank.entrySet()) {
            if (amount <= 0) {
                break;
            }
            teams.put(entry.getKey(), entry.getValue());
            amount--;
        }
        teams.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(java.util.Map.Entry::getKey);
        return teams;
    }


    public Integer getAutoStartPlayerNumber() {return getInfo().getAutoStartPlayerNumber();}

    public Integer getMinPlayerNumber() {return getInfo().getMinPlayerNumber();}

    public List<Integer> getTeamSizes() {return getInfo().getTeamSizes();}

    public Type.Availability getTeamMerge() {return getInfo().getTeamMerge();}

    public boolean isEqualTimeSizeRequired() {return getInfo().isEqualTimeSizeRequired();}

    public boolean hideTeams() {
        return getInfo().hideTeams();
    }

    public Type.Discord getDiscordType() {return getInfo().getDiscordType();}

    public List<String> getDescription() {return getInfo().getDescription();}

    public String getName() {return getInfo().getName();}

    public String getDisplayName() {return getInfo().getDisplayName();}

    public Integer getMaxPlayers() {return getInfo().getMaxPlayers();}

    public String getHeadLine() {return getInfo().getHeadLine();}

    public ItemStack getItem() {return getInfo().getItem();}

    public Integer getSlot() {return getInfo().getSlot();}

    public Type.Availability getMapAvailability() {return getInfo().getMapAvailability();}

    public Type.Availability getKitAvailability() {return getInfo().getKitAvailability();}

    public String getTexturePackLink() {return getInfo().getTexturePackLink();}

    public Boolean hasTexturePack() {return getInfo().hasTexturePack();}
}
