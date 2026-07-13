package com.shike.ordering.service.user;

import com.shike.ordering.dto.user.CartItemAddDTO;
import com.shike.ordering.dto.user.CartItemQuantityDTO;
import com.shike.ordering.vo.user.CartVO;

public interface CartService {
    CartVO getCart();
    CartVO add(CartItemAddDTO request);
    CartVO updateQuantity(Long id, CartItemQuantityDTO request);
    void delete(Long id);
    void clear();
}
