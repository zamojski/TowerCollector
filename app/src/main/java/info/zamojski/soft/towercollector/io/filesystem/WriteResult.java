/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.filesystem;

import android.net.Uri;

public class WriteResult {
    private final WriteResultType resultType;
    private Uri filePath;
    private String errorMessage;

    public WriteResult(WriteResultType resultType, Uri filePath) {
        this.resultType = resultType;
        this.filePath = filePath;
    }

    public WriteResult(WriteResultType resultType, Uri filePath, String errorMessage) {
        this.resultType = resultType;
        this.filePath = filePath;
        this.errorMessage = errorMessage;
    }

    public WriteResultType getResultType() {
        return resultType;
    }

    public Uri getFilePath() {
        return filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
