package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.exceptions.UnsupportedGroupRankException;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.game.TmpGameInfo;
import org.bukkit.ChatColor;
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

    public List<String> getDescription() {return getInfo().getDescription();}

    public String getName() {return getInfo().getName();}

    public String getDisplayName() {return getInfo().getDisplayName();}

    public ChatColor getChatColor() {return getInfo().getChatColor();}

    public Integer getMaxPlayers() {return getInfo().getMaxPlayers();}

    public String getHeadLine() {return getInfo().getHeadLine();}

    public ItemStack getItem() {return getInfo().getItem();}

    public Integer getSlot() {return getInfo().getSlot();}

    public Type.Availability getMapAvailability() {return getInfo().getMapAvailability();}

    public Type.Availability getKitAvailability() {return getInfo().getKitAvailability();}

    public String getTexturePackLink() {return getInfo().getTexturePackLink();}

    public Boolean hasTexturePack() {return getInfo().hasTexturePack();}
}
