package si.uni_lj.fri.mhdreader.utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import si.uni_lj.fri.mhdreader.utils.obj.Triangle;
import si.uni_lj.fri.mhdreader.utils.obj.Vertex;

public class LabelUtil {

	public static boolean[] getValidLabels(int nTriangles, int minTriangles, TrianglesLabelHelper helper) {
		labelTriangels(nTriangles, helper);
		int nLabels = findNumLabels(helper);
		int[] hist = createLabelHistogram(nLabels, helper);
		return findValidLabels(hist, minTriangles);
	}

	private static int findNumLabels(TrianglesLabelHelper helper) {
		Triangle[] tris = helper.getTriangles();
		int nLabels = 0;
		for (Triangle t : tris) {
			if (t.label > nLabels)
				nLabels = t.label;
		}
		return nLabels + 1;
	}

	private static int[] createLabelHistogram(int nLabels, TrianglesLabelHelper helper) {
		Triangle[] tris = helper.getTriangles();
		int[] hist = new int[nLabels];
		for (Triangle t : tris) {
			hist[t.label]++;
		}
		return hist;
	}

	private static boolean[] findValidLabels(int[] hist, int minTriangles) {
		boolean[] validLabels = new boolean[hist.length];
		for (int i = 0; i < hist.length; i++) {
			if (hist[i] >= minTriangles) {
				validLabels[i] = true;
			}
		}
		return validLabels;
	}

	private static void labelTriangels(int nTriangles, TrianglesLabelHelper helper) {
		Triangle[] tris = helper.getTriangles();
		HashMap<Vertex, ArrayList<Integer>> map = helper.getVertTriMap();
		LinkedHashMap<Vertex, Boolean> vertLabeled = helper.getUniqueVerts();
		LinkedList<Integer> trianglesFIFO = new LinkedList<Integer>();
		Vertex[] tVrts = new Vertex[3];
		int label = 1;
		for (Vertex v : map.keySet()) {
			if (!vertLabeled.put(v, true)) {
				trianglesFIFO.addAll(map.get(v));
				while (!trianglesFIFO.isEmpty()) {
					Triangle t = tris[trianglesFIFO.pop()];
					if (!t.isLabeled()) {
						t.label = label;
						tVrts[0] = t.v1;
						tVrts[1] = t.v2;
						tVrts[2] = t.v3;
						for (Vertex tV : tVrts) {
							if (!vertLabeled.put(tV, true)) {
								trianglesFIFO.addAll(map.get(tV));
							}
						}
					}
				}
				label++;
			}
		}
	}

	public static void createVertexList(int nTriangles, FloatBuffer trianglesBuff, TrianglesLabelHelper helper) {
		helper.flagLabeled();
		Triangle[] tris = helper.getTriangles();
		HashMap<Vertex, ArrayList<Integer>> map = helper.getVertTriMap();
		LinkedHashMap<Vertex, Boolean> vertLabeled = helper.getUniqueVerts();
		HashMap<Vertex, Integer> vertIndexes = new HashMap<Vertex, Integer>(nTriangles);
		Vertex[] verts = new Vertex[3];
		int vertIndex = 1;
		for (int i = 0; i < nTriangles; i++) {
			for (int j = 0; j < 3; j++) {
				float x = trianglesBuff.get(i * 9 + j * 3);
				float y = trianglesBuff.get(i * 9 + j * 3 + 1);
				float z = trianglesBuff.get(i * 9 + j * 3 + 2);
				Vertex v = new Vertex(x, y, z, vertIndex);
				verts[j] = v;
				if (map.containsKey(v)) {
					map.get(v).add(i);
					verts[j].normalIndex = 1;
					verts[j].index = vertIndexes.get(v);
				} else {
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(i);
					v.normalIndex = 1;
					v.index = vertIndex;
					map.put(v, list);
					vertLabeled.put(v, false);
					vertIndexes.put(v, vertIndex++);
				}
			}
			tris[i] = new Triangle(verts[0], verts[1], verts[2]);
			tris[i].normalIndex = 1;
		}
	}

}
