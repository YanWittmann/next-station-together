package de.yanwittmann.nextstation.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class TextureProviderAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!TextureProvider.class.isAssignableFrom(type.getRawType())) {
            return null;
        }

        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, T value) {
                final JsonElement jsonElement = delegate.toJsonTree(value);
                final JsonObject jsonObject = jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : new JsonObject();

                if (value instanceof TextureProvider) {
                    final TextureProvider textureProvider = (TextureProvider) value;
                    jsonObject.addProperty("texture", textureProvider.getTextureHash());
                }

                gson.toJson(jsonObject, out);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                return delegate.read(in);
            }
        };
    }
}

