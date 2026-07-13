package com.shike.ordering.service.merchant;

import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.dto.merchant.MerchantOrderQueryDTO;
import com.shike.ordering.vo.common.OrderDetailVO;
import com.shike.ordering.vo.common.OrderSummaryVO;
import com.shike.ordering.vo.merchant.DashboardVO;

public interface MerchantOrderService {
    PageResult<OrderSummaryVO> list(MerchantOrderQueryDTO query);
    OrderDetailVO detail(Long id);
    DashboardVO dashboard();
    void accept(Long id);
    void reject(Long id, String reason);
    void startCooking(Long id);
    void readyForPickup(Long id);
    void startDelivery(Long id);
    void markDelivered(Long id);
    void complete(Long id);
}
