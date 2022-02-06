package de.timesnake.basic.game.util;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Plugin;
import de.timesnake.database.util.game.DbKit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Kit {

    public static final Kit RANDOM = new Kit(0, "Random", Material.GRAY_WOOL, List.of("§7Click to get a random kit"));


    private final Integer id;
    private final String name;
    private Material material;
    private final List<String> description = new ArrayList<>();

    public Kit(Integer id, String name, Material material, List<String> description) {
        this.id = id;
        this.name = "§5" + name;
        this.material = material;

        for (String s : description) {
            this.description.add("§f" + s);
        }

    }

    public Kit(DbKit kit) {
        this.id = kit.getId();
        this.name = kit.getName();

        for (String s : kit.getDescription()) {
            this.description.add("§f" + s);
        }

        String materialName = kit.getItemType();

        try {
            this.material = Material.getMaterial(materialName);
        } catch (IllegalArgumentException var4) {
            Server.printError(Plugin.BUKKIT, "Error while loading item for kit " + this.getName(), "Kit");
        }

    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Collection<String> getDescription() {
        return this.description;
    }

    public Material getMaterial() {
        return material;
    }
}
