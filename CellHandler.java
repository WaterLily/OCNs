//adapted from NodeEdgeHandler.java
import org.xml.sax.helpers.*; 
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler; //sort of worked
//import org.apache.xerces.parsers.*; // for SAXParser
import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import java.util.HashMap;
import java.util.Collection;
//This class extends DefaultHandler and handles SAX events for us.
//NodeHandler overloads startElement, startDocument, and endDocument from
//DefaultHandler

class CellHandler extends DefaultHandler{

    boolean someCoordsSpecified;
    boolean readNode;
    boolean readEdge;
	Collection<Cell> cells;
	int currentId;
	HashMap<Integer, Cell> idMap = new HashMap<Integer, Cell>();
    public CellHandler(Collection cells){
		this.cells = cells;
		someCoordsSpecified = false;
		readNode = false;
		readEdge = false;
    }
    public void startDocument() {
		someCoordsSpecified = false;
	}

    public void startElement(String uri, String localName, String rawName, Attributes attributes){
		if (localName.equals("graphics") && readNode){
			readNode = false;
			readEdge = false;
			String type = attributes.getValue("type");
			String xstr = attributes.getValue("x");
			String ystr = attributes.getValue("y");
			String zstr = attributes.getValue("z");
			double x, y, z;
			x = 0.0;
			y = 0.0;
			z = 0.0;
			boolean coordsSpecified = false;

			if (xstr != null && ystr != null && zstr != null){
				try{
					Double xWrapper = new Double(xstr);
					Double yWrapper = new Double(ystr);
					Double zWrapper = new Double(zstr);
					x = xWrapper.doubleValue();
					y = yWrapper.doubleValue();
					z = zWrapper.doubleValue();
				}
				catch(NumberFormatException nfe){
					//error("Number Format Exception: x,y,z coordinates not valid for node " + n.label());
					return;
				}
				coordsSpecified = true;
				someCoordsSpecified = true;
			}
			Cell newCell = new Cell(new Tuple((int)x, (int)y, (int)z));
			newCell.setDD(newCell);
			//cells.add(newCell);
			idMap.put(currentId, newCell);
		}

		else if(localName.equals("node")){
			//get node information
		    readNode = true;
		    readEdge = false;
			String label = attributes.getValue("label");
			String idstr = attributes.getValue("id");
			int id = 0;

			try{
				id = Integer.parseInt(idstr);
			}
			catch(NumberFormatException nfe) {
					error("Number Format Exception: id does not contain a valid integer for node " + label);
					return;
			}
			currentId = id;
			idMap.put(id, new Cell(new Tuple(0, 0, 0)));
		}
		else if (localName.equals("edge")) {
		    readEdge = true;
		    readNode = false;
			String from = attributes.getValue("source");
			String to = attributes.getValue("target");
			String label = attributes.getValue("label");
			String weight = attributes.getValue("weight");
			String fromlbl = "";
			String tolbl = "";
			int t, f;
			int w = 1;
			try{
				Integer toWrapper = new Integer(to);
				t = toWrapper.intValue();
				Integer fromWrapper = new Integer(from);
				f = fromWrapper.intValue();
				if (weight != null && !weight.equals("") ){
					Integer weightWrapper = new Integer(weight);
					w = weightWrapper.intValue();
				}
			}
			catch(NumberFormatException nfe){
				error("Invalid source, target, or weight in edge " + label);
				return;
			}
			Cell fromCell = idMap.get(f);
			Cell toCell = idMap.get(t);
			if(!(toCell == fromCell)){
				fromCell.setDD(toCell);
				toCell.addSource(fromCell);
				fromCell.setLength(w);
			}
		}
	}//end startElement()

	public void error(String s)
	{
		Main.pr(s);
		System.err.println(s);
	}   
    @Override
    public void endDocument(){
        cells.addAll(idMap.values());
    }

}//end class NodeEdgeHandler



