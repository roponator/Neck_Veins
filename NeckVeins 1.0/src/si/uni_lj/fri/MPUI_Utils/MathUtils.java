package si.uni_lj.fri.MPUI_Utils;


public class MathUtils {
	
	public static float weight(Point3f p, Point3f c, float r){
		float t = p.getDistance(c)*1.5f/r;
	    if (t < 0.5)
	       return -t*t + 0.75f;
	    else
	        return (0.5f*(1.5f-t)*(1.5f-t));

	}
	
	public static float[] backSubstitution(float[][] u, float[] w, float[][] v, int m, int n, float[] b)
	{
		float[] x = new float[n], tmp = new float[n];
		int jj,j,i;
		float s;

		for (j = 0;j < n;j++) {
			s = 0.0f;
			if (w[j] !=  0) {
				for (i=0; i<m; i++) s += u[i][j]*b[i];
				s /= w[j];
			}
			tmp[j] = s;
		}
		for (j = 0; j < n; j++) {
			s = 0.0f;
			for ( jj =0 ; jj < n; jj++) s += v[j][jj]*tmp[jj];
			x[j]=s;
		}
		
		return x;
	}
	
}
