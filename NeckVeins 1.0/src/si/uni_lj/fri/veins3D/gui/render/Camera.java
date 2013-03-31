package si.uni_lj.fri.veins3D.gui.render;

import si.uni_lj.fri.veins3D.math.Quaternion;

public class Camera {
	public static final float CAMERA_MOVE_SPEED = 1.667f;
	public static final double CAMERA_ROTATION_SPEED = (float) (72 * Math.PI / 180 / 60);
	private final double[] X_POSITIVE_AXIS = { 1, 0, 0 };
	private final double[] X_NEGATIVE_AXIS = { -1, 0, 0 };
	private final double[] Y_POSITIVE_AXIS = { 0, 1, 0 };
	private final double[] Y_NEGATIVE_AXIS = { 0, -1, 0 };
	private final double[] Z_POSITIVE_AXIS = { 0, 0, 1 };
	private final double[] Z_NEGATIVE_AXIS = { 0, 0, -1 };

	public float cameraX;
	public float cameraY;
	public float cameraZ;
	public Quaternion cameraOrientation;

	public Camera() {
		cameraX = 0;
		cameraY = 0;
		cameraZ = 0;
		cameraOrientation = new Quaternion();
	}

	public void zoomIn() {
		cameraX *= 0.8;
		cameraY *= 0.8;
		cameraZ *= 0.8;
	}

	public void zoomOut() {
		cameraX *= 1.25;
		cameraY *= 1.25;
		cameraZ *= 1.25;
	}

	public void lookUp() {
		// create a vector representing the rotation axis
		Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(CAMERA_ROTATION_SPEED, X_POSITIVE_AXIS);
		cameraOrientation = Quaternion.quaternionMultiplication(cameraOrientation, addRotation);

	}

	public void lookDown() {
		// create a vector representing the rotation axis
		Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(CAMERA_ROTATION_SPEED, X_NEGATIVE_AXIS);
		cameraOrientation = Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
	}

	public void lookRight() {
		// create a vector representing the rotation axis
		Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(CAMERA_ROTATION_SPEED, Y_POSITIVE_AXIS);
		cameraOrientation = Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
	}

	public void lookLeft() {
		// create a vector representing the rotation axis
		Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(CAMERA_ROTATION_SPEED, Y_NEGATIVE_AXIS);
		cameraOrientation = Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
	}

	public void rotateCounterClockwise() {
		// create a vector representing the rotation axis
		Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(CAMERA_ROTATION_SPEED, Z_POSITIVE_AXIS);
		cameraOrientation = Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
	}

	public void rotateClockwise() {
		// create a vector representing the rotation axis
		Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(CAMERA_ROTATION_SPEED, Z_NEGATIVE_AXIS);
		cameraOrientation = Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
	}

	public void rotate(float upRotation, float rightRotation) {
		// add rotation up
		Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED
				* upRotation, X_POSITIVE_AXIS);
		cameraOrientation = Quaternion.quaternionMultiplication(cameraOrientation, addRotation);

		// add rotation in right
		addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED * rightRotation,
				Y_NEGATIVE_AXIS);
		cameraOrientation = Quaternion.quaternionMultiplication(cameraOrientation, addRotation);
	}

	public void move(float moveUp, float moveRight) {
		double up[] = new double[] { 0, 0, -moveUp };
		up = cameraOrientation.rotateVector3d(up);
		double right[] = new double[] { moveRight, 0, 0 };
		right = cameraOrientation.rotateVector3d(right);
		cameraX += (float) (up[0] + right[0]);
		cameraY += (float) (up[1] + right[1]);
		cameraZ += (float) (up[2] + right[2]);
	}

	public void moveForward() {
		double rotateVector[] = cameraOrientation.rotateVector3d(Z_NEGATIVE_AXIS);
		cameraX += (float) rotateVector[0];
		cameraY += (float) rotateVector[1];
		cameraZ += (float) rotateVector[2];
	}

	public void moveBackwards() {
		double rotateVector[] = cameraOrientation.rotateVector3d(Z_POSITIVE_AXIS);
		cameraX += (float) rotateVector[0];
		cameraY += (float) rotateVector[1];
		cameraZ += (float) rotateVector[2];
	}

	public void moveRight() {
		double rotateVector[] = cameraOrientation.rotateVector3d(X_POSITIVE_AXIS);
		cameraX += (float) rotateVector[0];
		cameraY += (float) rotateVector[1];
		cameraZ += (float) rotateVector[2];
	}

	public void moveLeft() {
		double rotateVector[] = cameraOrientation.rotateVector3d(X_NEGATIVE_AXIS);
		cameraX += (float) rotateVector[0];
		cameraY += (float) rotateVector[1];
		cameraZ += (float) rotateVector[2];

	}

	public void moveUp() {
		double rotateVector[] = cameraOrientation.rotateVector3d(Y_POSITIVE_AXIS);
		cameraX += (float) rotateVector[0];
		cameraY += (float) rotateVector[1];
		cameraZ += (float) rotateVector[2];
	}

	public void moveDown() {
		double rotateVector[] = cameraOrientation.rotateVector3d(Y_NEGATIVE_AXIS);
		cameraX += (float) rotateVector[0];
		cameraY += (float) rotateVector[1];
		cameraZ += (float) rotateVector[2];
	}

}
