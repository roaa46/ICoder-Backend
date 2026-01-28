package com.icoder.core.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

public class UppercaseEnumDeserializer extends JsonDeserializer<Enum> implements ContextualDeserializer {

    private Class<? extends Enum> targetClass;

    public UppercaseEnumDeserializer() {}

    public UppercaseEnumDeserializer(Class<? extends Enum> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public Enum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String value = jsonParser.getText().toUpperCase();
        return Enum.valueOf(targetClass, value);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        Class<? extends Enum> rawClass = (Class<? extends Enum>) beanProperty.getType().getRawClass();
        return new UppercaseEnumDeserializer(rawClass);
    }
}
