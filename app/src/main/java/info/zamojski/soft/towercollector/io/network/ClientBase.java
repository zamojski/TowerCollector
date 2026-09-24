/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.network;

import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import info.zamojski.soft.towercollector.MyApplication;

public abstract class ClientBase {

    protected static final int CONN_TIMEOUT = 30000;
    protected static final int READ_TIMEOUT = 30000;

    protected void reportExceptionWithSuppress(IOException ex) {
        // suppress known exceptions
        if (isSuppressedRecursively(ex)) {
            return;
        }
        reportException(ex);
    }

    protected void reportException(Exception ex) {
        MyApplication.handleSilentException(ex);
    }

    private boolean isSuppressedRecursively(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        if (isSuppressed(throwable)) {
            return true;
        }
        for (Throwable suppressed : throwable.getSuppressed()) {
            if (isSuppressedRecursively(suppressed)) {
                return true;
            }
        }
        return isSuppressedRecursively(throwable.getCause());
    }

    private boolean isSuppressed(Throwable throwable) {
        if (throwable instanceof UnknownHostException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof SocketException
                || throwable instanceof SSLException
                || throwable instanceof EOFException
                || throwable instanceof ProtocolException) {
            return true;
        }
        String message = throwable.getMessage();
        if (message != null) {
            return message.contains("Unexpected response code for CONNECT")
                    || message.contains("Required SETTINGS preface not received");
        }
        return false;
    }
}
