package main;

import models.VeinsModel;
import tools.Quaternion;

public class ModelLoaderUtil {

	/**
	 * @since 0.4
	 * @version 0.4
	 */
	public static void loadModel(String fileName, VeinsRenderer renderer) {
		double fovy = renderer.fovy;
		VeinsModel veinsModel = new VeinsModel(fileName);
		// Calculate the appropriate camera distance:
		// The following code takes the most extreme values on each coordinate
		// of all the specified vertices in the .obj file.
		// It uses the bigger distance (of two) from the average location on
		// each axis
		// to calculate the radius of a circle that would surely enclose every
		// vertex,
		// although allowing the radius to be slightly bigger than necessary.
		double d1 = veinsModel.minX - veinsModel.centerx;
		double d2 = veinsModel.maxX - veinsModel.centerx;
		double d3 = veinsModel.minY - veinsModel.centery;
		double d4 = veinsModel.maxY - veinsModel.centery;
		double d5 = veinsModel.minZ - veinsModel.centerz;
		double d6 = veinsModel.maxZ - veinsModel.centerz;
		d1 *= d1;
		d2 *= d2;
		d3 *= d3;
		d4 *= d4;
		d5 *= d5;
		d6 *= d6;
		d1 = Math.max(d1, d2);
		d2 = Math.max(d3, d4);
		d3 = Math.max(d5, d6);
		d1 = Math.sqrt(Math.max(Math.max(d1 + d2, d2 + d3), d1 + d3));

		// The smaller angle of view of the horizontal and
		// vertical ones.
		double fovMin;

		if (VeinsWindow.settings.resWidth < VeinsWindow.settings.resHeight)
			fovMin = fovy * VeinsWindow.settings.resWidth / (double) VeinsWindow.settings.resHeight;
		else
			fovMin = fovy;
		fovMin = Math.PI * fovMin / 180;// To radians.
		double veinsRadius = d1 / Math.sqrt(2);
		float cameraZ = (float) (d1 / Math.tan(fovMin / 2));
		float cameraX = 0;
		float cameraY = 0;
		Quaternion cameraOrientation = new Quaternion();

		double yAngle = Math.PI * fovy / 360d;
		double xAngle = yAngle * (double) VeinsWindow.settings.resWidth / (double) VeinsWindow.settings.resHeight;
		double screenPlaneZ = -d1 / Math.tan(fovMin / 2);
		double screenPlaneY = Math.tan(yAngle) * (-screenPlaneZ);
		double screenPlaneX = Math.tan(xAngle) * (-screenPlaneZ);
		double[] screenPlaneInitialUpperLeft = new double[] { -screenPlaneX, screenPlaneY, screenPlaneZ };
		double[] screenPlaneInitialUpperRight = new double[] { screenPlaneX, screenPlaneY, screenPlaneZ };
		double[] screenPlaneInitialLowerLeft = new double[] { -screenPlaneX, -screenPlaneY, screenPlaneZ };
		double[] screenPlaneInitialLowerRight = new double[] { screenPlaneX, -screenPlaneY, screenPlaneZ };

		renderer.veinsGrabbedAt = null;
		double angle1 = Math.PI * -90 / 180;
		double angle2 = Math.PI * 180 / 180;
		Quaternion currentModelOrientation = Quaternion.quaternionFromAngleAndRotationAxis(angle1, new double[] { 1, 0,
				0 });
		double[] v = Quaternion.quaternionReciprocal(currentModelOrientation).rotateVector3d(new double[] { 0, 1, 0 });
		currentModelOrientation = Quaternion.quaternionMultiplication(currentModelOrientation,
				Quaternion.quaternionFromAngleAndRotationAxis(angle2, v));
		Quaternion addedModelOrientation = new Quaternion();

		/* Set the renderer */
		renderer.setVeinsModel(veinsModel);
		renderer.veinsRadius = veinsRadius;
		renderer.screenPlaneInitialUpperLeft = screenPlaneInitialUpperLeft;
		renderer.screenPlaneInitialUpperRight = screenPlaneInitialUpperRight;
		renderer.screenPlaneInitialLowerLeft = screenPlaneInitialLowerLeft;
		renderer.screenPlaneInitialLowerRight = screenPlaneInitialLowerRight;
		renderer.setCurrentModelOrientation(currentModelOrientation);
		renderer.setAddedModelOrientation(addedModelOrientation);
		renderer.getCamera().cameraX = cameraX;
		renderer.getCamera().cameraY = cameraY;
		renderer.getCamera().cameraZ = cameraZ;
		renderer.getCamera().cameraOrientation = cameraOrientation;

	}
}
