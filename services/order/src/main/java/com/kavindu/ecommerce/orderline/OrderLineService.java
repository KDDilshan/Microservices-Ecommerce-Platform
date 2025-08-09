package com.kavindu.ecommerce.orderline;

import com.kavindu.ecommerce.order.OrderMapper;
import com.kavindu.ecommerce.order.OrderRepository;
import com.kavindu.ecommerce.order.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderLineService {

  private final OrderLineRepository orderLineRepository;
  private final OrderLineMapper orderLineMapper;

    public OrderLineService(OrderLineRepository orderLineRepository, OrderLineMapper orderLineMapper) {
        this.orderLineRepository = orderLineRepository;
        this.orderLineMapper = orderLineMapper;
    }

    public Integer saveOrderLine(OrderLineRequest request) {
        var order = orderLineMapper.toOrderLine(request);
        return orderLineRepository.save(order).getId();
    }



    public List<OrderLineResponse> findAllByOrderId(Integer orderId) {
        return orderLineRepository.findAllByOrderId(orderId)
                .stream()
                .map(orderLineMapper::toOderLineResponse)
                .collect(Collectors.toList());
    }
}
