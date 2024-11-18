package org.kitodo.data.database.converter;

import java.io.IOException;
import java.util.Map;
import javax.persistence.AttributeConverter;

import org.codehaus.jackson.map.ObjectMapper;

public class TaskDataConverter implements AttributeConverter<Map<String, Object>, String> {
    @Override
    public String convertToDatabaseColumn(Map<String, Object> taskData) {

        String jsonString = null;

        try {
            jsonString = new ObjectMapper().writeValueAsString(taskData);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String taskDataJsonString) {

        Map<String, Object> taskDataObject = null;

        try {
            taskDataObject = new ObjectMapper().readValue(taskDataJsonString, Map.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return taskDataObject;
    }
}
