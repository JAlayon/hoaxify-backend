package com.alayon.hoaxify.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class SerializationConfiguration {

	@Bean
	public Module springDataPageModule() {
		final JsonSerializer<Page> pageSerializer = new JsonSerializer<Page>() {
			@Override
			public void serialize(final Page value, final JsonGenerator gen, final SerializerProvider serializers)
					throws IOException {
				gen.writeStartObject();
				gen.writeNumberField("numberOfElements", value.getNumberOfElements());
				gen.writeNumberField("totalElements", value.getTotalElements());
				gen.writeNumberField("totalPages", value.getTotalPages());
				gen.writeNumberField("number", value.getNumber());
				gen.writeNumberField("size", value.getSize());
				gen.writeBooleanField("first", value.isFirst());
				gen.writeBooleanField("last", value.isLast());
				gen.writeBooleanField("next", value.hasNext());
				gen.writeBooleanField("previous", value.hasPrevious());

				gen.writeFieldName("content");
				serializers.defaultSerializeValue(value.getContent(), gen);
				gen.writeEndObject();
			}
		};
		return new SimpleModule().addSerializer(Page.class, pageSerializer);
	}
}
