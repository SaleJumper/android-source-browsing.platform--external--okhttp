/*
 * Copyright (C) 2012 Square, Inc.
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.okhttp.internal;

import dalvik.system.SocketTagger;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.net.ssl.SSLSocket;
import com.android.org.conscrypt.OpenSSLSocketImpl;

/**
 * Access to proprietary Android APIs. Doesn't use reflection.
 */
public final class Platform {
    private static final Platform PLATFORM = new Platform();

    public static Platform get() {
        return PLATFORM;
    }

    public void logW(String warning) {
        System.logW(warning);
    }

    public void tagSocket(Socket socket) throws SocketException {
        SocketTagger.get().tag(socket);
    }

    public void untagSocket(Socket socket) throws SocketException {
        SocketTagger.get().untag(socket);
    }

    public URI toUriLenient(URL url) throws URISyntaxException {
        return url.toURILenient();
    }

    public void enableTlsExtensions(SSLSocket socket, String uriHost) {
        if (socket instanceof OpenSSLSocketImpl) {
            OpenSSLSocketImpl openSSLSocket = (OpenSSLSocketImpl) socket;
            openSSLSocket.setUseSessionTickets(true);
            openSSLSocket.setHostname(uriHost);
        }
    }

    public void supportTlsIntolerantServer(SSLSocket socket) {
        socket.setEnabledProtocols(new String[]{"SSLv3"});
    }

    /**
     * Returns the negotiated protocol, or null if no protocol was negotiated.
     */
    public byte[] getNpnSelectedProtocol(SSLSocket socket) {
        return socket instanceof OpenSSLSocketImpl
                ? ((OpenSSLSocketImpl) socket).getNpnSelectedProtocol()
                : null;
    }

    /**
     * Sets client-supported protocols on a socket to send to a server. The
     * protocols are only sent if the socket implementation supports NPN.
     */
    public void setNpnProtocols(SSLSocket socket, byte[] npnProtocols) {
        if (socket instanceof OpenSSLSocketImpl) {
            ((OpenSSLSocketImpl) socket).setNpnProtocols(npnProtocols);
        }
    }

    /**
     * Returns a deflater output stream that supports SYNC_FLUSH for SPDY name
     * value blocks. This throws an {@link UnsupportedOperationException} on
     * Java 6 and earlier where there is no built-in API to do SYNC_FLUSH.
     */
    public OutputStream newDeflaterOutputStream(
            OutputStream out, Deflater deflater, boolean syncFlush) {
        return new DeflaterOutputStream(out, deflater, syncFlush);
    }
}
