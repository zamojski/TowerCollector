/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.filesystem;

public class WriteResult {
    private final WriteResultType resultType;
    private String errorMessage;

    public WriteResult(WriteResultType resultType) {
        this.resultType = resultType;
    }

    public WriteResult(WriteResultType resultType, String errorMessage) {
        this.resultType = resultType;
        this.errorMessage = errorMessage;
    }

    public WriteResultType getResultType() {
        return resultType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
