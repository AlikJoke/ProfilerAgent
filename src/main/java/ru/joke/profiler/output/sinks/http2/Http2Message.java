package ru.joke.profiler.output.sinks.http2;

import org.apache.hc.core5.http.Header;

final class Http2Message {

    private final byte[] data;
    private final Header[] headers;

    Http2Message(final byte[] data, final Header[] headers) {
        this.data = data;
        this.headers = headers;
    }

    byte[] data() {
        return data;
    }

    Header[] headers() {
        return headers;
    }
}
