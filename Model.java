
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;
import java.awt.event.*;

public class Model{
	public int height;
	public int width;
	public int depth;
	public Tuple outlet;
	
	private double gamma;
	private boolean unitLengths = true;
	
	private HashSet<ActionListener> actionListeners;
	private Cell[][][] grid;	
	private Cell[][] firstplane;
	private HashMap<Cell, HashSet<Wall>> walls;
	private ArrayList<Cell> cells;
	
	private int supportThreshold;
	
	private final double ROOT2 = Math.sqrt(2);

	public Model(int width, int height, int depth, Tuple outlet) {
		this.width = (width!=0?width:1);
		this.height = (height!=0?height:1);
		this.depth = (depth!=0?depth:1);
		grid = new Cell[this.depth][this.height][this.width];
		firstplane = grid[0];
		this.outlet = outlet;
		walls = new HashMap<Cell, HashSet<Wall>>();
		initCells();
		initWalls();
		
		actionListeners = new HashSet<ActionListener>();	

		gamma = 0.5; //default
	}	
	public Model(int width, int height, Tuple outlet){
		this(width, height, 1, outlet);
		//Main.pr(width + ", " + height);
	}
	private Model() {
		actionListeners = new HashSet<ActionListener>();
	}
	public Model(Collection<Cell> cells){
		int maxx = 0, maxy = 0, maxz = 0;
		this.cells = new ArrayList<Cell>();
		for(Cell c : cells){
			//Main.pr(c.place);
			if(c.place.x >= maxx) maxx = c.place.x + 1;
			if(c.place.y >= maxy) maxy = c.place.y + 1;
			if(c.place.z >= maxz) maxz = c.place.z + 1;
			if(c.getDD().equals(c)) outlet = c.place;
		}
		//Main.pr(outlet);
		width = maxx;
		height = maxy;
		depth = maxz;
		//Main.pr(width, height, depth);
		grid = new Cell[maxz][maxy][maxx];
		walls = new HashMap<Cell, HashSet<Wall>>();
		for(Cell c : cells){
			grid[c.place.z][c.place.y][c.place.x] = c;
			this.cells.add(c);
			walls.put(c, new HashSet<Wall>());
		}
		
		firstplane = grid[0];
		
		initWalls();
		
		actionListeners = new HashSet<ActionListener>();	

		gamma = 0.5; //default
	}
	
	public Cell getCell(int x, int y, int z) {
		return grid[z][y][x];
	}
	public Cell getCell(int x, int y) {
		return firstplane[y][x];
	}
	
	public ArrayList<Cell> getCells(){
		return cells;
	}
	
	public void addActionListener(ActionListener al) {
		actionListeners.add(al);
	}
	public void removeActionListener(ActionListener al) {
		actionListeners.remove(al);
	}
	
	public void initCells() {
		cells = new ArrayList<Cell>();
		for (int z = 0; z<depth; ++z) {
			Cell[][] plane = grid[z];
			for (int y = 0; y<height; ++y){
				Cell[] row = plane[y];
				for(int x= 0; x<width; x++){
					Cell newc = new Cell(new Tuple(x, y, z));	
					row[x] = newc;
					cells.add(newc);
					walls.put(newc, new HashSet<Wall>());
				}
			}
		}
	}
	//
	public void initWalls() {
		Main.pr("iw" + width + ", " + height + ", " + depth);
		Tuple[] directions = {new Tuple(0, 0, 1), new Tuple(1, 0, 1), new Tuple(1, 0, 0)
				, new Tuple(0, 1, 0), new Tuple(1, 1, 0), new Tuple(0, 1, 1)};
		Tuple[] hardDirections = {new Tuple(-1, 1, 0),  new Tuple(-1, 0, 1), new Tuple(0, -1, 1)};
		for(int z = 0; z<depth; z++){
			Cell[][] plane = grid[z];
			for(int y = 0; y<height; ++y) {
				Cell[] row = plane[y];
				for (int x = 0; x<width; ++x){
					Cell c = row[x];
					//Main.pr(c);
					for(Tuple hd : hardDirections){
						Tuple downLeft = new Tuple(x+hd.x, y+hd.y, z+hd.z);
						if(!outOfBounds(downLeft)){
							Cell down = grid[hd.z>0?z+1:z][hd.y>0?y+1:y][hd.x>0?x+1:x];
							Cell left = grid[hd.z<0?z-1:z][hd.y<0?y-1:y][hd.x<0?x-1:x];
							//Main.pr(down);
							Wall diag = null;
							for(Wall w : walls.get(down)){
								if(w.getOpp(down).equals(left)) diag = w;
							}
							assert(diag.getOpp(down).equals(left)):"ERROR: bad diagonal wall";
							if(!diag.getOpp(down).equals(left)) Main.pr("ERROR: bad diagonal wall" + 
										diag + ", " + down + ", " + left + ", " + 
										diag.getOpp(down) + ", " + diag.getOpp(left));
							if(diag==null) Main.pr("ERROR: no diag");
							//Main.pr("opp1: " + c +"; " + downLeft);
							Cell downLeftCell = grid[downLeft.z][downLeft.y][downLeft.x];
							diag.addSides(c, downLeftCell);
							walls.get(c).add(diag);
							walls.get(downLeftCell).add(diag);
						}
					}
					//simple ones
					for(Tuple p : directions){
						Tuple nbp = c.place.plus(p);
						if(!outOfBounds(nbp)){
							Cell nb = grid[nbp.z][nbp.y][nbp.x];
							Wall nwall = new Wall();
							nwall.addSides(c, nb);
							walls.get(c).add(nwall);
							walls.get(nb).add(nwall);
						//Main.pr("opps: " + c +"; " + nb);
						}
					}
				}
			}
		}		
		if(walls == null) Main.pr("null walls");
		if(walls.keySet() == null) Main.pr("null keys");
	}
	
	public void randomSeed(){
		HashSet<Wall> twalls = new HashSet<Wall>(); //temp walls
		HashSet<Wall> usedWalls = new HashSet<Wall>(); //walls with edges going through them
		HashSet<Cell> tree = new HashSet<Cell>();
		Cell start = grid[outlet.z][outlet.y][outlet.x];
			Main.pr(outlet);
		tree.add(start);
		if(start==null){Main.pr("null start"); }
		if(walls.get(start)==null){Main.pr("null start wals"); }
		twalls.addAll(walls.get(start));
		while(!twalls.isEmpty()){
			Wall w = twalls.iterator().next(); //grab random unused wall
			Cell intree = null;
			int in = 0;
			for(Cell c : w.sides()){
				if(tree.contains(c)){ intree = c; break; }
			}
			assert(intree != null): "ERROR: wall without side in tree";
			Cell opp = w.getOpp(intree);
			if(!usedWalls.contains(w) && !tree.contains(opp)){
				tree.add(opp);
				opp.setDD(intree);
				intree.addSource(opp);
				twalls.addAll(walls.get(opp));
			}
			twalls.remove(w);		
			usedWalls.add(w);
		}
		calcArea(start, true);
		
		Main.prl(start.area());
		
		for (ActionListener al : actionListeners)
			al.actionPerformed(null);
	} 
	
	public double evaluate(){															
		//for all cells excluding outlet(s)
		//add up area^gamma
		double sum = 0;
		for(Cell[][] plane : grid){
			for(Cell[] row : plane) {
				for(Cell c : row){
					if(unitLengths || c.place.x==c.dd.place.x || c.place.y==c.dd.place.y)
							sum += Math.pow(c.area(), gamma);
					else sum += Math.pow(c.area(), gamma)*ROOT2;
					//Main.pr(Math.pow(c.area(), gamma));
				}
			}
		}
		return sum;
	}
	public void calcAreas(){ calcArea(grid[outlet.z][outlet.y][outlet.x], true); }
	public void calcVolumes(){ calcVolume(grid[outlet.z][outlet.y][outlet.x], true); }
	public void calcUPLs(){ calcUPL(grid[outlet.z][outlet.y][outlet.x]); }
	private int calcUPL(Cell c){
		int upl = 0;
		int maxArea = 0;
		Cell up = null;
		for(Cell u : c.upstream){
			calcUPL(u);
			if(u.area()> maxArea) {
				maxArea = u.area();
				up = u;
			}			
		}
		if(up!=null) upl = up.upl() + 1;
		else upl = 0;
		c.setUPL(upl);
		return upl;
	}
	public int calcArea(Cell c, boolean update){
		int a = 1;
		String toprint = c.place.toString();
		for(Cell source : c.upstream){
			a += calcArea(source, update);
		}
		if(update) c.setArea(a);
		return a;
	}
	//assumes area is already calculated for everything relevant
	public int calcVolume(Cell c, boolean update){
		int v = 0;
		String toprint = c.place.toString();
		//breadth-first method:
		/*Queue<Cell> upstreams = new LinkedList<Cell>();
		upstreams.addAll(c.upstream);
		while(!upstreams.isEmpty()){
			Cell u = upstreams.pop();
			v += u.area();
			upstreams.addAll(u.upstream);
		}*/
		for(Cell source : c.upstream){
			v += source.area + calcVolume(source, update);
			//toprint += " " + source.place.toString();
		}
		//Main.pr(toprint);
		if(update) c.setVolume(v);
		return v;
	}
	
	public boolean outOfBounds(Tuple coords){
		return (coords.x < 0 || coords.x >= width || coords.y < 0 
				|| coords.y >= height || coords.z < 0 || coords.z >= depth);	
	}
	
	public boolean isConnected(Cell start, Cell end){
		//TODO: implement //I think it is implemented now?
		//HashSet<Cell> marked = new HashSet<Cell>();
		Cell c = start;
		while(!c.place.equals(outlet)){
			Cell into = c.getDD();
			if(into.equals(end)) return true;
			c = into;
			//Main.pr("testing loops " + c);
		}
		return false;
	}

	public void optimize(int times, boolean updateView, boolean pause, boolean metropolis){
		double val = evaluate();
		Main.pr("start val: \t" + val);
		int changes = 0;
		int improvements = 0;
		long startTime = System.currentTimeMillis();
		boolean converge = false;
		int baseTimes = 0;
		double tenDiff = 0;
		int strikes = 0;
		if(times==-1){
			baseTimes = width*height*depth;
			times = 1000;
			converge = true;
		}
		for(int i = 0; i<times + baseTimes; i++){
			//Model clone = clone();
			//Main.pr("opt" + i);
			Tuple p = new Tuple((int)(Math.random()*width),
						(int)(Math.random()*height), (int)(Math.random()*depth));
			Cell c = grid[p.z][p.y][p.x];
			Cell oldDD = c.getDD();
			boolean changed = changeDirection(p.x, p.y, p.z);
			if(!changed) continue;
			else changes++;
			Cell newDD = c.getDD();
			double diff = valDiff(c, oldDD, newDD);
			//double newVal = evaluate();
			//Main.pr("changing " + p + ": " + newVal);
			//TODO: implement actual metropolis rule
			if(/**/diff < 0/**newVal < val /**/|| (metropolis && Math.random()*times > i)/**/){
				//Main.pr("improving");
				improvements++;
				val = val + diff; //newVal;
				tenDiff -= diff;
				//Main.pr(val + "; " + evaluate());
				if(updateView){
					for (ActionListener al : actionListeners)
						al.actionPerformed(null);
				}
				if(pause){ try{Thread.sleep(500);}catch(Exception e){} }
			}
			else { //undo changes made in changeDirection
				//Main.pr("undoing");
				newDD.removeSource(c);
				c.setDD(oldDD);
				oldDD.addSource(c);
				updateAreas(c.area(), newDD, oldDD);				
			}
			if(converge && i%1000==0){
				//Main.pr("\ttenDiff: " + tenDiff);
				if(tenDiff < 0.00001 /*&& changes*100/(times+baseTimes) < 10*/) strikes++;
				if(strikes < 3) times+=2000;				
				tenDiff=0;
			}
		}
		long endTime = System.currentTimeMillis();
		Main.pr("new val :\t" + val);		
		Main.pr("time    :\t" + (endTime - startTime));	
		Main.pr("tries   :\t" + (times + baseTimes));	
		Main.pr("changes :\t" + changes + "\t" + changes*100/(times+baseTimes) + "%");
		Main.pr("improved:\t" + improvements + "\t" + improvements*100/(times+baseTimes) + "%");
	}
	
	public double valDiff(Cell chosen, Cell oldDD, Cell newDD){
		int changeArea = chosen.area();
		double difference = 0;
		if(!unitLengths){
			double lengthDiff =	0;
			lengthDiff -= (chosen.place.x == oldDD.place.x
					|| chosen.place.y == oldDD.place.y?1:ROOT2);
			lengthDiff += (chosen.place.x == newDD.place.x
					|| chosen.place.y == newDD.place.y?1:ROOT2);
			difference += lengthDiff*Math.pow(changeArea, gamma);
			//Main.pr(chosen + ", " + lengthDiff + ", " + oldDD + ", " + newDD);
		}
		Cell oldInto = oldDD;
		HashSet<Cell> marked = new HashSet<Cell>();
		while(!oldInto.place.equals(outlet)){
			double length =	(unitLengths || oldInto.place.x == oldInto.dd.place.x
					|| oldInto.place.y == oldInto.dd.place.y?1:ROOT2);
			//Main.pr(oldInto + ", " + oldInto.dd + ", " + length);
			difference -= Math.pow(oldInto.area() + changeArea, gamma)*length;
			difference += Math.pow(oldInto.area(), gamma)*length;
			marked.add(oldInto);
			oldInto = oldInto.getDD();
		}
		Cell newInto = newDD;
		while(!marked.contains(newInto) && !newInto.place.equals(outlet)){
			double length =	(unitLengths || newInto.place.x == newInto.dd.place.x
					|| newInto.place.y == newInto.dd.place.y?1:ROOT2);
			//Main.pr(newInto + ", " + newInto.dd + ", " + length);
			//Main.pr(length);
			difference -= Math.pow(newInto.area() - changeArea, gamma)*length;
			difference += Math.pow(newInto.area(), gamma)*length;
			newInto = newInto.getDD();
		}
		while(!newInto.place.equals(outlet)){ //undo the changes to shared links
			double length =	(unitLengths || newInto.place.x == newInto.dd.place.x
					|| newInto.place.y == newInto.dd.place.y?1:ROOT2);
			//Main.pr(newInto + ", " + newInto.dd + ", " + length);
			//Main.pr(length);
			difference -= Math.pow(newInto.area(), gamma)*length;;
			difference += Math.pow(newInto.area() + changeArea, gamma)*length;;
			newInto = newInto.getDD();
		}
		return difference;
	}
	
	//efficiently calcArea for just the changed ones
	private void updateAreas(int changeArea, Cell oldDD, Cell newDD){
		Cell oldInto = oldDD;
		while(!oldInto.place.equals(outlet)){
			oldInto.setArea(oldInto.area() - changeArea);
			oldInto = oldInto.getDD();
		}
		Cell newInto = newDD;
		while(!newInto.place.equals(outlet)){
			newInto.setArea(newInto.area() + changeArea);
			newInto = newInto.getDD();
		}	
	}
	
	public boolean changeDirection(int x, int y, int z){
		ArrayList<Cell> pots = new ArrayList<Cell>(); //potentials
		Cell c = grid[z][y][x];
		if(outlet.equals(c.place)) return false;
		//Main.pr("cd checkpoint1");
		for(Wall w : walls.get(c)){
			//Main.pr("wall " + w);
			Cell pot = w.getOpp(c);
			boolean blocked = false; //a diagonal is blocked or the edge is already used
			for(Cell s : w.sides()){
				if(w.getOpp(s).equals(s.getDD())) {
					blocked = true;
					break;
				}
				//Main.pr("side " + s);
			}
			if(blocked || isConnected(pot, c)) continue;
			else pots.add(pot);	
			//Main.pr("after testing for loops");	
		}
		//Main.pr("after populating pots");
		if(pots.size() == 0) return false;
		Cell newDD = pots.get((int)(Math.random()*pots.size()));
		//Main.pr(newDD);
		//Main.pr(c.place + ": " + newDD + "; " + newDD.negate());
		Cell oldDD = c.getDD();
		oldDD.removeSource(c);
		c.setDD(newDD);
		newDD.addSource(c);
		
		//Main.pr("before updating areas");
		updateAreas(c.area(), oldDD, newDD);
		//calcArea(grid[outlet.y][outlet.x], true);
		return true;
	}
	//only clones zeroeth slice  TODO: make clone whole thing (maybe)
	public Model clone(){
		return clone(0);
	}
	//only clones one slice, at depth z
	public Model clone(int z) {
		//int height = this.width; //shadowing
		//int width = this.depth; //shadowing
		Cell[][] grid = this.grid[z]; //shadowing
		
		Model clone = new Model(width, height, outlet);
		//clone.grid = new Cell[depth][][];
		//clone.grid = new Cell[1][][];
		Cell[][] cloneplane = new Cell[grid.length][];
		for (int i = 0; i<grid.length; ++i){
			Cell[] row = grid[i];
			Cell[] newRow = new Cell[row.length];
			cloneplane[i] = newRow;
			for(int j = 0; j<row.length; j++){
				newRow[j] = row[j].clone();
				clone.walls.put(newRow[j], new HashSet<Wall>());
			}
		}			
		for (int i = 0; i<grid.length; ++i){
			Cell[] row = grid[i];
			Cell[] crow = cloneplane[i];
			for(int j = 0; j<row.length; j++){
				Tuple p = row[j].getDD().place;
				crow[j].setDD(cloneplane[p.y][p.x]);				
				for(Cell s : row[j].upstream){
					crow[j].addSource(cloneplane[s.place.y][s.place.x]);
				}
			}
		}
		Cell[][][] clonegrid = {cloneplane};
		clone.grid = clonegrid;
		clone.initWalls();
		return clone;
	}
	
	public int getSupportThreshold(){ return supportThreshold; }
	public void setSupportThreshold(int t){ supportThreshold = t; }
	public double getGamma(){ return gamma; }
	public void setGamma(double g){ gamma = g; }
	public void setLengths(boolean units){ unitLengths = units; }
	public boolean getLengths(){ return unitLengths; }
	
	public String printAreas(){
		String toPrint = "";
		for(Cell[][] row : grid){
			for(Cell[] col : row){
				for(Cell c: col) {
					toPrint += c.area() + "\n";
				}
			}
		}
		return toPrint;
	}
	public String printVolumes(){
		String toPrint = "";
		for(Cell[][] row : grid){
			for(Cell[] col : row){
				for(Cell c: col) {
					toPrint += c.volume() + "\n";
				}
			}
		}
		return toPrint;
	}
	public String printLengths(){
		String toPrint = "";
		for(Cell[][] row : grid){
			for(Cell[] col : row){
				for(Cell c: col) {
					toPrint += c.length() + "\n";
				}
			}
		}
		return toPrint;
	}
	public String printOrders(){
		String toPrint = "";
		for(Cell[][] row : grid){
			for(Cell[] col : row){
				for(Cell c: col) {
					toPrint += c.order() + "\n";
				}
			}
		}
		return toPrint;
	}
	public String printDDs(){
		String toPrint = "";
		for(Cell[][] row : grid){
			for(Cell[] col : row){
				for(Cell c: col) {
					toPrint += c.place + ", " + c.getDD().place + "\n";
				}
			}
		}
		return toPrint;
	}
	public String printUPLs(){
		String toPrint = "";
		for(Cell[][] row : grid){
			for(Cell[] col : row){
				for(Cell c: col) {
					toPrint += c.upl() + "\n";
				}
			}
		}
		return toPrint;
	}
	
	//df = displayFactor, in case a scaled version is wanted for displaying with some other program
	public StringBuilder getXGMML(int df){ 
		HashMap<Cell, Integer> map = new HashMap<Cell, Integer>();
		StringBuilder result = new StringBuilder("<?xml version=\"1.0\"?>\n<graph>\n\t<att name=\"directed\" value=\"0\"/>\n");
		result.append("\t<att name=\"mode\" value=\"UG\" />\n");
		int nodecnt = width*height*depth;
		int i = 0;
		for(Cell[][] plane : grid){
			for(Cell[] row : plane){
				for(Cell c: row) {
					result.append("\t<node id=\"" + i + "\" label=\"" + i + "\"/>\n");
					result.append("\t\t<graphics type=\"oval\" x=\"" + c.place.x*df + ".0\" y=\"" + c.place.y*df + ".0\" z=\"" + c.place.z*df + ".0\"/>\n");
					map.put(c, i);
					i++;
				}
			}
		}
		for(Cell[][] plane : grid){
			for(Cell[] row : plane){
				for(Cell c: row) {
					result.append("\t<edge source=\""+map.get(c)+"\" target=\"" + map.get(c.getDD()) + "\" label=\"\" weight=\"1\"/>\n");
				}
			}
		}
		result.append("</graph>\n");
		return result;
	}
	
}

/*

						Tuple downLeft = new Tuple(x+hd.x, y+hd.y, z+hd.z);
					//Tuple downLeft = new Tuple(x+1, y, z-1);
						if(!outOfBounds(downLeft)){
							Cell down = grid[hd.z>0?z+1:z][hd.y>0?y+1:y][hd.x>0?x+1:x];
						//Cell down = row[x+1]; //these won't be out of bounds either
						//Cell left = grid[z-1][y][x];
							Cell left = grid[hd.z<0?z-1:z][hd.y<0?y-1:y][hd.x<0?x-1:x];
						//Main.pr(down);
						Wall diag = null;
						for(Wall w : walls.get(down)){
							if(w.getOpp(down).equals(left)) diag = w;
						}
						assert(diag.getOpp(down).equals(left)):"ERROR: bad diagonal wall";
						if(!diag.getOpp(down).equals(left)) Main.pr("ERROR: bad diagonal wall" + diag + ", " + down + ", " + left + ", " + diag.getOpp(down) + ", " + diag.getOpp(left));
						if(diag==null) Main.pr("ERROR: no diag");
						//Main.pr("opp1: " + c +"; " + downLeft);
						Cell downLeftCell = grid[downLeft.z][downLeft.y][downLeft.x];
						diag.addSides(c, downLeftCell);
						walls.get(c).add(diag);
						walls.get(downLeftCell).add(diag);
					}
					
					downLeft = new Tuple(x, y+1, z-1);
					if(!outOfBounds(downLeft)){
						Cell down = grid[y+1][x][z]; //these won't be out of bounds either
						Cell left = col[z-1];
						//Main.pr(down);						
						Wall diag = null;
						for(Wall w : walls.get(down)){
							if(w.getOpp(down).equals(left)) diag = w;
						}
						assert(diag.getOpp(down).equals(left)):"ERROR: bad diagonal wall";
						if(!diag.getOpp(down).equals(left)) Main.pr("ERROR: bad diagonal wall" + diag + ", " + down + ", " + left + ", " + diag.getOpp(down) + ", " + diag.getOpp(left));
						if(diag==null) Main.pr("ERROR: no diag");
						//Main.pr("opp2: " + c +"; " + downLeft);
						diag.addSides(c, grid[downLeft.y][downLeft.x][downLeft.z]);
						walls.get(c).add(diag);
						walls.get(grid[downLeft.y][downLeft.x][downLeft.z]).add(diag);
					}
					
					downLeft = new Tuple(x-1, y+1, z);
					if(!outOfBounds(downLeft)){
						Cell down = grid[y+1][x][z]; //these won't be out of bounds either
						Cell left = plane[x-1][z];
						//Main.pr(down);
						Wall diag = null;
						for(Wall w : walls.get(down)){
							if(w.getOpp(down).equals(left)) diag = w;
						}
						assert(diag.getOpp(down).equals(left)):"ERROR: bad diagonal wall";
						if(!diag.getOpp(down).equals(left))Main.pr("ERROR: bad diagonal wall" + diag + ", " + down + ", " + left + ", " + diag.getOpp(down) + ", " + diag.getOpp(left));
						if(diag==null) Main.pr("ERROR: no diag");
						//Main.pr("opp3: " + c +"; " + downLeft);
						diag.addSides(c, grid[downLeft.y][downLeft.x][downLeft.z]);
						walls.get(c).add(diag);
						walls.get(grid[downLeft.y][downLeft.x][downLeft.z]).add(diag);
					}
					*/