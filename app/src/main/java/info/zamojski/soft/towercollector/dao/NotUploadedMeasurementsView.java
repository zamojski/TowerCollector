/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

public class NotUploadedMeasurementsView implements ITable {

    static final String VIEW_NAME = "not_uploaded_measurements";

    private static final String QUERY_DROP_VIEW = "DROP VIEW IF EXISTS " + VIEW_NAME;

    static final String QUERY_CREATE_VIEW = "CREATE VIEW " + VIEW_NAME + " AS "
            + "SELECT * FROM " + MeasurementsTable.TABLE_NAME + " WHERE " + MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT + " IS NULL AND " + MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT + " IS NULL";

    @Override
    public String[] getCreateQueries() {
        return new String[]{
                QUERY_DROP_VIEW,
                QUERY_CREATE_VIEW
        };
    }
}
