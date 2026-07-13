package com.shike.ordering.service.user;

import com.shike.ordering.dto.user.OrderCreateDTO;
import com.shike.ordering.vo.user.OrderCreateVO;

public interface OrderCreationService {
    OrderCreateVO create(OrderCreateDTO request, String idempotencyKey);
}
