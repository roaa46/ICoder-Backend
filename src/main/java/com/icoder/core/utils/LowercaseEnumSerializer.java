package com.icoder.core.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class LowercaseEnumSerializer extends JsonSerializer<Enum> {

    @Override
    public void serialize(Enum anEnum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (anEnum != null)
            jsonGenerator.writeString(anEnum.name().toLowerCase());
    }
}