package smsbot;

/**
 * Created by shohei on 3/13/16.
 */
public class OSUtils {
    private static String OS = null;

    public static String getOsName()
    {
        if(OS == null) { OS = System.getProperty("os.name"); }
        return OS;
    }

    public static boolean isWindows()
    {
        return getOsName().startsWith("Windows");
    }

    public static boolean is32bit(){
        if(System.getProperty("sun.arch.data.model").equals(32)){
            return true;
        }else{
            return false;
        }
    }

}
