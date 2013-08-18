package si.uni_lj.fri.mhdreader.utils.obj;

public class Vertex {
	public float x;
	public float y;
	public float z;
	public int index;
	public int normalIndex;
	public double value;

	public Vertex(float x, float y, float z, int index) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.index = index;
		this.normalIndex = 0;
	}

	public void setVertex(float x, float y, float z, float value) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.value = value;
	}

	public String toString() {
		return "v " + x + " " + y + " " + z;
	}
}
