package com.example.demo.util;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class JsonSchemaValidator {

    //if the subject is invalid, ValidationException is thrown
    public void validate(String  subjectString ) throws ValidationException {
        JSONObject jsonSubject = new JSONObject(subjectString);

        try (InputStream inputStream = getClass().getResourceAsStream("/schema.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(jsonSubject); // throws a ValidationException if this object is invalid
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
