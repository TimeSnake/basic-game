/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickListener;
import de.timesnake.database.util.game.DbKit;
import de.timesnake.library.basic.util.BuilderBasis;
import de.timesnake.library.basic.util.BuilderNotFullyInstantiatedException;
import de.timesnake.library.basic.util.Loggers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Material;

public class Kit {

    public static final Kit RANDOM = new Builder()
            .id(0)
            .name("Random")
            .material(Material.GRAY_WOOL)
            .addDescription("§7Click to get a random kit")
            .build();


    private final int id;
    private final String name;
    private final List<String> description = new ArrayList<>();
    private Material material;

    public Kit(Builder builder) {
        this.id = builder.id;
        this.name = "§5" + builder.name;
        this.material = builder.material;

        for (String s : builder.description) {
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
            Loggers.KITS.warning("Error while loading item for kit " + this.getName());
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

    public ExItemStack createDisplayItem(UserInventoryClickListener listener) {
        return new ExItemStack(this.getMaterial())
                .setDisplayName(this.getName())
                .setExLore(new ArrayList<>(this.getDescription()))
                .hideAll()
                .onClick(listener);
    }

    public static class Builder implements BuilderBasis {

        private int id;
        private String name;
        private LinkedList<String> description = new LinkedList<>();
        private Material material;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder addDescription(String... lines) {
            for (String line : lines) {
                this.description.addLast(line);
            }
            return this;
        }

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        @Override
        public void checkBuild() {
            if (this.material == null) {
                throw new BuilderNotFullyInstantiatedException("material is null");
            }
            if (this.name == null) {
                throw new BuilderNotFullyInstantiatedException("name is null");
            }
        }

        @Override
        public Kit build() {
            this.checkBuild();
            return new Kit(this);
        }
    }
}
