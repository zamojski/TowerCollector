/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.telephony.TelephonyManager;

import androidx.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.enums.NetworkGroup;

public class NetworkTypeUtils {

    public static int getNetworkGroupNameResId(NetworkGroup networkType) {
        switch (networkType) {
            case Cdma:
                return R.string.network_group_cdma;
            case Gsm:
                return R.string.network_group_gsm;
            case Wcdma:
                return R.string.network_group_umts;
            case Lte:
                return R.string.network_group_lte;
            case Nr:
                return R.string.network_group_nr;
            case Tdscdma:
                return R.string.network_group_tdscdma;
            default:
                return R.string.network_group_unknown;
        }
    }

    public static NetworkGroup getNetworkGroup(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return NetworkGroup.Gsm;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NetworkGroup.Wcdma;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return NetworkGroup.Cdma;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NetworkGroup.Lte;
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return NetworkGroup.Tdscdma;
            case TelephonyManager.NETWORK_TYPE_NR:
                return NetworkGroup.Nr;
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
            default:
                return NetworkGroup.Unknown;
        }
    }

    public static @DrawableRes
    int getNetworkGroupIcon(NetworkGroup networkType) {
        switch (networkType) {
            case Cdma:
                return R.drawable.dot_cdma;
            case Gsm:
                return R.drawable.dot_gsm;
            case Wcdma:
                return R.drawable.dot_wcdma;
            case Lte:
                return R.drawable.dot_lte;
            case Nr:
                return R.drawable.dot_nr;
            case Tdscdma:
                return R.drawable.dot_tdscdma;
            default:
                return R.drawable.dot_unknown;
        }
    }

    public static @DrawableRes
    int getNetworkGroupIcon(NetworkGroup firstNetworkType, NetworkGroup secondNetworkType) {
        if (firstNetworkType == secondNetworkType || firstNetworkType == NetworkGroup.Cdma || firstNetworkType == NetworkGroup.Tdscdma)
            return getNetworkGroupIcon(firstNetworkType);

        List<NetworkGroup> networkTypes = new ArrayList<>();
        networkTypes.add(firstNetworkType);
        networkTypes.add(secondNetworkType);
        if (networkTypes.contains(NetworkGroup.Gsm)) {
            if (networkTypes.contains(NetworkGroup.Wcdma)) {
                return R.drawable.dot_wcdma_gsm;
            } else if (networkTypes.contains(NetworkGroup.Lte)) {
                return R.drawable.dot_lte_gsm;
            } else if (networkTypes.contains(NetworkGroup.Nr)) {
                return R.drawable.dot_nr_gsm;
            } else {
                return R.drawable.dot_gsm;
            }
        } else if (networkTypes.contains(NetworkGroup.Wcdma)) {
            if (networkTypes.contains(NetworkGroup.Lte)) {
                return R.drawable.dot_lte_wcdma;
            } else if (networkTypes.contains(NetworkGroup.Nr)) {
                return R.drawable.dot_nr_wcdma;
            } else {
                return R.drawable.dot_wcdma;
            }
        } else if (networkTypes.contains(NetworkGroup.Lte)) {
            if (networkTypes.contains(NetworkGroup.Nr)) {
                return R.drawable.dot_nr_lte;
            } else {
                return R.drawable.dot_lte;
            }
        } else if (networkTypes.contains(NetworkGroup.Nr)) {
            return R.drawable.dot_nr;
        }

        return getNetworkGroupIcon(secondNetworkType);
    }
}
