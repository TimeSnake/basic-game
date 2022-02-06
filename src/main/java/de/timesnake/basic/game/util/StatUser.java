package de.timesnake.basic.game.util;

import de.timesnake.database.util.game.GameUserStatistic;
import de.timesnake.library.basic.util.statistics.Stat;
import de.timesnake.library.basic.util.statistics.Statistic;
import org.bukkit.entity.Player;

public class StatUser extends TeamUser {

    private final Statistic stats;
    private final GameUserStatistic dbStats;

    public StatUser(Player player) {
        super(player);

        this.stats = new Statistic();
        this.dbStats = GameServer.getGame().getDatabase().getUserStatistic(this.getUniqueId());

        for (Stat<?> type : GameServer.getGame().getStats()) {
            this.updateStat(type);
        }
    }

    private <Value> void updateStat(Stat<Value> type) {
        Value value = this.dbStats.getValue(type);
        if (value == null) {
            value = type.getDefaultValue();
            this.dbStats.addValue(type, type.getDefaultValue());
        }
        this.setStat(type, value);
    }

    public <Value> Value getStat(Stat<Value> type) {
        return this.stats.getStat(type);
    }

    public <Value> void setStat(Stat<Value> type, Value value) {
        this.stats.setStat(type, value);
        this.dbStats.setValue(type, value);
    }

    public <Value> void increaseStat(Stat<Value> type, Value value) {
        Value result = this.stats.increaseStat(type, value);
        this.dbStats.setValue(type, result);
    }

    public <Value> void higherStat(Stat<Value> type, Value value) {
        boolean higher = this.stats.higherStat(type, value);
        if (higher) this.dbStats.setValue(type, value);
    }

}
