// Copyright 2015 The Project Buendia Authors
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at: http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distrib-
// uted under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
// OR CONDITIONS OF ANY KIND, either express or implied.  See the License for
// specific language governing permissions and limitations under the License.

package org.projectbuendia.client.sync;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.common.collect.ObjectArrays;

import net.sqlcipher.database.SQLiteDatabase;

/** Constructs and executes SQL queries. */
public class QueryBuilder {
    String mTable;
    String mCondition = "1";
    String[] mArgs = {};
    String mOrderBy = null;
    String mGroupBy = null;

    public QueryBuilder(String table) {
        mTable = table;
    }

    /** Adds a SQL condition to the WHERE clause (joined using AND). */
    public QueryBuilder where(String sqlCondition, String... args) {
        if (sqlCondition != null && !sqlCondition.isEmpty()) {
            mCondition += " and (" + sqlCondition + ")";
            mArgs = args == null ? mArgs : ObjectArrays.concat(mArgs, args, String.class);
        }
        return this;
    }

    /** Sets the sort key in the ORDER BY clause. */
    public QueryBuilder orderBy(String key) {
        mOrderBy = key;
        return this;
    }

    /** Sets the group key in the GROUP BY clause. */
    public QueryBuilder groupBy(String key) {
        mGroupBy = key;
        return this;
    }

    /** Executes a SELECT query. */
    public Cursor select(SQLiteDatabase db, String... columns) {
        return db.query(mTable, columns, mCondition, mArgs, mGroupBy, null, mOrderBy, null);
    }

    /** Executes an UPDATE query. */
    public int update(SQLiteDatabase db, ContentValues values) {
        return db.update(mTable, values, mCondition, mArgs);
    }

    /** Executes a DELETE query. */
    public int delete(SQLiteDatabase db) {
        return db.delete(mTable, mCondition, mArgs);
    }
}
