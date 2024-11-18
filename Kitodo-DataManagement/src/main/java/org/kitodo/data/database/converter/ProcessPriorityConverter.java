package org.kitodo.data.database.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.kitodo.data.database.enums.ProcessPriority;

@Converter
public class ProcessPriorityConverter implements AttributeConverter<ProcessPriority, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProcessPriority processPriority) {
        return processPriority.getValue();
    }

    @Override
    public ProcessPriority convertToEntityAttribute(Integer integer) {
        return ProcessPriority.getPriorityFromValue(integer);
    }
}
