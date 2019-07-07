package csu.liutao.ffmpegmgr;

public class FfmpegMgr {

    static {
        System.loadLibrary("native-lib");
    }

    private static final FfmpegMgr ourInstance = new FfmpegMgr();

    public static FfmpegMgr getInstance() {
        return ourInstance;
    }

    private FfmpegMgr() {
    }

    public native String getStringFromNative(int order);

}
