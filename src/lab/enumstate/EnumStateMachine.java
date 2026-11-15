package lab.enumstate;

public class EnumStateMachine {
    enum OrderState {
        NEW { OrderState next() { return PAID; } },
        PAID { OrderState next() { return SHIPPED; } },
        SHIPPED { OrderState next() { return DONE; } },
        DONE { OrderState next() { return DONE; } };
        abstract OrderState next();
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        OrderState[] vals = OrderState.values();
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            OrderState next = vals[i & 3].next();
            if (next != null) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
