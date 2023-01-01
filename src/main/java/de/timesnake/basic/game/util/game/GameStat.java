/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.game;

import de.timesnake.database.util.game.GameUserStatistic;
import de.timesnake.library.basic.util.statistics.Stat;
import de.timesnake.library.basic.util.statistics.StatPeriod;
import de.timesnake.library.basic.util.statistics.StatType;
import java.util.Map;

public class GameStat<Value> extends Stat<Value> {

    private final GameUserStatistic database;

    public GameStat(GameUserStatistic database, StatType<Value> type) {
        this(database, type, type.getDefaultValue());
    }

    public GameStat(GameUserStatistic database, StatType<Value> type, Value value) {
        super(type);
        this.database = database;

        this.setAll(value);
    }

    public GameStat(GameUserStatistic database, StatType<Value> type, Map<StatPeriod, Value> values) {
        super(type, values);
        this.database = database;

        for (Map.Entry<StatPeriod, Value> entry : values.entrySet()) {
            this.set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void set(StatPeriod period, Value value) {
        super.set(period, value);
        this.database.setValue(period, this.type, value);
    }

    @Override
    public Value get(StatPeriod period) {
        return super.get(period);
    }

    @Override
    public Map<StatPeriod, Value> getAll() {
        return super.getAll();
    }

    @Override
    public void setAll(Value value) {
        super.setAll(value);
        this.database.setValues(this.type, value);
    }

    @Override
    public Value increase(StatPeriod period, Value value) {
        Value res = super.increase(period, value);
        this.database.setValue(period, this.type, res);
        return res;
    }

    @Override
    public Value higher(StatPeriod period, Value value) {
        Value res = super.higher(period, value);
        this.database.setValue(period, this.type, res);
        return res;
    }

    @Override
    public Map<StatPeriod, Value> increaseAll(Value value) {
        Map<StatPeriod, Value> res = super.increaseAll(value);
        this.database.setValues(res, this.type);
        return res;
    }

    @Override
    public Map<StatPeriod, Value> higherAll(Value value) {
        Map<StatPeriod, Value> res = super.higherAll(value);
        this.database.setValues(res, this.type);
        return res;
    }
}
