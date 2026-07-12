package com.shike.ordering.common.exception;

public final class OrderStateConflictException extends BusinessException {
    public OrderStateConflictException() {
        super(ErrorCode.ORDER_STATE_CONFLICT);
    }
}
