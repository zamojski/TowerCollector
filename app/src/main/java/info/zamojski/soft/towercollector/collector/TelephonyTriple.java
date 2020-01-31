/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class TelephonyTriple {
    private final TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private Object cellInfoUpdateRequestCallback; // object to reuse the type on lower api TelephonyManager.CellInfoCallback on which it's always null

    public TelephonyTriple(TelephonyManager telephonyManager) {
        this.telephonyManager = telephonyManager;
    }

    public TelephonyManager getTelephonyManager() {
        return telephonyManager;
    }

    public PhoneStateListener getPhoneStateListener() {
        return phoneStateListener;
    }

    public void setPhoneStateListener(PhoneStateListener phoneStateListener) {
        this.phoneStateListener = phoneStateListener;
    }

    public Object getCellInfoUpdateRequestCallback() {
        return cellInfoUpdateRequestCallback;
    }

    public void setCellInfoUpdateRequestCallback(Object cellInfoUpdateRequestCallback) {
        this.cellInfoUpdateRequestCallback = cellInfoUpdateRequestCallback;
    }
}
