/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import java.io.EOFException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import info.zamojski.soft.towercollector.MyApplication;

import org.acra.ACRA;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import javax.net.ssl.SSLHandshakeException;

import trikita.log.Log;

public class NetworkHelper {
    private static final String TAG = NetworkHelper.class.getSimpleName();

    public static final int CONN_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 30000;

    public static ResponseData sendGet(String url) {
        Log.d("sendGet(): Sending get request");

        try {
            HttpRequest request = HttpRequest.get(url)
                    .followRedirects(false)
                    .connectTimeout(CONN_TIMEOUT)
                    .readTimeout(READ_TIMEOUT);
            return new ResponseData(request.code(), request.body());
        } catch (HttpRequestException ex) {
            Log.e("sendGet(): Errors encountered", ex);
            return processException(ex);
        }
    }

    public static ResponseData sendPost(String url, String appId, String apiKey, String csvContent) {
        Log.d("sendPost(): Sending post request");

        try {
            HttpRequest request = HttpRequest.post(url)
                    .followRedirects(false)
                    .connectTimeout(CONN_TIMEOUT)
                    .readTimeout(READ_TIMEOUT);
            // add API key
            request.part("key", apiKey);
            // add app name
            request.part("appId", appId);
            // add file data as bytes
            request.part("datafile", "TowerCollector_measurements_" + System.currentTimeMillis() + ".csv", "text/csv", csvContent);
            return new ResponseData(request.code(), request.body());
        } catch (HttpRequestException ex) {
            Log.e("sendPost(): Errors encountered", ex);
            return processException(ex);
        }
    }

    private static ResponseData processException(Exception ex) {
        Throwable originalException = ex.getCause();
        if (!(originalException instanceof UnknownHostException)
                && !(originalException instanceof SocketTimeoutException)
                && !(originalException instanceof SocketException)
                && !(originalException instanceof EOFException)
                && !(originalException instanceof SSLHandshakeException)) {
            // known exceptions suppressed
            MyApplication.getAnalytics().sendException(ex, Boolean.FALSE);
            ACRA.getErrorReporter().handleSilentException(ex);
        }
        return new ResponseData();
    }

    public static class ResponseData {
        private int code;
        private String content = "";

        public ResponseData(int code, String content) {
            this();
            this.code = code;
            this.content = content;
        }

        public ResponseData() {
        }

        public int getCode() {
            return code;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "ResponseData [code=" + code + ", content=" + content + "]";
        }
    }
}
