/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files;

public class DeviceOperationException extends Exception {

    private static final long serialVersionUID = 5110297137703144265L;

    private Reason reason;

    public DeviceOperationException(String message, Reason reason) {
        super(message + ": caused by " + reason);
        this.reason = reason;
    }

    public Reason getReason() {
        return this.reason;
    }

    public enum Reason {
        Unknown, LocationNotExists, LocationNotWritable, DeviceNotWritable
    }
}
