/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.game.TmpGameInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import org.bukkit.inventory.ItemStack;

public class TmpGame extends Game<TmpGameInfo> {

    protected final java.util.Map<String, Team> teamsByName = new HashMap<>();
    protected final TreeSet<Team> teamsSortedByRank = new TreeSet<>(
            Comparator.comparingInt(Team::getRank));

    public TmpGame(DbTmpGame database, boolean loadWorlds) {
        super(database, new TmpGameInfo(database.getInfo()), loadWorlds);

        for (DbTeam dbTeam : database.getTeams()) {
            Team team;
            try {
                team = this.loadTeam(dbTeam);
            } catch (UnsupportedGroupRankException e) {
                Loggers.GROUPS.warning(e.getMessage());
                continue;
            }

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
        return teamsSortedByRank.stream().limit(amount).toList();
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

    public Type.Availability getTeamMerge() {
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

    public Type.Discord getDiscordType() {
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

    public Type.Availability getMapAvailability() {
        return getInfo().getMapAvailability();
    }

    public Type.Availability getKitAvailability() {
        return getInfo().getKitAvailability();
    }

    public String getTexturePackLink() {
        return getInfo().getTexturePackLink();
    }

    public Boolean hasTexturePack() {
        return getInfo().hasTexturePack();
    }
}
