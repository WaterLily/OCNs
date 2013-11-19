import java.util.HashMap;
import java.util.Set;
public class Wall{
	private HashMap<Cell, Cell> internal;
	public Wall(){
		internal = new HashMap<Cell, Cell>();
	}
	public void addSides(Cell one, Cell two){internal.put(one, two); internal.put(two, one);}
	public Set<Cell> sides(){ return internal.keySet(); }
	public Cell getOpp(Cell one) { return internal.get(one); }
}