package dev.sora.protohax.relay;

public final class Listener {
    public static boolean netease = false;
    public static boolean cn = false;

    public static final boolean getCN(){
        return Listener.cn;
    }

    public static void setNetease(boolean netease) {
        Listener.netease = netease;
    }

    public static void setCn(boolean cn) {
        Listener.cn = cn;
    }

    public static boolean getNetease() {
        return netease;
    }
}
