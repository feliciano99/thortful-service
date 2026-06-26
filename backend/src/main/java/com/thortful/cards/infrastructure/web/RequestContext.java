package com.thortful.cards.infrastructure.web;

public final class RequestContext {

    public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    private RequestContext() {
    }

    public static String currentRequestId() {
        return REQUEST_ID.isBound() ? REQUEST_ID.get() : null;
    }
}
