package com.kavindu.ecommerce.order;

import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public Order toOder(OrderRequest orderRequest) {
        return null;
    }

    public OrderResponse fromOrder(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getReference(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getCustomerId()
        );
    }
}
