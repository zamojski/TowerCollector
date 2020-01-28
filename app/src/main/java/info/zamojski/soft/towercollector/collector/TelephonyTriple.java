/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class TelephonyTriple {
    private final TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager.CellInfoCallback cellInfoUpdateRequestCallback;

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

    public TelephonyManager.CellInfoCallback getCellInfoUpdateRequestCallback() {
        return cellInfoUpdateRequestCallback;
    }

    public void setCellInfoUpdateRequestCallback(TelephonyManager.CellInfoCallback cellInfoUpdateRequestCallback) {
        this.cellInfoUpdateRequestCallback = cellInfoUpdateRequestCallback;
    }
}
