package com.kavindu.ecommerce.order;

import com.kavindu.ecommerce.customer.CustomerClient;
import com.kavindu.ecommerce.exception.BussinessException;
import com.kavindu.ecommerce.kafka.OrderConfirmation;
import com.kavindu.ecommerce.kafka.OrderProducer;
import com.kavindu.ecommerce.orderline.OrderLineRequest;
import com.kavindu.ecommerce.orderline.OrderLineService;
import com.kavindu.ecommerce.payment.PaymentClient;
import com.kavindu.ecommerce.payment.PaymentRequest;
import com.kavindu.ecommerce.product.ProductClient;
import com.kavindu.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final PaymentClient paymentClient;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;



    public OrderService(OrderRepository orderRepository, CustomerClient customerClient, ProductClient productClient, OrderMapper orderMapper, PaymentClient paymentClient, OrderLineService orderLineService, OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
        this.productClient = productClient;
        this.orderMapper = orderMapper;
        this.paymentClient = paymentClient;
        this.orderLineService = orderLineService;
        this.orderProducer = orderProducer;
    }

    @Transactional
    public Integer CreateOrder(OrderRequest orderRequest) {

        var customer=customerClient.findCustomerById(orderRequest.customerId())
                .orElseThrow(()->new BussinessException("Cannot create order:: no customer with provide ID"));

        var purchaseProducts=productClient.purchaseProducts(orderRequest.products());

        var order=orderRepository.save(orderMapper.toOder(orderRequest));

        for (PurchaseRequest purchaseRequest : orderRequest.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        var paymentRequest = new PaymentRequest(
                orderRequest.amount(),
                orderRequest.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        orderRequest.reference(),
                        orderRequest.amount(),
                        orderRequest.paymentMethod(),
                        customer,
                        purchaseProducts
                )
        );
        return order.getId();

    }

    public OrderResponse findById(Integer orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", orderId)));
    }

    public List<OrderResponse> findAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::fromOrder)
                .collect(Collectors.toList());
    }
}
