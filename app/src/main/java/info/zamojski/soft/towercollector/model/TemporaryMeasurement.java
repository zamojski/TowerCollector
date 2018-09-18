/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

public class TemporaryMeasurement extends Measurement {

    /**
     * Unix Timestamp with milliseconds.
     */
    private long uploadedToOcidAt;

    /**
     * Unix Timestamp with milliseconds.
     */
    private long uploadedToMlsAt;

    public long getUploadedToOcidAt() {
        return uploadedToOcidAt;
    }

    public void setUploadedToOcidAt(long uploadedToOcidAt) {
        this.uploadedToOcidAt = uploadedToOcidAt;
    }

    public long getUploadedToMlsAt() {
        return uploadedToMlsAt;
    }

    public void setUploadedToMlsAt(long uploadedToMlsAt) {
        this.uploadedToMlsAt = uploadedToMlsAt;
    }
}
