package com.payment.utils;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;

public class RequestBuilderCarrier implements io.opentracing.propagation.TextMap {
    private final GetMethod builder;

    public RequestBuilderCarrier(GetMethod builder) {
        this.builder = builder;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("carrier is write-only");
    }

    @Override
    public void put(String key, String value) {
        builder.addRequestHeader(key, value);
    }
}
