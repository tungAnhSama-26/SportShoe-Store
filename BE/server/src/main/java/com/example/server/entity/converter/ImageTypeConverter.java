package com.example.server.entity.converter;

import com.example.server.entity.enums.ImageType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ImageTypeConverter implements AttributeConverter<ImageType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ImageType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ImageType convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : ImageType.fromValue(dbData);
    }
}
