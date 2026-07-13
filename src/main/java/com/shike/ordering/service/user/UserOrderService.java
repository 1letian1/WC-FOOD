package com.shike.ordering.service.user;

import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.dto.user.OrderQueryDTO;
import com.shike.ordering.vo.common.OrderDetailVO;
import com.shike.ordering.vo.common.OrderSummaryVO;

public interface UserOrderService {
    PageResult<OrderSummaryVO> list(OrderQueryDTO query);
    OrderDetailVO detail(Long id);
    void cancel(Long id);
    void confirmReceipt(Long id);
}
