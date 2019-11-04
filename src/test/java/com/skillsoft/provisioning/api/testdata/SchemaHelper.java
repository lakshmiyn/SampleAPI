package com.skillsoft.provisioning.api.testdata;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class SchemaHelper {
    public static String loadSchema(String baseName) throws IOException {
        if (!baseName.contains(".")) {
            baseName += ".json";
        }
        final String schemaPath = String.format("schema/%s", baseName);
        final InputStream is = SchemaHelper.class.getClassLoader().getResourceAsStream(schemaPath);
        return IOUtils.toString(is);
    }
}
