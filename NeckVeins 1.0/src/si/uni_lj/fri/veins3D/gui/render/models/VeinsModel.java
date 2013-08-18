/* Author of this file: Simon Žagar, 2012, Ljubljana
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 * or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 */
package si.uni_lj.fri.veins3D.gui.render.models;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.math.Quaternion;
import si.uni_lj.fri.veins3D.math.Vector;
import si.uni_lj.fri.veins3D.utils.RayUtil;

/**
 * @author Simon Žagar
 * @since 0.2
 * @version 0.2
 */
public class VeinsModel {
	private final int APLICATION_SUBDIVISION_LIMIT = 3;

	protected ArrayList<Float> vertices;
	protected ArrayList<Mesh> meshes;
	public double centerx, centery, centerz;
	public float maxX, maxY, maxZ;
	public float minX, minY, minZ;
	private int numberOfSubdivisions = 0;
	private int maxSubDepth = 0;

	private Quaternion currentOrientation;
	private Quaternion addedOrientation;

	public double[] veinsGrabbedAt;
	public double veinsGrabRadius;

	public VeinsModel(String filepath) {
		constructVBOFromFile(filepath);
		setDefaultOrientation();
	}

	public void constructVBOFromFile(String filepath) {
		vertices = new ArrayList<Float>();
		centerx = 0;
		centery = 0;
		centerz = 0;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		float x, y, z;

		// meshes variables
		meshes = new ArrayList<Mesh>();
		ArrayList<Integer> tempFaces = new ArrayList<Integer>();
		ArrayList<String> groups = new ArrayList<String>();
		boolean newG = false;
		int tempFaceCount = 0;

		File file = new File(filepath);
		Scanner scanner;
		try {
			scanner = new Scanner(file);
			String type;
			String line;
			while (scanner.hasNext()) {
				line = scanner.nextLine();
				StringTokenizer strTokenizer = new StringTokenizer(line);
				type = strTokenizer.nextToken();
				if (type.equalsIgnoreCase("v")) {
					vertices.add(x = Float.parseFloat(strTokenizer.nextToken()));
					vertices.add(y = Float.parseFloat(strTokenizer.nextToken()));
					vertices.add(z = Float.parseFloat(strTokenizer.nextToken()));
					centerx += x;
					centery += y;
					centerz += z;
					if (x < minX)
						minX = x;
					if (y < minY)
						minY = y;
					if (z < minZ)
						minZ = z;
					if (x > maxX)
						maxX = x;
					if (y > maxY)
						maxY = y;
					if (z > maxZ)
						maxZ = z;
				} else if (type.equalsIgnoreCase("f")) {
					int a, b, c;
					StringTokenizer tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					a = Integer.parseInt(tok.nextToken());
					tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					b = Integer.parseInt(tok.nextToken());
					tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					c = Integer.parseInt(tok.nextToken());

					tempFaces.add(c);
					tempFaces.add(b);
					tempFaces.add(a);

					tempFaceCount++;
				} else if (type.equalsIgnoreCase("g")) {
					if (tempFaceCount > 0) {
						// It seems that since last starting a new group, there
						// have been faces stored
						// Here I create a new mesh
						Mesh mesh = new Mesh(groups, tempFaces, vertices);
						meshes.add(mesh);
						// After the whole file will be read, each mesh object's
						// faces will be stored as VBOs (Vertex Buffer Objects).
						System.out.println("Created a new mesh java object that will have it's own VBO.");
					} else if (newG)
						System.out.println("One \"g\" holding 0 elements discarted.");
					// start a new group
					newG = true;
					groups = new ArrayList<String>();
					tempFaces = new ArrayList<Integer>();
					tempFaceCount = 0;
					while (strTokenizer.hasMoreTokens()) {
						groups.add(strTokenizer.nextToken());
					}
				}
			}
			if (tempFaceCount > 0) {
				// It seems that since last starting a new group, there have
				// been faces stored
				// Here I create a new mesh
				Mesh mesh = new Mesh(groups, tempFaces, vertices);
				meshes.add(mesh);
				// After the whole file will be read, each mesh object's faces
				// will be stored as VBOs (Vertex Buffer Objects).
				System.out.println("Created a new mesh java object that will have it's own VBO.");
			} else {
				System.out.println("One \"g\" holding 0 elements discarted.");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// The file has been read.
		centerx /= (vertices.size() / 3);
		centery /= (vertices.size() / 3);
		centerz /= (vertices.size() / 3);

		for (Mesh mesh : meshes) {
			mesh.constructVBO();
		}
	}

	public void increaseSubdivisionDepth() {
		numberOfSubdivisions = Math.min(APLICATION_SUBDIVISION_LIMIT, numberOfSubdivisions + 1);
		if (maxSubDepth < numberOfSubdivisions) {
			for (Mesh mesh : meshes)
				mesh.increaseMaxSubdivision();
			maxSubDepth++;
		}
	}

	public void decreaseSubdivisionDepth() {
		numberOfSubdivisions = Math.max(0, numberOfSubdivisions - 1);
	}

	public void render() {
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();

		/* Apply orientation (add rotations) */
		Quaternion compositeOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
		FloatBuffer fb = compositeOrientation.getRotationMatrix(false);
		GL11.glMultMatrix(fb);

		/* Translate and render */
		glTranslatef(-(float) centerx, -(float) centery, -(float) centerz);
		for (Mesh vbo : meshes) {
			vbo.render(numberOfSubdivisions);
		}

		glPopMatrix();
	}

	/**
	 * Change added orientation (rotations) of the model
	 */
	public void changeAddedOrientation(VeinsRenderer renderer) {
		double[] veinsHeldAt = RayUtil.getRaySphereIntersection(Mouse.getX(), Mouse.getY(), renderer);
		if (veinsHeldAt != null) {
			double[] rotationAxis = Vector.crossProduct(veinsGrabbedAt, veinsHeldAt);
			if (Vector.length(rotationAxis) > 0) {
				rotationAxis = Vector.normalize(rotationAxis);
				rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);
				double angle = Math.acos(Vector.dotProduct(veinsGrabbedAt, veinsHeldAt)
						/ (Vector.length(veinsGrabbedAt) * Vector.length(veinsHeldAt)));
				addedOrientation = Quaternion.quaternionFromAngleAndRotationAxis(angle, rotationAxis);
			}
		}
	}

	private void setDefaultOrientation() {
		double angle1 = Math.toRadians(-90); // Math.PI * -90 / 180;
		double angle2 = Math.toRadians(180); // Math.PI * 180 / 180;
		currentOrientation = Quaternion.quaternionFromAngleAndRotationAxis(angle1, new double[] { 1, 0, 0 });
		double[] v = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(new double[] { 0, 1, 0 });
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation,
				Quaternion.quaternionFromAngleAndRotationAxis(angle2, v));
		addedOrientation = new Quaternion();
	}

	public void normalizeCurrentOrientation() {
		currentOrientation = Quaternion.quaternionNormalization(currentOrientation);
	}

	public void normalizeAddedOrientation() {
		addedOrientation = Quaternion.quaternionNormalization(addedOrientation);
	}

	/**
	 * Saves the current orientation of the model in currenOrientation
	 */
	public void saveCurrentOrientation() {
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
	}

	public void setCurrentOrientation(Quaternion q) {
		currentOrientation = q;
	}

	public void setAddedOrientation(Quaternion q) {
		addedOrientation = q;
	}

	public void rotateModel3D(double[] rot) {
		addedOrientation = Quaternion.quaternionFromAngleAndRotationAxis(-rot[0], new double[] { 1, 0, 0 });
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
		addedOrientation = Quaternion.quaternionFromAngleAndRotationAxis(-rot[1], new double[] { 0, 1, 0 });
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
		addedOrientation = Quaternion.quaternionFromAngleAndRotationAxis(-rot[2], new double[] { 0, 0, 1 });
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
	}

}
