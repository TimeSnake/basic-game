package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.exceptions.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.game.GameInfo;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbKit;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.basic.util.statistics.Stat;

import java.util.*;

public class Game extends GameInfo {

    protected final ArrayList<Kit> kits;
    protected final java.util.Map<String, Team> teamsByName = new HashMap<>();
    protected final LinkedHashMap<Integer, Team> teamsSortedByRank = new LinkedHashMap<>();
    protected final HashMap<String, Map> maps = new HashMap<>();

    protected final HashMap<Integer, HashMap<Integer, Stat<?>>> statsByLineByDisplay = new HashMap<>();

    public Game(DbGame game, boolean loadWorlds) {
        super(game);

        for (DbTeam dbTeam : game.getTeams()) {
            Team team;
            try {
                team = this.loadTeam(dbTeam);
            } catch (UnsupportedGroupRankException e) {
                Server.printError(Plugin.BUKKIT, ChatColor.WARNING + e.getMessage());
                continue;
            }

            if (team != null) {
                this.teamsByName.put(team.getName(), team);
                this.teamsSortedByRank.put(team.getRank(), team);
            }
        }
        this.teamsSortedByRank.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(java.util.Map.Entry::getKey);

        this.kits = new ArrayList<>();

        if (this.kitAvailability.equals(Type.Availability.ALLOWED) || this.kitAvailability.equals(Type.Availability.REQUIRED)) {
            for (DbKit dbKit : game.getKits()) {
                Kit kit = this.loadKit(dbKit);
                if (kit != null) {
                    this.kits.add(kit);
                }
            }
        }

        this.loadMaps(loadWorlds);

        for (Stat<?> stat : game.getStats()) {
            Integer displayIndex = stat.getDisplayIndex();
            Integer lineIndex = stat.getDisplayLineIndex();

            HashMap<Integer, Stat<?>> display = this.statsByLineByDisplay.computeIfAbsent(displayIndex, (i) -> new HashMap<>());
            display.put(lineIndex, stat);

        }

    }

    public final void loadMaps(boolean loadWorlds) {
        if (this.mapAvailability.equals(Type.Availability.REQUIRED) || this.mapAvailability.equals(Type.Availability.ALLOWED)) {

            this.maps.clear();

            for (DbMap dbMap : this.database.getMaps()) {
                if (dbMap.isEnabled()) {
                    Map map = this.loadMap(dbMap, loadWorlds);
                    if (map != null) {
                        this.maps.put(map.getName(), map);
                        if (loadWorlds && map.getWorld() != null) {
                            Server.printText(de.timesnake.basic.bukkit.util.chat.Plugin.BUKKIT, "Loaded map " + map.getName() + " (world: " + map.getWorld().getName() + ")", "Game", "Map");
                        } else {
                            Server.printText(de.timesnake.basic.bukkit.util.chat.Plugin.BUKKIT, "Loaded map " + map.getName(), "Game", "Map");
                        }
                    }
                } else {
                    Server.printText(de.timesnake.basic.bukkit.util.chat.Plugin.BUKKIT, "NOT loaded map " + dbMap.getName() + " (disabled)", "Game", "Map");
                }
            }
        }

    }

    public Map loadMap(DbMap dbMap, boolean loadWorld) {
        return new Map(dbMap, loadWorld);
    }

    public Team loadTeam(DbTeam team) throws UnsupportedGroupRankException {
        return new Team(team);
    }

    public Kit loadKit(DbKit dbKit) {
        return new Kit(dbKit);
    }

    public Collection<Map> getMaps() {
        return this.maps.values();
    }

    public Map getMap(String mapName) {
        return this.maps.get(mapName);
    }

    public Collection<Kit> getKits() {
        return this.kits;
    }

    public Kit getKit(int index) {
        return this.kits.get(index);
    }

    public boolean hasTeam(String name) {
        return this.teamsByName.containsKey(name);
    }

    public final Team getTeam(String team) {
        return this.teamsByName.get(team);
    }

    public final Collection<Team> getTeams() {
        return new ArrayList<>(this.teamsByName.values());
    }

    public java.util.Map<String, Team> getTeamsByName() {
        return teamsByName;
    }

    public LinkedHashMap<Integer, Team> getTeamsSortedByRank() {
        return teamsSortedByRank;
    }

    public LinkedHashMap<Integer, Team> getTeamsSortedByRank(int amount) {
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

    public Set<Stat<?>> getStats() {
        Set<Stat<?>> stats = new HashSet<>();
        this.statsByLineByDisplay.forEach((key, list) -> stats.addAll(list.values()));
        return stats;
    }

    public HashMap<Integer, HashMap<Integer, Stat<?>>> getStatsByLineByDisplay() {
        return statsByLineByDisplay;
    }
}
