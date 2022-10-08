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

import de.timesnake.database.util.game.GameUserStatistic;
import de.timesnake.library.basic.util.statistics.Stat;
import de.timesnake.library.basic.util.statistics.StatPeriod;
import de.timesnake.library.basic.util.statistics.StatType;
import de.timesnake.library.basic.util.statistics.Statistic;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;

public class StatUser extends TeamUser {

    private final Statistic stats;
    private final GameUserStatistic dbStats;

    public StatUser(Player player) {
        super(player);

        this.dbStats = GameServer.getGame().getDatabase().getUserStatistic(this.getUniqueId());

        this.stats = new Statistic() {
            @Override
            public <Value> Stat<Value> addStat(StatType<Value> type) {
                GameStat<Value> stat = new GameStat<>(dbStats, type);
                this.statsByName.put(type.getName(), stat);
                return stat;
            }

            @Override
            public <Value> Stat<Value> addStat(StatType<Value> type, Value value) {
                GameStat<Value> stat = new GameStat<>(dbStats, type, value);
                this.statsByName.put(type.getName(), stat);
                return stat;
            }

            @Override
            public <Value> Stat<Value> addStat(StatType<Value> type, Map<StatPeriod, Value> values) {
                GameStat<Value> stat = new GameStat<>(dbStats, type, values);
                this.statsByName.put(type.getName(), stat);
                return stat;
            }
        };

        Map<StatType<?>, Map<StatPeriod, Object>> values = this.dbStats.get(Arrays.asList(StatPeriod.values()),
                GameServer.getGame().getStats().toArray(new StatType[0]));

        for (StatType<?> statType : GameServer.getGame().getStats()) {
            this.loadStat(statType, values.get(statType));
        }
    }

    private <Value> void loadStat(StatType<Value> type, Map<StatPeriod, ?> values) {
        this.stats.addStat(type, ((Map<StatPeriod, Value>) values));
    }

    public GameUserStatistic getDbStats() {
        return dbStats;
    }

    public <Value> Stat<Value> getStat(StatType<Value> type) {
        return this.stats.getStat(type);
    }


}
