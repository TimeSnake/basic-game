package de.timesnake.basic.game.util;

import de.timesnake.database.util.game.DbNonTmpGame;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.game.NonTmpGameInfo;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class NonTmpGame extends Game<NonTmpGameInfo> {

    public NonTmpGame(DbNonTmpGame database, boolean loadWorlds) {
        super(database, new NonTmpGameInfo(database.getInfo()), loadWorlds);
    }

    public boolean isGenerateable() {return getInfo().isGenerateable();}

    public boolean isAutoDeleteAllowed() {return getInfo().isAutoDeleteAllowed();}

    public boolean isOwnable() {return getInfo().isOwnable();}

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
