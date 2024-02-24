/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exception.LocationNotInWorldException;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.object.DbLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameRule;
import org.bukkit.Material;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Map {

  protected final Logger logger = LogManager.getLogger("map");

  protected final String name;
  protected final String displayName;
  protected final Integer minPlayers;
  protected final Integer maxPlayers;
  protected final List<Integer> teamAmounts;
  protected final ExItemStack item;
  protected final List<String> description;
  protected final java.util.Map<String, String> properties;
  protected final DbMap dbMap;
  protected final String worldName;
  protected final java.util.Map<Integer, ExLocation> locationsById = new HashMap<>();
  protected final List<String> authors;
  protected ExWorld world;
  private Integer votes = 0;

  public Map(DbMap map, boolean loadWorld) {
    this.dbMap = map;

    map = map.toLocal();

    this.name = map.getName();
    this.displayName = map.getDisplayName();
    this.minPlayers = map.getMinPlayers();
    this.maxPlayers = map.getMaxPlayers();
    this.teamAmounts = map.getTeamAmounts();

    String materialName = map.getItemName();
    if (materialName != null) {
      materialName = materialName.toUpperCase();

      ExItemStack item;
      try {
        item = new ExItemStack(Material.getMaterial(materialName));
      } catch (IllegalArgumentException var6) {
        this.logger.warn("Can not load item for map {}", this.getName());
        item = new ExItemStack(new ExItemStack(Material.MAP));
      }
      this.item = item;
    } else {
      this.item = new ExItemStack(Material.BARRIER);
    }

    this.properties = map.getProperties();
    this.description = map.getDescription();
    this.worldName = map.getWorldName();
    this.authors = map.getAuthorNames();

    if (loadWorld) {
      this.loadWorld();
    }

  }

  public Map(ExWorld world) {
    this.name = world.getName();
    this.displayName = world.getName();
    this.item = new ExItemStack(Material.MAP);
    this.worldName = world.getName();
    this.world = world;
    this.minPlayers = null;
    this.maxPlayers = null;
    this.teamAmounts = null;
    this.description = null;
    this.properties = null;
    this.authors = null;
    this.dbMap = null;
  }

  private void loadWorld() {
    this.world = Server.getWorld(this.worldName);

    if (this.world == null) {
      this.logger.warn("World '{}' of map '{}' could not loaded, world not exists", this.worldName, this.name);
      return;
    }

    for (java.util.Map.Entry<Integer, DbLocation> entry : this.getDatabase().getMapLocations().entrySet()) {
      try {
        this.locationsById.put(entry.getKey(), Server.getExLocationFromDbLocation(entry.getValue()));
      } catch (WorldNotExistException e) {
        this.logger.warn("Map '{}' can not load location '{}': {}", this.worldName, entry.getKey(), e.getMessage());
      }
    }

    this.loadWorldSettings();

    this.logger.info("Loaded locations of map '{}'", this.name);
  }

  protected void loadWorldSettings() {
    this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    this.world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
    this.world.setAutoSave(false);
  }

  public String getName() {
    return this.name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Integer getMinPlayers() {
    return this.minPlayers;
  }

  public Integer getMaxPlayers() {
    return this.maxPlayers;
  }

  public List<Integer> getTeamAmounts() {
    return teamAmounts;
  }

  public ExLocation getLocation(int number) {
    return this.locationsById.get(number);
  }

  public DbMap getDatabase() {
    return this.dbMap;
  }

  public int addLocation(ExLocation location) throws LocationNotInWorldException {
    int number = this.locationsById.size() + 1;
    if (location.getExWorld().equals(this.world)) {
      this.locationsById.put(number, location);
      this.dbMap.setLocation(number, Server.getDbLocationFromLocation(location));
      return number;
    } else {
      throw new LocationNotInWorldException(location);
    }
  }

  public boolean setLocation(int number, ExLocation location) throws LocationNotInWorldException {
    if (location.getExWorld().equals(this.world)) {
      if (this.locationsById.containsKey(number)) {
        this.locationsById.put(number, location);
        this.dbMap.deleteLocation(number);
        this.dbMap.setLocation(number, Server.getDbLocationFromLocation(location));
        return true;
      } else {
        this.locationsById.put(number, location);
        this.dbMap.setLocation(number, Server.getDbLocationFromLocation(location));
        return false;
      }
    } else {
      throw new LocationNotInWorldException(location);
    }
  }

  public boolean removeLocation(ExLocation location) {
    if (this.locationsById.containsValue(location)) {

      for (java.util.Map.Entry<Integer, ExLocation> entry : this.locationsById.entrySet()) {
        if (entry.getValue().equals(location)) {
          this.removeLocation(entry.getKey());
          return true;
        }
      }
    }

    return false;
  }

  public boolean removeLocation(int number) {
    if (this.locationsById.containsKey(number)) {
      this.locationsById.remove(number);
      this.dbMap.deleteLocation(number);
      return true;
    } else {
      return false;
    }
  }

  public boolean containsLocation(int number) {
    return this.locationsById.containsKey(number);
  }

  public boolean containsLocation(ExLocation location) {
    return this.locationsById.containsValue(location);
  }

  public java.util.Map<Integer, ExLocation> getLocationsById() {
    return this.locationsById;
  }

  public Collection<ExLocation> getLocations() {
    return this.locationsById.values();
  }

  public List<ExLocation> getLocations(int begin) {
    return this.locationsById.entrySet().stream()
        .filter(e -> e.getKey() >= begin)
        .map(java.util.Map.Entry::getValue)
        .collect(Collectors.toList());
  }

  public List<ExLocation> getLocations(int begin, int end) {
    return this.locationsById.entrySet().stream()
        .filter(e -> e.getKey() >= begin && e.getKey() < end)
        .map(java.util.Map.Entry::getValue)
        .collect(Collectors.toList());
  }

  public List<ExLocation> getLocations(Collection<Integer> ids) {
    return this.locationsById.entrySet().stream()
        .filter(e -> ids.contains(e.getKey()))
        .map(java.util.Map.Entry::getValue)
        .collect(Collectors.toList());
  }

  public Collection<Integer> getLocationIds() {
    return this.locationsById.keySet();
  }

  public Collection<Integer> getLocationsIds(int begin, int end) {
    return this.getLocationIds().stream().filter(i -> i >= begin && i < end).toList();
  }

  public Collection<Integer> getLocationIds(int begin) {
    return this.getLocationIds().stream().filter(i -> i >= begin).toList();
  }

  public Integer getSpawnsAmount() {
    return this.locationsById.size();
  }

  public void addVote() {
    this.votes++;
  }

  public void removeVote() {
    if (this.votes > 0) {
      this.votes--;
    }
  }

  public Integer getVotes() {
    return this.votes;
  }

  public void resetVotes() {
    this.votes = 0;
  }

  public ExWorld getWorld() {
    if (this.world == null) {
      this.loadWorld();
    }
    return this.world;
  }

  public void setWorld(ExWorld world) {
    this.world = world;
  }

  public ExItemStack getItem() {
    return this.item;
  }

  public List<String> getDescription() {
    return this.description;
  }

  public java.util.Map<String, String> getProperties() {
    return properties;
  }

  public String getProperty(String key) {
    return this.properties.get(key);
  }

  public void setProperty(String key, String value) {
    this.properties.put(key, value);
    this.getDatabase().setProperty(key, value);
  }

  public <T> T getProperty(String key, Class<T> clazz, T defaultValue) {
    return this.getProperty(key, clazz, defaultValue, v -> {
    });
  }

  public <T> T getProperty(String key, Class<T> clazz, T defaultValue, Consumer<String> onError) {
    String value = this.getProperty(key);

    if (value == null) {
      return defaultValue;
    }

    if (clazz.equals(String.class)) {
      return (T) value;
    } else if (clazz.equals(Integer.class)) {
      try {
        return (T) Integer.valueOf(value);
      } catch (NumberFormatException e) {
        onError.accept(value);
        return defaultValue;
      }
    } else if (clazz.equals(Float.class)) {
      try {
        return (T) Float.valueOf(value);
      } catch (NumberFormatException e) {
        onError.accept(value);
        return defaultValue;
      }
    } else if (clazz.equals(UUID.class)) {
      try {
        return (T) UUID.fromString(value);
      } catch (IllegalArgumentException e) {
        onError.accept(value);
        return defaultValue;
      }
    }

    onError.accept(value);
    return defaultValue;
  }

  public List<String> getAuthors() {
    return this.authors;
  }

  public List<String> getAuthors(int length) {
    if (this.authors == null) {
      return List.of(Server.DEFAULT_NETWORK_NAME + " Community");
    }
    LinkedList<String> authors = new LinkedList<>();
    Iterator<String> author = this.authors.iterator();

    while (author.hasNext()) {
      String part = author.next();
      if (!authors.isEmpty()) {
        if (authors.getLast().length() + part.length() + ", ".length() <= length) {
          String entry = authors.getLast();
          if (author.hasNext()) {
            entry = entry + part + ", ";
          } else {
            entry = entry + part;
          }

          authors.set(authors.size() - 1, entry);
        } else if (author.hasNext()) {
          authors.addLast(part + ", ");
        } else {
          authors.addLast(part);
        }
      } else if (author.hasNext()) {
        authors.addLast(part + ", ");
      } else {
        authors.addLast(part);
      }
    }

    return authors;
  }
}
