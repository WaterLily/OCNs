public class Tuple{
	public int x, y, z;
	public Tuple(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Tuple(int x, int y){
		this.x = x;
		this.y = y;
		this.z = 0;
	}
	public Tuple plus(Tuple o){ return new Tuple(this.x + o.x, this.y + o.y, this.z + o.z); }
	public Tuple minus(Tuple o){ return new Tuple(this.x - o.x, this.y - o.y, this.z - o.z); }
	public Tuple negate(){ return new Tuple(-x, -y, -z); }

	public String toString() {
		return x + ", " + y + ", " + z;
	}	

	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object other) {
		if (other instanceof Tuple) return toString().equals(other.toString());
		return false;
	}
	

}