package com.ecommerce.pricing.infrastructure.persistence.adapter;

import com.ecommerce.pricing.domain.model.OrderItem;
import com.ecommerce.pricing.domain.model.PricingRequest;
import com.ecommerce.pricing.domain.model.PricingResult;
import com.ecommerce.pricing.domain.port.OrderPersistencePort;
import com.ecommerce.pricing.infrastructure.persistence.entity.OrderEntity;
import com.ecommerce.pricing.infrastructure.persistence.entity.OrderItemEntity;
import com.ecommerce.pricing.infrastructure.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderPersistenceAdapter implements OrderPersistencePort {

    private final OrderJpaRepository orderRepository;

    public OrderPersistenceAdapter(OrderJpaRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Long saveCalculatedOrder(PricingRequest request, PricingResult result) {
        OrderEntity order = new OrderEntity();
        order.setCustomerType(request.customerType().name());
        order.setSubtotal(result.subtotal());
        order.setDiscount(result.discount());
        order.setFinalPrice(result.finalPrice());
        order.setCouponCode(request.couponCode());

        for (OrderItem item : request.items()) {
            OrderItemEntity entity = new OrderItemEntity();
            entity.setSku(item.sku());
            entity.setUnitPrice(item.unitPrice());
            entity.setQuantity(item.quantity());
            entity.setLineTotal(item.lineTotal());
            order.addItem(entity);
        }

        return orderRepository.save(order).getId();
    }
}
