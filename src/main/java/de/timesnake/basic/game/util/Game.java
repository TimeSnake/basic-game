package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.UnsupportedGroupRankException;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbKit;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.basic.util.statistics.StatType;
import de.timesnake.library.game.GameInfo;

import java.util.*;

public class Game<Info extends GameInfo> {

    protected final DbGame database;

    protected final Info info;

    protected final ArrayList<Kit> kits;

    protected final HashMap<String, Map> maps = new HashMap<>();

    protected final HashMap<Integer, HashMap<Integer, StatType<?>>> statByLineByDisplay = new HashMap<>();
    protected final HashMap<Integer, HashMap<Integer, StatType<?>>> globalStatByLineByDisplay = new HashMap<>();

    public Game(DbGame database, Info info, boolean loadWorlds) {
        this.database = database;
        this.info = info;

        this.kits = new ArrayList<>();

        if (this.info.getKitAvailability().equals(Type.Availability.ALLOWED) || this.info.getKitAvailability().equals(Type.Availability.REQUIRED)) {
            for (DbKit dbKit : database.getKits()) {
                Kit kit = this.loadKit(dbKit);
                if (kit != null) {
                    this.kits.add(kit);
                }
            }
        }

        this.loadMaps(loadWorlds);

        for (StatType<?> stat : database.getStats()) {
            Integer displayIndex = stat.getDisplayIndex();
            Integer lineIndex = stat.getDisplayLineIndex();

            this.statByLineByDisplay.computeIfAbsent(displayIndex, (i) -> new HashMap<>()).put(lineIndex, stat);

            Integer globalDisplayIndex = stat.getGlobalDisplayIndex();
            Integer globalLineIndex = stat.getGlobalDisplayLineIndex();

            if (stat.getGlobalDisplay() && globalDisplayIndex != null && globalLineIndex != null) {
                this.globalStatByLineByDisplay.computeIfAbsent(globalDisplayIndex, (i) -> new HashMap<>()).put(globalLineIndex, stat);
            }
        }

    }

    public final void loadMaps(boolean loadWorlds) {
        if (this.info.getMapAvailability().equals(Type.Availability.REQUIRED) || this.info.getMapAvailability().equals(Type.Availability.ALLOWED)) {

            this.maps.clear();

            for (DbMap dbMap : this.database.getMaps()) {
                if (dbMap.isEnabled()) {
                    Map map = this.loadMap(dbMap.toLocal(), loadWorlds);
                    if (map != null) {
                        this.maps.put(map.getName(), map);
                        if (loadWorlds && map.getWorld() != null) {
                            Server.printText(de.timesnake.basic.bukkit.util.chat.Plugin.BUKKIT,
                                    "Loaded map " + map.getName() + " (world: " + map.getWorld().getName() + ")",
                                    "Game", "Map");
                        } else {
                            Server.printText(de.timesnake.basic.bukkit.util.chat.Plugin.BUKKIT,
                                    "Loaded map " + map.getName(), "Game", "Map");
                        }
                    }
                } else {
                    Server.printText(de.timesnake.basic.bukkit.util.chat.Plugin.BUKKIT,
                            "NOT loaded map " + dbMap.getName() + " (disabled)", "Game", "Map");
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

    public DbGame getDatabase() {
        return database;
    }

    public Info getInfo() {
        return info;
    }

    public Collection<? extends Map> getMaps() {
        return this.maps.values();
    }

    public Map getMap(String mapName) {
        return this.maps.get(mapName);
    }

    public Collection<? extends Kit> getKits() {
        return this.kits;
    }

    public Kit getKit(int index) {
        return this.kits.get(index);
    }

    public Set<StatType<?>> getStats() {
        Set<StatType<?>> stats = new HashSet<>();
        this.statByLineByDisplay.forEach((key, list) -> stats.addAll(list.values()));
        return stats;
    }

    public HashMap<Integer, HashMap<Integer, StatType<?>>> getStatByLineByDisplay() {
        return statByLineByDisplay;
    }

    public HashMap<Integer, HashMap<Integer, StatType<?>>> getGlobalStatByLineByDisplay() {
        return globalStatByLineByDisplay;
    }
}
