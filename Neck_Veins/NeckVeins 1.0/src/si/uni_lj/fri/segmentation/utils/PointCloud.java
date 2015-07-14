package si.uni_lj.fri.segmentation.utils;

import java.util.LinkedList;

public class PointCloud {
	private float[] coordinateArray;
	private float[] normalsArray;
	LinkedList<float[]> normals = new LinkedList<float[]>();

	
	
	public void createVertices(float[][][] ctMatrix, float isovalue) {

		boolean[] neighbourStates = { false, false, false, false, false, false };

		for (int i = 1; i < ctMatrix.length - 1; i++) {
			for (int j = 1; j < ctMatrix[0].length - 1; j++) {
				for (int k = 1; k < ctMatrix[0][0].length - 1; k++) {
					if (ctMatrix[i][j][k] > isovalue
							&& ctMatrix[i][j][k - 1] < isovalue
							&& ctMatrix[i][j][k + 1] < isovalue
							&& ctMatrix[i][j - 1][k] < isovalue
							&& ctMatrix[i][j + 1][k] < isovalue
							&& ctMatrix[i - 1][j][k] < isovalue
							&& ctMatrix[i + 1][j][k] < isovalue &&

							ctMatrix[i][j + 1][k - 1] < isovalue
							&& ctMatrix[i][j - 1][k - 1] < isovalue
							&& ctMatrix[i + 1][j][k - 1] < isovalue
							&& ctMatrix[i - 1][j][k - 1] < isovalue &&

							ctMatrix[i][j + 1][k + 1] < isovalue
							&& ctMatrix[i][j - 1][k + 1] < isovalue
							&& ctMatrix[i + 1][j][k + 1] < isovalue
							&& ctMatrix[i - 1][j][k + 1] < isovalue &&

							ctMatrix[i + 1][j - 1][k] < isovalue
							&& ctMatrix[i + 1][j + 1][k] < isovalue
							&& ctMatrix[i - 1][j - 1][k] < isovalue
							&& ctMatrix[i - 1][j + 1][k] < isovalue

					) {
						ctMatrix[i][j][k] = 0;

					}
				}
			}
		}

		LinkedList<float[]> vertices = new LinkedList<float[]>();

		

		int n = 0;
		for (int i = 1; i < ctMatrix.length - 1; i++)
		{
			for (int j = 1; j < ctMatrix[0].length - 1; j++)
			{
				for (int k = 1; k < ctMatrix[0][0].length - 1; k++) 
				{
					float[] gradient = new float[] { 0, 0, 0 };

					if (ctMatrix[i][j][k] < isovalue) {
						if (ctMatrix[i][j][k - 1] > isovalue) {
							neighbourStates[0] = true;
							gradient[2]++;
							n++;
						}
						if (ctMatrix[i][j][k + 1] > isovalue) {
							neighbourStates[1] = true;
							gradient[2]--;
							n++;
						}
						if (ctMatrix[i][j - 1][k] > isovalue) {
							neighbourStates[2] = true;
							gradient[1]++;
							n++;
						}
						if (ctMatrix[i][j + 1][k] > isovalue) {
							neighbourStates[3] = true;
							gradient[1]--;
							n++;
						}
						if (ctMatrix[i - 1][j][k] > isovalue) {
							neighbourStates[4] = true;
							gradient[0]++;
							n++;
						}
						if (ctMatrix[i + 1][j][k] > isovalue) {
							neighbourStates[5] = true;
							gradient[0]--;
							n++;
						}
						vertices.addAll(checkNeighbours(neighbourStates, n, i,
								j, k, gradient));

						neighbourStates[0] = false;
						neighbourStates[1] = false;
						neighbourStates[2] = false;
						neighbourStates[3] = false;
						neighbourStates[4] = false;
						neighbourStates[5] = false;

						n = 0;
					}

				}
			}
		}

		coordinateArray = new float[vertices.size() * 3]; 
		normalsArray = new float[coordinateArray.length];
		int index = 0;

		for (float[] vertex : vertices) {
			coordinateArray[index++] = vertex[0];
			coordinateArray[index++] = vertex[1];
			coordinateArray[index++] = vertex[2];
		}
		index = 0;

		for (float[] normal : normals) {
			normalsArray[index++] = normal[0];
			normalsArray[index++] = normal[1];
			normalsArray[index++] = normal[2];
		}
	}

	public LinkedList<float[]> checkNeighbours(boolean[] states, int n, int i,
			int j, int k, float[] gradient) {
		LinkedList<float[]> vertices = new LinkedList<float[]>();

		if (n == 6 || n == 0)
			return vertices;
		else if (n == 1) {

			if (states[0]) {
				vertices.add(new float[] { i , j , k-0.5f });
			} else if (states[1]) {
				vertices.add(new float[] { i , j, k + 0.5f });
			} else if (states[2]) {
				vertices.add(new float[] { i, j-0.5f, k });
			} else if (states[3]) {
				vertices.add(new float[] { i , j + 0.5f, k});
			} else if (states[4]) {
				vertices.add(new float[] { i-0.5f, j, k });
			} else {
				vertices.add(new float[] { i +0.5f, j, k  });
			}

		} else if (n == 2) {
			if (states[0] && states[1]) {
				vertices.add(new float[] { i , j , k-0.5f });
				vertices.add(new float[] { i, j , k + 0.5f });
				normals.add(new float[] { 0, 0, 1f });
				normals.add(new float[] { 0, 0, -1f });
			} else if (states[2] && states[3]) {
				vertices.add(new float[] { i , j-0.5f, k  });
				vertices.add(new float[] { i , j + 0.5f, k  });
				normals.add(new float[] { 0, 1, 0 });
				normals.add(new float[] { 0, -1, 0 });
			} else if (states[4] && states[5]) {
				vertices.add(new float[] { i-0.5f, j, k });
				vertices.add(new float[] { i +0.5f, j, k });
				normals.add(new float[] { 1, 0, 0 });
				normals.add(new float[] { -1, 0, 0 });
			} else {
				vertices.add(new float[] { i , j, k });
				gradient = normalize(gradient);
				normals.add(gradient);
			}
			return vertices;

		} else if (n == 4) {
			if (!states[0] && !states[1]) {
				vertices.add(new float[] { i , j-0.5f, k  });
				vertices.add(new float[] { i, j + 0.5f, k  });
				normals.add(new float[] { 0, 1, 0 });
				normals.add(new float[] { 0, -1, 0 });
				vertices.add(new float[] { i-0.5f, j, k });
				vertices.add(new float[] { i + 0.5f, j , k  });
				normals.add(new float[] { 1, 0, 0 });
				normals.add(new float[] { -1, 0, 0 });

			}

			else if (!states[2] && !states[3]) {
				vertices.add(new float[] { i , j , k -0.5f});
				vertices.add(new float[] { i, j , k +0.5f });
				normals.add(new float[] { 0, 0, 1f });
				normals.add(new float[] { 0, 0, -1f });
				vertices.add(new float[] { i-0.5f, j , k  });
				vertices.add(new float[] { i +0.5f, j , k  });
				normals.add(new float[] { 1f, 0, 0 });
				normals.add(new float[] { -1f, 0, 0 });
			}

			else if (!states[4] && !states[5]) {
				vertices.add(new float[] { i , j-0.5f, k  });
				vertices.add(new float[] { i , j+0.5f, k });
				normals.add(new float[] { 0, 1f, 0 });
				normals.add(new float[] { 0, -1f, 0 });
				vertices.add(new float[] { i, j, k -0.5f});
				vertices.add(new float[] { i , j , k +0.5f });
				normals.add(new float[] { 0, 0, 1f });
				normals.add(new float[] { 0, 0, -1f });
			}
			return vertices;

		} else if (n == 5) {
			if (!states[0])
				vertices.add(new float[] { i, j, k + 0.5f });
			else if (!states[1])
				vertices.add(new float[] { i , j, k - 0.5f});

			else if (!states[2])
				vertices.add(new float[] { i , j +0.5f, k  });

			else if (!states[3])
				vertices.add(new float[] { i , j-0.5f, k });

			else if (!states[4])
				vertices.add(new float[] { i + 0.5f, j , k });

			else
				vertices.add(new float[] { i-0.5f, j , k });

		} else {
			vertices.add(new float[] { i , j, k });
			gradient = normalize(gradient);
		}

		normals.add(gradient);
		return vertices;
	}

	public float[] normalize(float[] gradient) {
		float length = (float) Math.sqrt((gradient[0] * gradient[0])
				+ (gradient[1] * gradient[1]) + (gradient[2] * gradient[2]));
		length = 1 / length;
		gradient[0] *= length;
		gradient[1] *= length;
		gradient[2] *= length;
		return gradient;

	}

	public float[] getNormals() {

		return normalsArray;
	}

	public float[] getVertices() {
		return coordinateArray;
	}
}