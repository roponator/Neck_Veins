package si.uni_lj.fri.mhdreader;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import si.uni_lj.fri.mhdreader.utils.obj.Coordinates;
import si.uni_lj.fri.mhdreader.utils.obj.Normal;
import si.uni_lj.fri.mhdreader.utils.obj.Triangle;
import si.uni_lj.fri.mhdreader.utils.obj.Vertex;

public class MHDReader {
	public static int Nx;
	public static int Ny;
	public static int Nz;

	public static double dx;
	public static double dy;
	public static double dz;

	public static double tx;
	public static double ty;
	public static double tz;

	public static double[][] rotationMatrix = new double[4][4];
	public static boolean elementByteOrder;
	public static String elementType;

	public static String rawFile;

	public static void readMHD(String fileName) {
		File file = new File(fileName);
		Scanner sc = null;
		try {
			sc = new Scanner(file);
			String value = "";
			while (sc.hasNext()) {
				value = sc.next();
				sc.next(); // move past sign =
				if (value.equals("DimSize")) {
					Nx = sc.nextInt();
					Ny = sc.nextInt();
					Nz = sc.nextInt();
				} else if (value.equals("ElementSpacing")) {
					dx = Double.parseDouble(sc.next());
					dy = Double.parseDouble(sc.next());
					dz = Double.parseDouble(sc.next());
				} else if (value.equals("Position")) {
					tx = sc.nextDouble();
					ty = sc.nextDouble();
					tz = sc.nextDouble();
				} else if (value.equals("Orientation")) {
					for (int i = 0; i < rotationMatrix.length; i++) {
						for (int j = 0; j < rotationMatrix[i].length; j++) {
							if (i >= 3 || j >= 3)
								rotationMatrix[i][j] = (i == j) ? 1 : 0;
							else
								rotationMatrix[i][j] = Double.parseDouble(sc.next());
						}
					}
				} else if (value.equals("AnatomicalOrientation")) {
					sc.nextLine();
				} else if (value.equals("ElementByteOrderMSB")) {
					elementByteOrder = Boolean.parseBoolean(sc.next().toLowerCase());
				} else if (value.equals("ElementType")) {
					elementType = sc.next();
				} else if (value.equals("ElementDataFile")) {
					rawFile = file.getParentFile().getAbsolutePath() + "\\" + sc.next();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}

	}

	/*
	 * Handle Big - little indian issue
	 */
	public static double[][][] getCTMatrix(String fileName) {
		double[][][] CTMatrix = new double[Ny][Nx][Nz];
		Scanner sc = null;
		DataInputStream dataIn = null;
		try {
			FileInputStream in = new FileInputStream(fileName + ".raw");
			BufferedInputStream buffIn = new BufferedInputStream(in);
			dataIn = new DataInputStream(buffIn);
			if (elementType.equals("MET_FLOAT")) {
				// while ((c = dataIn.readFloat()) != -1) {
				// System.out.println(c);
				// }
			} else if (elementType.equals("MET_USHORT")) {
				// while ((c = dataIn.readUnsignedShort()) != -1) {
				// System.out.println(c);
				// }
			} else if (elementType.equals("MET_SHORT")) {
				for (int k = 0; k < Nz; k++) {
					for (int j = 0; j < Ny; j++) {
						for (int i = 0; i < Nx; i++) {
							// byte[] valueArray = new byte[2];
							// dataIn.readFully(valueArray);
							// short value =
							// ByteBuffer.wrap(valueArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
							float value = Short.reverseBytes(dataIn.readShort());
							CTMatrix[j][i][k] = value;
						}
					}
				}

			}
			in.close();
			buffIn.close();
			dataIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}

		return CTMatrix;
	}

	/*
	 * Handle Big - little indian issue
	 */
	public static float[] getFloatCTMatrix(String fileName) {
		float[] CTMatrix = new float[Ny * Nx * Nz];
		Scanner sc = null;
		DataInputStream dataIn = null;
		try {
			FileInputStream in = new FileInputStream(fileName + ".raw");
			BufferedInputStream buffIn = new BufferedInputStream(in);
			dataIn = new DataInputStream(buffIn);
			if (elementType.equals("MET_FLOAT")) {
				// while ((c = dataIn.readFloat()) != -1) {
				// System.out.println(c);
				// }
			} else if (elementType.equals("MET_USHORT")) {
				// while ((c = dataIn.readUnsignedShort()) != -1) {
				// System.out.println(c);
				// }
			} else if (elementType.equals("MET_SHORT")) {
				for (int z = 0; z < Nz * Nx * Ny; z++) {

					// byte[] valueArray = new byte[2];
					// dataIn.readFully(valueArray);
					// short value =
					// ByteBuffer.wrap(valueArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
					double value = Short.reverseBytes(dataIn.readShort());
					CTMatrix[z] = (float) value;

				}

			}
			in.close();
			buffIn.close();
			dataIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}

		return CTMatrix;
	}

	/**
	 * Reads data and returns ArrayList of sub matrices
	 * 
	 * @param fileName
	 * @param nSubMatrices
	 *            number of sub matrices
	 * @return
	 */
	public static ArrayList<float[]> getFloatCTMatrix(String fileName, int nSubMatrices) {
		float[] CTMatrix = new float[Nx * Ny * Nz];
		Scanner sc = null;
		DataInputStream dataIn = null;
		try {
			FileInputStream in = new FileInputStream(fileName + ".raw");
			BufferedInputStream buffIn = new BufferedInputStream(in);
			dataIn = new DataInputStream(buffIn);
			if (elementType.equals("MET_FLOAT")) {
				// while ((c = dataIn.readFloat()) != -1) {
				// System.out.println(c);
				// }
			} else if (elementType.equals("MET_USHORT")) {
				// while ((c = dataIn.readUnsignedShort()) != -1) {
				// System.out.println(c);
				// }
			} else if (elementType.equals("MET_SHORT")) {
				for (int z = 0; z < Nz; z++) {
					for (int y = 0; y < Ny; y++) {
						for (int x = 0; x < Nx; x++) {
							// byte[] valueArray = new byte[2];
							// dataIn.readFully(valueArray);
							// short value =
							// ByteBuffer.wrap(valueArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
							double value = Short.reverseBytes(dataIn.readShort());
							CTMatrix[x + y * Nx + z * Nx * Ny] = (float) value;
						}
					}
				}

			}
			in.close();
			buffIn.close();
			dataIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}

		ArrayList<float[]> subMatrices = new ArrayList<float[]>();
		int sliceSize = (Nz + nSubMatrices - 1) / nSubMatrices;
		int lastSliceSize = (Nz + nSubMatrices - 1) - sliceSize * nSubMatrices;
		nSubMatrices = (lastSliceSize <= 1) ? nSubMatrices : nSubMatrices + 1;
		for (int i = 0; i < nSubMatrices; i++) {
			int from = (i * sliceSize - i) * Nx * Ny;
			int to = Nx - 1 + (Ny - 1) * Nx + ((i + 1) * sliceSize - i + 1) * Nx * Ny;
			subMatrices.add(Arrays.copyOfRange(CTMatrix, from, to));
		}
		return subMatrices;
	}

	public static float[] DoubleToFloatCTMatrix(String fileName) {
		readMHD(fileName);
		double[][][] CTMatrix = getCTMatrix(fileName);
		System.out.println("reading done");
		float[] floatCTMatrix = new float[Nx * Ny * Nz];
		for (int z = 0; z < Nz; z++) {
			for (int y = 0; y < Ny; y++) {
				for (int x = 0; x < Nx; x++) {
					// byte[] valueArray = new byte[2];
					// dataIn.readFully(valueArray);
					// short value =
					// ByteBuffer.wrap(valueArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
					floatCTMatrix[x + y * Nx + z * Nx * Ny] = (float) CTMatrix[y][x][z];
				}
			}
		}
		return floatCTMatrix;
	}

	public static void writeToFile(ArrayList<Triangle> triangles, LinkedHashMap<Coordinates, Vertex> vertices,
			LinkedHashMap<Coordinates, Normal> normals, String file) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file + ".obj"));
			for (Map.Entry<Coordinates, Vertex> vertex : vertices.entrySet()) {
				out.write(vertex.getValue().toString() + "\n");
			}
			for (Map.Entry<Coordinates, Normal> normal : normals.entrySet()) {
				out.write(normal.getValue().toString() + "\n");
			}
			out.write("g foo\n");
			for (Triangle t : triangles)
				out.write(t.toString());
			out.write("g");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		System.out.println("Triangles written to: " + file + ".obj");
	}
}
