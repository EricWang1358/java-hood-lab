package com.javahood.seckill.engine;

public interface PreDeductEngine {
    void initCampaignStock(Long campaignId, int totalStock);

    PreDeductResult tryPreDeduct(Long campaignId, Long userId);

    void rollback(Long campaignId, Long userId, int shardIndex);

    /**
     * Restores 1 unit of stock back to the sharded pool AFTER an unpaid PENDING order is closed on timeout,
     * allowing other buyers to claim it while keeping the timed-out user marked to prevent malicious hoarding.
     */
    void restoreStockAfterClose(Long campaignId);

    int remainingStock(Long campaignId);
}
