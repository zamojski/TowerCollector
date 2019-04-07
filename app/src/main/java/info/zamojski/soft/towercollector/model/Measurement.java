/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Measurement implements Serializable {

    private static final long serialVersionUID = -1561708154666574202L;

    public static final float GPS_VALUE_NOT_AVAILABLE = 0.0f;

    /**
     * Measurement ID.
     */
    private int measurementId;
    /**
     * Geographic Latitude.
     */
    private double latitude;
    /**
     * Geographic Longitude.
     */
    private double longitude;
    /**
     * GPS Accuracy in m.
     * 0 - not available.
     */
    private float gpsAccuracy = GPS_VALUE_NOT_AVAILABLE;
    /**
     * GPS Speed in m/s.
     * 0 - not available.
     */
    private float gpsSpeed = GPS_VALUE_NOT_AVAILABLE;
    /**
     * GPS Bearing in degrees within range of (0-360].
     * 0 - not available.
     */
    private float gpsBearing = GPS_VALUE_NOT_AVAILABLE;
    /**
     * GPS Altitude in m.
     * 0 - not available.
     */
    private double gpsAltitude = GPS_VALUE_NOT_AVAILABLE;
    /**
     * Measured at Unix Timestamp with milliseconds.
     */
    private long measuredAt;
    /**
     * Uploaded to OCID Unix Timestamp with milliseconds.
     */
    private Long uploadedToOcidAt;
    /**
     * Uploaded to MLS Unix Timestamp with milliseconds.
     */
    private Long uploadedToMlsAt;
    /**
     * Associated cells.
     */
    private List<Cell> cells = new ArrayList<>();

    public int getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(int measurementId) {
        this.measurementId = measurementId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getGpsAccuracy() {
        return gpsAccuracy;
    }

    public void setGpsAccuracy(float gpsAccuracy) {
        this.gpsAccuracy = gpsAccuracy;
    }

    public float getGpsSpeed() {
        return gpsSpeed;
    }

    public void setGpsSpeed(float gpsSpeed) {
        this.gpsSpeed = gpsSpeed;
    }

    public float getGpsBearing() {
        return gpsBearing;
    }

    public void setGpsBearing(float gpsBearing) {
        this.gpsBearing = gpsBearing;
    }

    public double getGpsAltitude() {
        return gpsAltitude;
    }

    public void setGpsAltitude(double gpsAltitude) {
        this.gpsAltitude = gpsAltitude;
    }

    public long getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(long measuredAt) {
        this.measuredAt = measuredAt;
    }

    public Long getUploadedToOcidAt() {
        return uploadedToOcidAt;
    }

    public void setUploadedToOcidAt(Long uploadedToOcidAt) {
        this.uploadedToOcidAt = uploadedToOcidAt;
    }

    public Long getUploadedToMlsAt() {
        return uploadedToMlsAt;
    }

    public void setUploadedToMlsAt(Long uploadedToMlsAt) {
        this.uploadedToMlsAt = uploadedToMlsAt;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void addCell(Cell cell) {
        this.cells.add(cell);
    }

    public List<Cell> getMainCells() {
        List<Cell> mainCells = new ArrayList<>();
        for (Cell cell : cells) {
            if (!cell.isNeighboring())
                mainCells.add(cell);
        }
        if (mainCells.isEmpty())
            mainCells.add(cells.get(0)); // should never happen
        return mainCells;
    }

    public int getNeighboringCellsCount() {
        int count = 0;
        for (Cell cell : cells) {
            if (cell.isNeighboring())
                count++;
        }
        return count;
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "measurementId=" + measurementId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", gpsAccuracy=" + gpsAccuracy +
                ", gpsSpeed=" + gpsSpeed +
                ", gpsBearing=" + gpsBearing +
                ", gpsAltitude=" + gpsAltitude +
                ", measuredAt=" + measuredAt +
                ", uploadedToOcidAt=" + uploadedToOcidAt +
                ", uploadedToMlsAt=" + uploadedToMlsAt +
                ", cells=[" + TextUtils.join(", ", cells) + "]" +
                '}';
    }
}
