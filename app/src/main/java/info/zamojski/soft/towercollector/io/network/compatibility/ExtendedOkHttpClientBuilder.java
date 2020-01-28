/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network.compatibility;

import android.os.Build;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import timber.log.Timber;

public class ExtendedOkHttpClientBuilder {

    public OkHttpClient.Builder newBuilder() {
        return enableTls12(new OkHttpClient().newBuilder());
    }

    /**
     * If on [Build.VERSION_CODES.LOLLIPOP] or lower, sets [OkHttpClient.Builder.sslSocketFactory] to an instance of
     * [Tls12SocketFactory] that wraps the default [SSLContext.getSocketFactory] for [TlsVersion.TLS_1_2].
     * Does nothing when called on [Build.VERSION_CODES.LOLLIPOP_MR1] or higher.
     * <p>
     * For some reason, Android supports TLS v1.2 from [Build.VERSION_CODES.JELLY_BEAN], but the spec only has it
     * enabled by default from API [Build.VERSION_CODES.KITKAT]. Furthermore, some devices on
     * [Build.VERSION_CODES.LOLLIPOP] don't have it enabled, despite the spec saying they should.
     *
     * @return the (potentially modified) [OkHttpClient.Builder]
     */
    private OkHttpClient.Builder enableTls12(OkHttpClient.Builder clientBuilder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                X509TrustManager trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];

                SSLContext sc = SSLContext.getInstance(TlsVersion.TLS_1_2.javaName());
                sc.init(null, new TrustManager[]{trustManager}, null);
                clientBuilder.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()), trustManager);

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(getModernTls12Spec());
                specs.add(getCompatibleTls12Spec());

                clientBuilder.connectionSpecs(specs);
            } catch (Exception ex) {
                Timber.e(ex, "enableTls12(): Error while setting TLS 1.2");
            }
        }
        return clientBuilder;
    }

    private ConnectionSpec getModernTls12Spec() {
        return new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();
    }

    private ConnectionSpec getCompatibleTls12Spec() {
        return new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();
    }
}
