/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.filesystem;

public class ReadResult<T> {
    private final ReadResultType resultType;
    private String errorMessage;
    private T value;

    public ReadResult(ReadResultType resultType) {
        this.resultType = resultType;
    }

    public ReadResult(ReadResultType resultType, String errorMessage) {
        this.resultType = resultType;
        this.errorMessage = errorMessage;
    }

    public ReadResult(ReadResultType resultType, T value) {
        this.resultType = resultType;
        this.value = value;
    }

    public ReadResultType getResultType() {
        return resultType;
    }

    public T getValue() {
        return value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
