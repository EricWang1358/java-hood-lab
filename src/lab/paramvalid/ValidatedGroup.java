package lab.paramvalid;

public class ValidatedGroup {
    static void validateDto(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("MethodArgumentNotValidException");
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int rejected = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                validateDto(-100);
            } catch (IllegalArgumentException e) {
                // 不写 BindingResult，交由 @RestControllerAdvice 统一拦截返回 400
                rejected++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("rejected=" + rejected);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
