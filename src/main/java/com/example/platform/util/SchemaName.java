package com.example.platform.util;

import java.util.UUID;

public final class SchemaName {
    private SchemaName() {}

    public static String from(UUID projectId) {
        return "p_" + projectId.toString().replace("-", "");
    }
}
