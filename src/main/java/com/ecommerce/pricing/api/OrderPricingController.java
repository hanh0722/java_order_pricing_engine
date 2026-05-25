package com.ecommerce.pricing.api;

import com.ecommerce.pricing.application.OrderPricingApplicationService;
import com.ecommerce.pricing.application.dto.CalculateOrderRequest;
import com.ecommerce.pricing.application.dto.CalculateOrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderPricingController {

    private final OrderPricingApplicationService applicationService;

    public OrderPricingController(OrderPricingApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<CalculateOrderResponse> calculate(@Valid @RequestBody CalculateOrderRequest request) {
        return ResponseEntity.ok(applicationService.calculate(request));
    }
}
