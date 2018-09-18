/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import org.acra.ACRA;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import info.zamojski.soft.towercollector.MyApplication;

public abstract class ClientBase {

    protected static final int CONN_TIMEOUT = 30000;
    protected static final int READ_TIMEOUT = 30000;

    protected void reportExceptionWithSuppress(IOException ex) {
        Throwable originalException = ex.getCause();
        // suppress known exceptions
        if (isSuppressed(ex) || isSuppressed(originalException)) {
            return;
        }
        reportException(ex);
    }

    protected void reportException(Exception ex) {
        MyApplication.getAnalytics().sendException(ex, Boolean.FALSE);
        ACRA.getErrorReporter().handleSilentException(ex);
    }

    private boolean isSuppressed(Throwable throwable) {
        return (throwable instanceof UnknownHostException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof SocketException
                || throwable instanceof SSLProtocolException
                || throwable instanceof SSLHandshakeException
                || throwable instanceof EOFException);
    }
}
