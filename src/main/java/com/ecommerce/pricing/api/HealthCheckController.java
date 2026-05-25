package com.ecommerce.pricing.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.pricing.api.dto.BaseResponse;

@Controller
@RequestMapping("/health")
public class HealthCheckController {
    
    @GetMapping()
    public ResponseEntity<BaseResponse> getHealthCheck() {
        return ResponseEntity.ok(new BaseResponse("Successfully"));
    }
}
