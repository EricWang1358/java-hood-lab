package lab.immut;

import java.util.ArrayList;
import java.util.List;

public class MutableExposeList {
    static class Config {
        private final List<String> roles;
        Config(List<String> roles) { this.roles = roles; }
        List<String> getRoles() { return roles; }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int intact = 0;
        for (int i = 0; i < n; i++) {
            List<String> src = new ArrayList<>();
            src.add("USER");
            Config cfg = new Config(src);
            src.add("ADMIN");
            cfg.getRoles().clear();
            if (cfg.getRoles().size() == 1 && "USER".equals(cfg.getRoles().get(0))) intact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("intact=" + intact);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
