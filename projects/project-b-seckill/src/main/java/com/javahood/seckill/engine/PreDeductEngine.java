package com.javahood.seckill.engine;

public interface PreDeductEngine {
    void initCampaignStock(Long campaignId, int totalStock);

    PreDeductResult tryPreDeduct(Long campaignId, Long userId);

    void rollback(Long campaignId, Long userId, int shardIndex);

    int remainingStock(Long campaignId);
}
