/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class UpgradeScript5 implements IUpgradeScript {

    private static final String TAG = UpgradeScript5.class.getSimpleName();

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Log.d(TAG, "performUpgrade(): Upgrading db to version 5");
        // create new table
        database.execSQL("CREATE TABLE operators (row_id INTEGER PRIMARY KEY NOT NULL, name VARCHAR(50) NOT NULL, UNIQUE (name) ON CONFLICT IGNORE)");
        // add new column to measurements
        database.execSQL("ALTER TABLE measurements ADD COLUMN operator_id INTEGER NULL REFERENCES operators(row_id)");
    }

}
