/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import com.github.kevinsawicki.http.HttpRequest;

import trikita.log.Log;

public class OcidUploadClient extends ClientBase implements IUploadClient {
    private static final String TAG = OcidUploadClient.class.getSimpleName();

    private String url;
    private String appId;
    private String apiKey;

    public OcidUploadClient(String url, String appId, String apiKey) {
        this.url = url;
        this.appId = appId;
        this.apiKey = apiKey;
    }

    @Override
    public RequestResult uploadMeasurements(String content) {
        Log.d("uploadMeasurements(): Sending post request");
        try {
            HttpRequest request = HttpRequest.post(url)
                    .followRedirects(false)
                    .connectTimeout(CONN_TIMEOUT)
                    .readTimeout(READ_TIMEOUT);
            // add API key
            request.part("key", apiKey);
            // add app name
            request.part("appId", appId);
            // add file data as file
            request.part("datafile", "TowerCollector_measurements_" + System.currentTimeMillis() + ".csv", "text/csv", content);
            return handleResponse(request.code(), request.body());
        } catch (HttpRequest.HttpRequestException ex) {
            Log.d("uploadMeasurements(): Errors encountered", ex);
            reportExceptionWithSuppress(ex);
            return RequestResult.Failure;
        }
    }

    private RequestResult handleResponse(int code, String body) {
        body = (body == null ? "" : body.trim());

        if (code == 200 && "0,OK".equalsIgnoreCase(body)) {
            return RequestResult.Success;
        }
        if (code >= 500 && code <= 599) {
            return RequestResult.ServerError;
        }
        if (code == 401 || code == 403 || "Error: Authorization failed. Check your API key.".equalsIgnoreCase(body)) {
            RuntimeException ex = new RequestException(body);
            reportException(ex);
            return RequestResult.InvalidApiKey;
        }
        if (code == 400) {
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
