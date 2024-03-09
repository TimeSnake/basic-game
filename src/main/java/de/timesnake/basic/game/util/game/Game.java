/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbKit;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.library.basic.util.Availability;
import de.timesnake.library.basic.util.statistics.StatType;
import de.timesnake.library.game.GameInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

public class Game<Info extends GameInfo> {

  protected final Logger logger = LogManager.getLogger("game");

  protected final DbGame database;

  protected final Info info;

  protected final List<Kit> kits;

  protected final HashMap<String, Map> maps = new HashMap<>();

  protected final HashMap<Integer, HashMap<Integer, StatType<?>>> statByLineByDisplay = new HashMap<>();
  protected final HashMap<Integer, HashMap<Integer, StatType<?>>> globalStatByLineByDisplay = new HashMap<>();

  public Game(DbGame database, Info info, boolean loadWorlds) {
    this.database = database;
    this.info = info;

    this.kits = new LinkedList<>();

    if (this.info.getKitAvailability().equals(Availability.ALLOWED)
        || this.info.getKitAvailability().equals(Availability.REQUIRED)) {
      this.loadKits(database);
    }

    this.loadMaps(loadWorlds);

    for (StatType<?> stat : database.getStats()) {
      Integer displayIndex = stat.getDisplayIndex();
      Integer lineIndex = stat.getDisplayLineIndex();

      this.statByLineByDisplay.computeIfAbsent(displayIndex, (i) -> new HashMap<>()).put(lineIndex, stat);

      Integer globalDisplayIndex = stat.getGlobalDisplayIndex();
      Integer globalLineIndex = stat.getGlobalDisplayLineIndex();

      if (stat.getGlobalDisplay() && globalDisplayIndex != null && globalLineIndex != null) {
        this.globalStatByLineByDisplay.computeIfAbsent(globalDisplayIndex,
            (i) -> new HashMap<>()).put(globalLineIndex, stat);
      }
    }

  }

  public final void loadMaps(boolean loadWorlds) {
    if (this.info.getMapAvailability().equals(Availability.REQUIRED)
        || this.info.getMapAvailability().equals(Availability.ALLOWED)) {

      this.maps.clear();

      for (DbMap dbMap : this.database.getMaps()) {
        if (dbMap.isEnabled()) {
          Map map = this.loadMap(dbMap.toLocal(), loadWorlds);
          if (map != null) {
            this.maps.put(map.getName(), map);
            if (loadWorlds && map.getWorld() != null) {
              this.logger.info("Loaded map '{}' (world: '{}')", map.getName(), map.getWorld().getName());
            } else {
              this.logger.info("Loaded map {}", map.getName());
            }
          }
        } else {
          this.logger.info("NOT loaded map {} (disabled)", dbMap.getName());
        }
      }
    }

  }

  public Map loadMap(DbMap dbMap, boolean loadWorld) {
    return new Map(dbMap, loadWorld);
  }

  public Team loadTeam(DbTeam team) {
    return new Team(team);
  }

  public void loadKits(DbGame database) {
    for (DbKit dbKit : database.getKits()) {
      this.loadKit(dbKit).ifPresent(this.kits::add);
    }
  }

  public Optional<? extends Kit> loadKit(DbKit dbKit) {
    return Optional.of(new Kit(dbKit));
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

  public Collection<? extends Map> getMaps(Predicate<Map> filter) {
    return this.maps.values().stream().filter(filter).toList();
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
