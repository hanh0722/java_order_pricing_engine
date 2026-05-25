package com.ecommerce.pricing.domain.port;

import com.ecommerce.pricing.domain.model.PricingRequest;
import com.ecommerce.pricing.domain.model.PricingResult;

public interface OrderPersistencePort {

    Long saveCalculatedOrder(PricingRequest request, PricingResult result);
}
