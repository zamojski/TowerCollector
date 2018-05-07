/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import info.zamojski.soft.towercollector.utils.StringUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class UpdateClient extends ClientBase {

    private String url;

    public UpdateClient(String url) {
        this.url = url;
    }

    public String fetchUpdates() {
        Timber.d("fetchUpdates(): Sending get request");
        try {
            OkHttpClient client = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(CONN_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            return handleResponse(response.code(), response.body().string());
        } catch (IOException ex) {
            Timber.d(ex, "fetchUpdates(): Errors encountered");
            reportExceptionWithSuppress(ex);
            return null;
        }
    }

    private String handleResponse(int code, String body) {
        if (code == 200 && !StringUtils.isNullEmptyOrWhitespace(body))
            return body;
        return null;
    }
}
