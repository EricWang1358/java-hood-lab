package com.javahood.seckill.engine;

public record PreDeductResult(Status status, int shardIndex) {

    public enum Status {
        SUCCESS,
        SOLD_OUT,
        ALREADY_CLAIMED
    }

    public static PreDeductResult success(int shardIndex) {
        return new PreDeductResult(Status.SUCCESS, shardIndex);
    }

    public static PreDeductResult soldOut() {
        return new PreDeductResult(Status.SOLD_OUT, -1);
    }

    public static PreDeductResult alreadyClaimed() {
        return new PreDeductResult(Status.ALREADY_CLAIMED, -1);
    }
}
