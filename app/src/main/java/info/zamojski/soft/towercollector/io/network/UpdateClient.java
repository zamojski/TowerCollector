/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import com.github.kevinsawicki.http.HttpRequest;

import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class UpdateClient extends ClientBase {

    private String url;

    public UpdateClient(String url) {
        this.url = url;
    }

    public String fetchUpdates() {
        Timber.d("fetchUpdates(): Sending get request");
        try {
            HttpRequest request = HttpRequest.get(url)
                    .followRedirects(false)
                    .connectTimeout(CONN_TIMEOUT)
                    .readTimeout(READ_TIMEOUT);
            return handleResponse(request.code(), request.body());
        } catch (HttpRequest.HttpRequestException ex) {
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
