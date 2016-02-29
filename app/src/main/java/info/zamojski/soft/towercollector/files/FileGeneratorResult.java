/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files;

import info.zamojski.soft.towercollector.enums.GeneratorResult;
import info.zamojski.soft.towercollector.files.DeviceOperationException.Reason;

public class FileGeneratorResult {

    private GeneratorResult result;
    private Reason reason;
    private String message;

    public FileGeneratorResult(GeneratorResult result, Reason reason) {
        this(result, reason, "");
    }

    public FileGeneratorResult(GeneratorResult result, Reason reason, String message) {
        this.result = result;
        this.reason = reason;
        this.message = message;
    }

    public GeneratorResult getResult() {
        return result;
    }

    public Reason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("%s [ Result = `%s`, Reason = `%s`, Message = `%s` ]", FileGeneratorResult.class.getSimpleName(), result, reason, message);
    }
}
