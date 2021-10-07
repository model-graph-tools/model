package org.wildfly.modelgraph;

import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.jackson.ObjectMapperCustomizer;

@Singleton
public class MgtObjectMapper implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.PUBLIC_ONLY);
    }
}
