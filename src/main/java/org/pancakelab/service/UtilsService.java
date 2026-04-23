package org.pancakelab.service;

import java.util.UUID;

public class UtilsService {
    public static void validateUuid(UUID id, String label) {
        if (id == null) {
            throw new IllegalArgumentException(label + " must not be null");
        }
    }
}
