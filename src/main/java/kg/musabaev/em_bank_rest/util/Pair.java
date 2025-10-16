package kg.musabaev.em_bank_rest.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonSerialize(using = Pair.PairSerializer.class)
public record Pair<V>(String key, V value) {
    public static <V> Pair<V> of(String key, V value) {
        return new Pair<>(key, value);
    }

    static class PairSerializer extends JsonSerializer<Pair<?>> {
        @Override
        public void serialize(Pair<?> pair, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField(String.valueOf(pair.key()), pair.value());
            gen.writeEndObject();
        }
    }
}
