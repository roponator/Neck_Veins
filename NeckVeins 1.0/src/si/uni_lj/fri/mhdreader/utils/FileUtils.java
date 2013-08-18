package si.uni_lj.fri.mhdreader.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import si.uni_lj.fri.mhdreader.MHDReader;
import si.uni_lj.fri.mhdreader.utils.obj.Coordinates;
import si.uni_lj.fri.mhdreader.utils.obj.Normal;
import si.uni_lj.fri.mhdreader.utils.obj.Triangle;
import si.uni_lj.fri.mhdreader.utils.obj.Vertex;
import si.uni_lj.fri.veins3D.gui.VeinsWindow;

public class FileUtils {

	public static String loadText(String name) {
		if (!name.endsWith(".cls")) {
			name += ".cls";
		}
		BufferedReader br = null;
		String resultString = null;
		try {
			br = new BufferedReader(new InputStreamReader(VeinsWindow.class.getResourceAsStream(name)));
			String line = null;
			StringBuilder result = new StringBuilder();
			while ((line = br.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			resultString = result.toString();
		} catch (NullPointerException npe) {
			System.err.println("Error retrieving OpenCL source file: ");
			npe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Error reading OpenCL source file: ");
			ioe.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException ex) {
				System.err.println("Error closing OpenCL source file");
				ex.printStackTrace();
			}
		}

		return resultString;
	}

	public static float[] readFile(String fileName) {
		MHDReader.readMHD(fileName);
		System.out.println("Reading raw file " + fileName + "...");
		File file = new File(MHDReader.rawFile);
		byte[] fileData = new byte[(int) file.length()];
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		ByteBuffer buffer = ByteBuffer.wrap(fileData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		ShortBuffer shorts = buffer.asShortBuffer();
		float[] floatCTMatrix = new float[shorts.capacity()];
		for (int i = 0; i < shorts.capacity(); i++) {
			floatCTMatrix[i] = shorts.get(i);
		}
		return floatCTMatrix;
	}

	public static void writeFile(String fileName, IntBuffer nTrianglesBuff, FloatBuffer trianglesBuff,
			FloatBuffer normalsBuff) {
		long startTime = System.currentTimeMillis();
		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
		System.out.println("Number of triagles: " + nTrianglesBuff.get(0));

		System.out.println("Packing for writing to file...");
		int index = 1;
		int normalIndex = 1;
		LinkedHashMap<Coordinates, Vertex> vertices = new LinkedHashMap<Coordinates, Vertex>(nTrianglesBuff.get(0) / 10);
		LinkedHashMap<Coordinates, Normal> normals = new LinkedHashMap<Coordinates, Normal>(nTrianglesBuff.get(0) / 10);
		Vertex[] triangleVertices = new Vertex[3];
		for (int i = 0; i < nTrianglesBuff.get(0) * 9; i += 9) {
			for (int j = 0; j < 3; j++) {
				float x = trianglesBuff.get(i + j * 3);
				float y = trianglesBuff.get(i + j * 3 + 1);
				float z = trianglesBuff.get(i + j * 3 + 2);
				Coordinates key = new Coordinates(x, y, z);
				if (!vertices.containsKey(key)) {
					triangleVertices[j] = new Vertex(x, y, z, index++);
					vertices.put(key, triangleVertices[j]);
				} else {
					triangleVertices[j] = vertices.get(key);
				}

				float nx = normalsBuff.get(i + j * 3);
				float ny = normalsBuff.get(i + j * 3 + 1);
				float nz = normalsBuff.get(i + j * 3 + 2);
				key = new Coordinates(x, y, z);
				if (!normals.containsKey(key)) {
					normals.put(key, new Normal(nx, ny, nz, normalIndex));
					triangleVertices[j].normalIndex = normalIndex++;
				} else {
					triangleVertices[j].normalIndex = normalIndex;
				}
			}
			triangles.add(new Triangle(triangleVertices[0], triangleVertices[1], triangleVertices[2]));
		}
		System.out.println("Packing time: " + (System.currentTimeMillis() - startTime) / 1000.0f + "s");

		System.out.println("All triagles: " + nTrianglesBuff.get(0) + ". Packed triangles: " + triangles.size());

		MHDReader.writeToFile(triangles, vertices, normals, fileName);
	}

}
