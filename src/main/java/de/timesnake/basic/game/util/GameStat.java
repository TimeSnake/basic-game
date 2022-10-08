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
