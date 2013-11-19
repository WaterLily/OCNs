import java.util.HashSet;
// really represents the link flowing out of a cell
public class Cell{
	final Tuple place;
	Cell dd; //drainage direction
	int magnitude;
	int area;
	int volume;
	int order;
	double length; //distance to DD //not used - instead, model calculates lengths
	int upl; //upstream length
	Picture image;
	HashSet<Cell> upstream;
	public Cell(Tuple place){
		this.place = place;
		this.dd = this;
		upstream = new HashSet<Cell>();
		area = 1;
	}
	public void setDD(Cell dd){ this.dd = dd; }
	public Cell getDD(){ return dd; }
	public Cell clone(){
		Cell clone = new Cell(place);
		clone.setArea(area);
		//clone.setDD(dd.clone());
		return clone;
	}
	public Picture getImage(){ return image; }
	public void setImage(Picture image){ this.image = image; }
	
	public int hashCode() {
		return place.hashCode();
	}
	public void addSource(Cell up){ 
		//Main.pr("adding to " + place + "; " + up.place);
		//if(upstream.contains(up)) Main.pr("redundant source addition: " + place + "; " + up.place);
		upstream.add(up);
	}
	public void removeSource(Cell up){
		//Main.pr("removing from " + place + "; " + up.place);
		//if(!upstream.contains(up)) Main.pr("bad source removal: " + place + "; " + up.place);
		upstream.remove(up);
	}
	public int area(){ return area; }
	public int magnitude(){ return magnitude; }
	public int volume(){ return volume; }
	public double length(){ return length; }
	public int order(){ return order; }
	public int upl(){ return upl; }
	public void setArea(int a){ area = a; }
	public void setMagnitude(int m){ magnitude = m; }
	public void setVolume(int v){ volume = v; }
	public void setLength(int el){ length = el; }
	public void setOrder(int o){ order = o; }
	public void setUPL(int u){upl = u; }
	
	public String toString(){ return "c " + place; }
}