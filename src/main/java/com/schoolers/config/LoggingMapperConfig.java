package com.schoolers.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.schoolers.annotations.Censor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

@Configuration
public class LoggingMapperConfig {


    protected static class CensorSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (!Objects.isNull(value) && value instanceof String str) {
                gen.writeString("*".repeat(str.length()));
                return;
            }
            gen.writeString("********");
        }
    }

    protected static class CensorBeanSerializerModifier extends BeanSerializerModifier {

        private final transient JsonSerializer<Object> censorSerializer = new CensorSerializer();

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                         BeanDescription beanDesc,
                                                         List<BeanPropertyWriter> beanProperties) {
            for (BeanPropertyWriter writer : beanProperties) {
                if (writer.getMember().hasAnnotation(Censor.class)) {
                    writer.assignSerializer(censorSerializer);
                }
            }
            return beanProperties;
        }
    }

    @Bean("loggingMapper")
    public ObjectMapper loggingMapper() {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new CensorBeanSerializerModifier());
        return JsonMapper.builder()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build()
                .findAndRegisterModules()
                .registerModule(module)
                .setTimeZone(TimeZone.getDefault());
    }
}
