package com.kavindu.ecommerce.payment;

import com.kavindu.ecommerce.customer.CustomerResponse;
import com.kavindu.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
