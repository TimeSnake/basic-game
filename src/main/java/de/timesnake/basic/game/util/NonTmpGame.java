/*
 * basic-game.main
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

package de.timesnake.basic.game.util;

import de.timesnake.database.util.game.DbNonTmpGame;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.game.NonTmpGameInfo;
import org.bukkit.inventory.ItemStack;

public class NonTmpGame extends Game<NonTmpGameInfo> {

    public NonTmpGame(DbNonTmpGame database, boolean loadWorlds) {
        super(database, new NonTmpGameInfo(database.getInfo()), loadWorlds);
    }

    public boolean isCreationRequestable() {return getInfo().isCreationRequestable();}

    public boolean isOwnable() {return getInfo().isOwnable();}

    public boolean isNetherAndEndAllowed() {
        return getInfo().isNetherAndEndAllowed();
    }

    public String getName() {return getInfo().getName();}

    public String getDisplayName() {return getInfo().getDisplayName();}

    public Integer getMaxPlayers() {return getInfo().getMaxPlayers();}

    public String getHeadLine() {return getInfo().getHeadLine();}

    public ItemStack getItem() {return getInfo().getItem();}

    public Integer getSlot() {return getInfo().getSlot();}

    public Type.Availability getMapAvailability() {return getInfo().getMapAvailability();}

    public Type.Availability getKitAvailability() {return getInfo().getKitAvailability();}

    public String getTexturePackLink() {return getInfo().getTexturePackLink();}

    public Boolean hasTexturePack() {return getInfo().hasTexturePack();}
}