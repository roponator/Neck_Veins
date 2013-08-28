package si.uni_lj.fri.mhdreader.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import si.uni_lj.fri.mhdreader.utils.obj.Triangle;
import si.uni_lj.fri.mhdreader.utils.obj.Vertex;

public class TrianglesLabelHelper {
	private Triangle[] triangles;
	private LinkedHashMap<Vertex, Boolean> uniqueVertsLabeled;
	private HashMap<Vertex, ArrayList<Integer>> vertTriMap;
	private boolean areLabeled;

	public TrianglesLabelHelper(int nTriangles) {
		triangles = new Triangle[nTriangles];
		uniqueVertsLabeled = new LinkedHashMap<Vertex, Boolean>(nTriangles);
		vertTriMap = new HashMap<Vertex, ArrayList<Integer>>(nTriangles);
		areLabeled = false;
	}

	public Triangle[] getTriangles() {
		return triangles;
	}

	public LinkedHashMap<Vertex, Boolean> getUniqueVerts() {
		return uniqueVertsLabeled;
	}

	public HashMap<Vertex, ArrayList<Integer>> getVertTriMap() {
		return vertTriMap;
	}

	private void resetLabels() {
		if (areLabeled) {
			for (Triangle t : triangles)
				t.label = 0;
			for (Map.Entry<Vertex, Boolean> entry : uniqueVertsLabeled.entrySet())
				entry.setValue(false);
		}
	}

	public void flagLabeled() {
		resetLabels();
		areLabeled = true;
	}

}
