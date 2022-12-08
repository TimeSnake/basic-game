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

package de.timesnake.basic.game.util.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.basic.bukkit.util.exception.LocationNotInWorldException;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.object.DbLocation;
import org.bukkit.Material;

import java.util.*;
import java.util.stream.Collectors;

public class Map {

    protected final String name;
    protected final String displayName;
    protected final Integer minPlayers;
    protected final Integer maxPlayers;
    protected final ExItemStack item;
    protected final ArrayList<String> description;
    protected final ArrayList<String> info;
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

        String materialName = map.getItemName();
        if (materialName != null) {
            materialName = materialName.toUpperCase();

            ExItemStack item;
            try {
                item = new ExItemStack(Material.getMaterial(materialName));
            } catch (IllegalArgumentException var6) {
                Server.printWarning(Plugin.BUKKIT, "Can not load item for map " + this.getName(),
                        "Game", "Map");
                item = new ExItemStack(new ExItemStack(Material.MAP));
            }
            this.item = item;
        } else {
            this.item = new ExItemStack(Material.BARRIER);
        }

        this.description = map.getDescription();
        this.info = map.getInfo();
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
        this.description = null;
        this.info = null;
        this.authors = null;
        this.dbMap = null;
    }

    private void loadWorld() {
        this.world = Server.getWorld(this.worldName);
        if (this.world == null) {
            Server.printWarning(Plugin.BUKKIT, "Map-World " + this.worldName + " of map " + this.name +
                    " could not loaded, world not exists", "Game", "Map");
        } else {
            for (java.util.Map.Entry<Integer, DbLocation> entry : this.getDatabase().getMapLocations().entrySet()) {

                try {
                    this.locationsById.put(entry.getKey(), Server.getExLocationFromDbLocation(entry.getValue()));
                } catch (WorldNotExistException var4) {
                    Server.printWarning(Plugin.BUKKIT, "Map " + this.worldName + " can not load location " +
                            entry.getKey(), "Game", "Map");
                }
            }

            Server.printText(Plugin.BUKKIT, "Loaded locations of map " + this.name, "Game", "Map");
        }
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
            this.dbMap.addLocation(number, Server.getDbLocationFromLocation(location));
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
                this.dbMap.addLocation(number, Server.getDbLocationFromLocation(location));
                return true;
            } else {
                this.locationsById.put(number, location);
                this.dbMap.addLocation(number, Server.getDbLocationFromLocation(location));
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
        return this.locationsById.entrySet().stream().filter(
                e -> e.getKey() >= begin).map(java.util.Map.Entry::getValue).collect(Collectors.toList());
    }

    public List<ExLocation> getLocations(int begin, int end) {
        return this.locationsById.entrySet().stream().filter(
                e -> e.getKey() >= begin && e.getKey() < end).map(java.util.Map.Entry::getValue).collect(Collectors.toList());
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

    public ArrayList<String> getDescription() {
        return this.description;
    }

    public ArrayList<String> getInfo() {
        return this.info;
    }

    public List<String> getAuthors() {
        return this.authors;
    }

    public List<String> getAuthors(int length) {
        if (this.authors == null) {
            return List.of();
        }
        LinkedList<String> authors = new LinkedList<>();
        Iterator<String> author = this.authors.iterator();

        while (author.hasNext()) {
            String part = author.next();
            if (authors.size() > 0) {
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
