package io.github.goodees.ese.store.subscription.support;

import com.fasterxml.jackson.databind.ObjectMapper;

class JacksonSerialization {
    static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }
}
