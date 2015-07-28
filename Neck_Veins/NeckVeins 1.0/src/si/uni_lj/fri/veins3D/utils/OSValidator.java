package si.uni_lj.fri.veins3D.utils;

// Detects operating system version (windows,linux,max)
public class OSValidator {
 
	private static String OS = System.getProperty("os.name").toLowerCase();
 
	public static final String GetOsString()
	{
		return OS;
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}
 
	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0); 
	}
 
	public static boolean isUnix() {
 		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}
 
	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}
 
}