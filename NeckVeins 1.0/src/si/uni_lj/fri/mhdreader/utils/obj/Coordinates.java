package si.uni_lj.fri.mhdreader.utils.obj;

public class Coordinates {
	private float x;
	private float y;
	private float z;

	public Coordinates(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;

		Coordinates other = (Coordinates) obj;
		if (this.x != other.x || this.y != other.y || this.z != other.z)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = (new Float(x).hashCode() >> 17) ^ (new Float(y).hashCode() ^ new Float(z).hashCode());
		return hashCode;
	}
}
