package com.kavindu.ecommerce.kafka;

import com.kavindu.ecommerce.customer.CustomerResponse;
import com.kavindu.ecommerce.order.PaymentMethod;
import com.kavindu.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}
