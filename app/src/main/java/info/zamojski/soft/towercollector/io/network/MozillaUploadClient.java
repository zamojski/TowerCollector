/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import timber.log.Timber;


public class MozillaUploadClient extends ClientBase implements IUploadClient {

    private final String uploadUrl;

    public MozillaUploadClient(String url, String apiKey) {
        this.uploadUrl = String.format(url, apiKey);
    }

    @Override
    public RequestResult uploadMeasurements(String content) {
        Timber.d("uploadMeasurements(): Sending post request");
        try {
            HttpRequest request = HttpRequest.post(uploadUrl)
                    .followRedirects(false)
                    .connectTimeout(CONN_TIMEOUT)
                    .readTimeout(READ_TIMEOUT);
            // add json as request content
            request.header("Content-Type", "application/json");
            request.send(content);
            return handleResponse(request.code(), request.body());
        } catch (HttpRequestException ex) {
            Timber.e(ex, "uploadMeasurements(): Errors encountered");
            reportExceptionWithSuppress(ex);
            return RequestResult.Failure;
        }
    }

    private RequestResult handleResponse(int code, String body) {
        if (code == 200) {
            return RequestResult.Success;
        }
        if (code >= 500 && code <= 599) {
            return RequestResult.ServerError;
        }
        if (code == 400 || code == 403) {
            RuntimeException ex = new RequestException(body);
            reportException(ex);
            return RequestResult.ConfigurationError;
        }
        // don't report captive portals
        if (code != 302) {
            RuntimeException ex = new RequestException(body);
            reportException(ex);
        }
        return RequestResult.ConnectionError;
    }
}
