package com.javahood.seckill.controller;

import com.javahood.seckill.domain.CouponCampaign;
import com.javahood.seckill.domain.SeckillOrder;
import com.javahood.seckill.domain.UserCoupon;
import com.javahood.seckill.repository.SeckillOrderRepository;
import com.javahood.seckill.repository.UserCouponRepository;
import com.javahood.seckill.service.CampaignQueryService;
import com.javahood.seckill.service.OrderStateService;
import com.javahood.seckill.service.SeckillService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    public record CreateCampaignRequest(@NotBlank String title, @Min(1) int stock) {
    }

    public record ClaimRequest(@NotNull Long userId) {
    }

    private final SeckillService seckillService;
    private final CampaignQueryService campaignQueryService;
    private final OrderStateService orderStateService;
    private final SeckillOrderRepository orderRepository;
    private final UserCouponRepository userCouponRepository;

    public SeckillController(SeckillService seckillService,
                             CampaignQueryService campaignQueryService,
                             OrderStateService orderStateService,
                             SeckillOrderRepository orderRepository,
                             UserCouponRepository userCouponRepository) {
        this.seckillService = seckillService;
        this.campaignQueryService = campaignQueryService;
        this.orderStateService = orderStateService;
        this.orderRepository = orderRepository;
        this.userCouponRepository = userCouponRepository;
    }

    @PostMapping("/campaigns")
    public CouponCampaign createCampaign(@Valid @RequestBody CreateCampaignRequest req) {
        return seckillService.createCampaign(req.title(), req.stock());
    }

    @GetMapping("/campaigns/{campaignId}")
    public ResponseEntity<CampaignQueryService.CampaignDetailVO> getCampaign(@PathVariable Long campaignId) {
        return campaignQueryService.getCampaignDetail(campaignId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/campaigns/{campaignId}/claim")
    public ResponseEntity<SeckillService.ClaimResponse> claim(@PathVariable Long campaignId,
                                                              @Valid @RequestBody ClaimRequest req) {
        SeckillService.ClaimResponse resp = seckillService.claimOptimized(campaignId, req.userId());
        return switch (resp.outcome()) {
            case ACCEPTED -> ResponseEntity.accepted().body(resp);
            case BACKPRESSURE_REJECTED -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(resp);
            case ALREADY_CLAIMED, SOLD_OUT -> ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        };
    }

    @PostMapping("/orders/{orderNo}/pay")
    public ResponseEntity<Map<String, Object>> pay(@PathVariable String orderNo) {
        boolean ok = orderStateService.markPaid(orderNo);
        return ResponseEntity.ok(Map.of("orderNo", orderNo, "paid", ok));
    }

    @PostMapping("/orders/{orderNo}/close")
    public ResponseEntity<Map<String, Object>> close(@PathVariable String orderNo) {
        boolean ok = orderStateService.closeTimeout(orderNo);
        return ResponseEntity.ok(Map.of("orderNo", orderNo, "closed", ok));
    }

    @GetMapping("/orders/{orderNo}")
    public ResponseEntity<SeckillOrder> getOrder(@PathVariable String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/coupons")
    public List<UserCoupon> listUserCoupons(@RequestParam Long userId) {
        return userCouponRepository.findByUserIdOrderByIdDesc(userId);
    }
}
