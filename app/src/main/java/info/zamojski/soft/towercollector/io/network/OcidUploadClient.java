/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import info.zamojski.soft.towercollector.io.network.compatibility.ExtendedOkHttpClientBuilder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class OcidUploadClient extends ClientBase implements IUploadClient {

    private static final MediaType CSV = MediaType.parse("text/csv");

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
        return uploadMeasurementsEncrypted(content);
    }

    private RequestResult uploadMeasurementsEncrypted(String content) {
        Timber.d("uploadMeasurementsEncrypted(): Sending encrypted post request");
        return uploadMeasurementsCommon(new ExtendedOkHttpClientBuilder().newBuilder(), url, content);
    }

    private RequestResult uploadMeasurementsCommon(OkHttpClient.Builder clientBuilder, String url, String content) {
        try {
            OkHttpClient client = clientBuilder
                    .connectTimeout(CONN_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", apiKey)
                    .addFormDataPart("appId", appId)
                    .addFormDataPart("datafile", "TowerCollector_measurements_" + System.currentTimeMillis() + ".csv", RequestBody.create(CSV, content))
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            return handleResponse(response.code(), response.body().string());
        } catch (SocketTimeoutException | ConnectException ex) {
            Timber.d(ex, "uploadMeasurements(): Timeout encountered");
            return RequestResult.ConnectionError;
        } catch (IOException ex) {
            Timber.d(ex, "uploadMeasurements(): Errors encountered");
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
        if (code == 401 || code == 403 || "Err: Invalid token".equalsIgnoreCase(body)) {
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
