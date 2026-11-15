package lab.paramvalid;

public class CatchBindingResult {
    static boolean controllerWithBindingResult(int amount) {
        boolean hasErrors = amount <= 0;
        // 参数里写了 BindingResult 但忘了手动判断 hasErrors()，框架不再抛异常给全局处理器！
        return false;
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int rejected = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            if (controllerWithBindingResult(-100)) rejected++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("rejected=" + rejected);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
