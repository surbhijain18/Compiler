package cop5556sp18;
import java.lang.Math;
public class RuntimeFunctions {

	public static String className = "cop5556sp18/RuntimeFunctions";
	
	
	public static final String logSig = "(D)D";
	public static double log(double arg0) {
		double l =  Math.round(Math.log(arg0)); //correct?
		return l;
	}
	
	public static String absSig_int = "(I)I"; 
	public static int abs_int(int arg0) {
		return (int)Math.abs(arg0);
	}
	
	public static String absSig_ft = "(F)F"; 
	public static float abs_ft(float arg0) {
		return (float)Math.abs(arg0);
	}
	
	
	
	public static String sinSig = "(D)D";
	public static double sin(double arg0) {
		double res = Math.sin(arg0);
		return res;
	}
	
	public static String cosSig = "(D)D";
	public static double cos(double val) {
		double res = Math.cos(val);
		return res;
	}
	
	public static String atanSig = "(D)D";
	public static double atan(double val) {
		double res = Math.atan(val);
		return res;
	}
	
	public static String cart_xSig = "(DD)D";
	public static double cart_x(double r, double theta) {
	     double y = r * Math.cos(theta);
	     return  y;
	}
	
	public static String cart_ySig = "(DD)D";
	public static double cart_y(double r, double theta) {
		double y =  r * Math.sin(theta);
		return  y;
	}
	
	public static String polar_aSig = "(DD)D";
	public static double polar_a(double x, double y) {
		double  a=  Math.atan2(y, x);
		return  a;
	}

	public static String polar_rSig = "(DD)D";
	public static double polar_r(double x, double y) {
		double  r = Math.hypot(x,y);
		return r;
	}
	
	public static String power = "(DD)D";
	public static double power(double x, double y) {
		double  r =  Math.pow(x,y);
		return r;
	}
}
