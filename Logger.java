public abstract class Logger {
    public static final boolean mustLog = true;

    public static void log(Object s){
        if (mustLog) System.err.println(s.toString());
    }
}