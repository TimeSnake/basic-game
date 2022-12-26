/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.game.util.user;

import de.timesnake.basic.game.util.game.GameStat;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.database.util.game.GameUserStatistic;
import de.timesnake.library.basic.util.statistics.Stat;
import de.timesnake.library.basic.util.statistics.StatPeriod;
import de.timesnake.library.basic.util.statistics.StatType;
import de.timesnake.library.basic.util.statistics.Statistic;
import java.util.Arrays;
import java.util.Map;
import org.bukkit.entity.Player;

public class StatUser extends SpectatorUser {

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
            public <Value> Stat<Value> addStat(StatType<Value> type,
                    Map<StatPeriod, Value> values) {
                GameStat<Value> stat = new GameStat<>(dbStats, type, values);
                this.statsByName.put(type.getName(), stat);
                return stat;
            }
        };

        Map<StatType<?>, Map<StatPeriod, Object>> values = this.dbStats.get(
                Arrays.asList(StatPeriod.values()),
                GameServer.getGame().getStats().toArray(new StatType[0]));

        for (StatType<?> statType : GameServer.getGame().getStats()) {
            this.loadStat(statType, values.get(statType));
        }
    }

    private <Value> void loadStat(StatType<Value> type, Map<StatPeriod, ?> values) {
        this.stats.addStat(type, ((Map<StatPeriod, Value>) values));
    }

    public GameUserStatistic getDatabaseStats() {
        return dbStats;
    }

    public <Value> Stat<Value> getStat(StatType<Value> type) {
        return this.stats.getStat(type);
    }


}
