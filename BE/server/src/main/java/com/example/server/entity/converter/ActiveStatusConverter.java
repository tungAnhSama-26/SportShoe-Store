package com.example.server.entity.converter;

import com.example.server.entity.enums.ActiveStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActiveStatusConverter implements AttributeConverter<ActiveStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ActiveStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ActiveStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : ActiveStatus.fromValue(dbData);
    }
}
