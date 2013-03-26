package main;

import tools.Vector;

public class RayUtil {

	/**
	 * TODO Veins radius, double[] screenPlaneInitialUpperLeft, double[]
	 * screenPlaneInitialLowerLeft, double[] screenPlaneInitialLowerRight
	 * 
	 * @param x
	 * @param y
	 * @param cam
	 * @param veinsRadius
	 * @return
	 */
	public static double[] getRaySphereIntersection(int x, int y, Camera cam, double veinsRadius) {
		// figure out if the click on the screen intersects the circle that
		// surrounds the veins model

		// get the direction of the "ray" cast from the camera location
		double[] d = getRayDirection(x, y, cam, new double[1], new double[1], new double[1]);

		// a vector representing the camera location
		double[] e = new double[] { cam.cameraX, cam.cameraY, cam.cameraZ };

		// the location of the sphere is the zero vector
		double[] c = new double[3];
		// partial calculations
		double[] eSc = Vector.subtraction(e, c);
		double dDPeSc = Vector.dotProduct(d, eSc);
		double discriminant = dDPeSc * dDPeSc - Vector.dotProduct(d, d)
				* (Vector.dotProduct(eSc, eSc) - veinsRadius * veinsRadius);

		// in this case the mouse is not pressed near the veins sphere
		if (discriminant < 0) {
			return null;
			// in this case we hold the mouse on the sphere surrounding the
			// veins model in some way
		} else {
			// partial calculation
			double[] Sd = Vector.subtraction(new double[3], d);
			// t1 and t2 are the parameter values for vor "ray" expression e+t*d
			double t1 = (Vector.dotProduct(Sd, eSc) + Math.sqrt(discriminant)) / Vector.dotProduct(d, d);
			double t2 = (Vector.dotProduct(Sd, eSc) - Math.sqrt(discriminant)) / Vector.dotProduct(d, d);

			if (t2 < 0)
				return Vector.sum(e, Vector.vScale(d, t1));
			else
				return Vector.sum(e, Vector.vScale(d, t2));
		}
	}

	/**
	 * TODO screenPlane
	 * 
	 * @param x
	 * @param y
	 * @param cam
	 * @return
	 */
	public static double[] getRayDirection(int x, int y, Camera cam, double[] screenPlaneInitialUpperLeft,
			double[] screenPlaneInitialLowerLeft, double[] screenPlaneInitialLowerRight) {
		double[] tempUpperLeft = cam.cameraOrientation.rotateVector3d(screenPlaneInitialUpperLeft);
		double[] tempLowerLeft = cam.cameraOrientation.rotateVector3d(screenPlaneInitialLowerLeft);
		double[] tempLowerRight = cam.cameraOrientation.rotateVector3d(screenPlaneInitialLowerRight);

		double[] leftToRight = Vector.subtraction(tempLowerRight, tempLowerLeft);
		leftToRight = Vector.vScale(leftToRight, (0.5d + x) / (double) MainFrameRefactored.settings.resWidth);
		double[] rayD = Vector.sum(tempLowerLeft, leftToRight);

		double[] downToUp = Vector.subtraction(tempUpperLeft, tempLowerLeft);
		downToUp = Vector.vScale(downToUp, (0.5d + y) / (double) MainFrameRefactored.settings.resHeight);

		rayD = Vector.sum(rayD, downToUp);

		return rayD;
	}

}
