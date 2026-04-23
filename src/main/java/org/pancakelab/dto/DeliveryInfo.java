package org.pancakelab.dto;

import java.util.List;
import java.util.UUID;

public record DeliveryInfo(UUID orderId, int building, int room, List<String> pancakes) {}
