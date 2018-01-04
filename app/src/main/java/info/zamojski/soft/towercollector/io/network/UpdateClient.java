/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import com.github.kevinsawicki.http.HttpRequest;

import info.zamojski.soft.towercollector.utils.StringUtils;
import trikita.log.Log;

public class UpdateClient extends ClientBase {
    private static final String TAG = UpdateClient.class.getSimpleName();

    private String url;

    public UpdateClient(String url) {
        this.url = url;
    }

    public String fetchUpdates() {
        Log.d("fetchUpdates(): Sending get request");
        try {
            HttpRequest request = HttpRequest.get(url)
                    .followRedirects(false)
                    .connectTimeout(CONN_TIMEOUT)
                    .readTimeout(READ_TIMEOUT);
            return handleResponse(request.code(), request.body());
        } catch (HttpRequest.HttpRequestException ex) {
            Log.d("fetchUpdates(): Errors encountered", ex);
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
