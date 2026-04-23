package org.pancakelab.dto;

import java.util.UUID;

public record OrderInfo(UUID id, int building, int room) {}
