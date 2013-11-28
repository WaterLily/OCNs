import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;

import javax.xml.parsers.*;
//import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.XMLReader;

//import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import org.xml.sax.InputSource;
public class Main{
	static	StringBuilder output = new StringBuilder();
	public static void main(String[] args){
		Model model = null;
		pr(args);
		if(args.length == 1 && args[0].equals("file")) { //popup open file
			JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));

			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new GraphFilter());

			int val = fileChooser.showOpenDialog(null);
			if (val == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				String fileName = file.getName();
				pr(fileName);
				model = readXGMML(readFile(file));
			}
		}
		else if(args.length == 1){
			pr("invalid arguments: please use \"file\" to open a saved xgmml file, or give dimensions for a new random network.");
			return;
		}
		else {
			if(args.length == 2) model = new Model(Integer.parseInt(args[0]), Integer.parseInt(args[1]), new Tuple(0, 0));
			if(args.length == 3) model = new Model(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), new Tuple(0, 0));
			else if(args.length == 0) model = new Model(30, 30, new Tuple(0, 0));
			
			model.randomSeed();
		}
		View staticView = new View(model.clone());
		View view = new View(model);
		model.removeActionListener(staticView);
		Scanner keyboard = new Scanner(System.in);
		while(true) {
			try{
			pr("--");
			String input = keyboard.nextLine();
			output.append(input + "\n");
			StringTokenizer tokey = new StringTokenizer(input);
			if(!tokey.hasMoreTokens()) continue;
			String firstWord = tokey.nextToken();
			View toUpdate = view;
			if(firstWord.charAt(0)=='2') {
				toUpdate = staticView;
				firstWord = firstWord.substring(1, firstWord.length());
			}
			if(firstWord.startsWith("o")){ //optimize
				int times = 1000; //default
				if(!tokey.hasMoreTokens()) continue;
				times = Integer.parseInt(tokey.nextToken());
				boolean updatetoUpdate = false;
				boolean pause = false;
				boolean metropolis = false;
				while(tokey.hasMoreTokens()){
					String nextWord = tokey.nextToken();
					if(nextWord.startsWith("u")) updatetoUpdate = true;
					if(nextWord.startsWith("p")) pause = true;
					if(nextWord.startsWith("m")) metropolis = true;
				}
				model.optimize(times, updatetoUpdate, pause, metropolis);
			}
			if(firstWord.startsWith("sa")) { //save
				pr("type description:>   ");
				String desc = keyboard.nextLine();
				desc += "dimensions:\t" + model.width + ", " + model.height + ", " + model.depth
						+ "\nenergy:\t" + model.evaluate()
						+ "\ngamma:\t" + model.getGamma()
						+ "\noutput:\n" + output.toString()
						+ "\nareas:\n" + model.printAreas()
						+ (model.getLengths()?"\nunit lengths":"real lengths");
				
				String graph = model.getXGMML(1).toString();
				String displayGraph = model.getXGMML(100).toString();
				
				JFileChooser fileSaver = new JFileChooser(System.getProperty("user.dir"));
				fileSaver.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int val = fileSaver.showSaveDialog(null);

				if (val == JFileChooser.APPROVE_OPTION) {
					File savingFile = fileSaver.getSelectedFile();
					PrintWriter pw = null;
					try {
						String fileName = savingFile.getName();
						if(fileName.indexOf(".") <=0) fileName += ".xgmml";
						pw = new PrintWriter(new FileOutputStream(fileName));
						pw.print(graph);
						pw.close();
						pw = new PrintWriter(new FileOutputStream(fileName.substring(0, fileName.length()-6) + "2.xgmml"));
						pw.print(displayGraph);
						pw.close();
						pw = new PrintWriter(new FileOutputStream(fileName.substring(0, fileName.lastIndexOf(".")) + ".desc"));
						pw.print(desc);
						pw.close();
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					} finally {
						pw.close();
					}
				}
				//model.save(filename);
			}
			if(firstWord.startsWith("su")){ //change support area
				if(!tokey.hasMoreTokens()) continue;
				int howMuch = Integer.parseInt(tokey.nextToken());
				toUpdate.support(howMuch);
			}
			if(firstWord.startsWith("d")){ //view specified slice of 3D network
				if(!tokey.hasMoreTokens()) continue;
				int where = Integer.parseInt(tokey.nextToken());
				toUpdate.viewDepth(where);
			}
			if(firstWord.startsWith("g")){ //reset gamma
				if(!tokey.hasMoreTokens()) continue;
				double newGamma = Double.parseDouble(tokey.nextToken());
				model.setGamma(newGamma);
			}
			if(firstWord.startsWith("t")){ //toggle thickness display
				toUpdate.toggleThickness();
			}
			if(firstWord.startsWith("a")){ //toggle showing areas
				toUpdate.toggleAreas();
			}
			if(firstWord.startsWith("v")){ //toggle showing volumes
				toUpdate.toggleVolumes();
			}
			if(firstWord.startsWith("up")){ //toggle showing upstream lengths
				toUpdate.toggleUPLs();
			}
			if(firstWord.startsWith("un")){ //give links unit length
				model.setLengths(true);
			}
			if(firstWord.startsWith("r")){ //give links real length
				model.setLengths(false);
			}
			if(firstWord.startsWith("p")){ //print something
				while(tokey.hasMoreTokens()){
					String nextWord = tokey.nextToken();
					if(nextWord.startsWith("a")) pr(model.printAreas());
					if(nextWord.startsWith("v")) pr(model.printVolumes());
					if(nextWord.startsWith("l")) pr(model.printLengths());
					if(nextWord.startsWith("o")) pr(model.printOrders());
					if(nextWord.startsWith("d")) pr(model.printDDs()); //drainage directions
					if(nextWord.startsWith("u")) pr(model.printUPLs()); //upstream lengths
				}
			}
			if(firstWord.startsWith("c")){ //calc something
				while(tokey.hasMoreTokens()){
					String nextWord = tokey.nextToken();
					if(nextWord.startsWith("a")) model.calcAreas();
					if(nextWord.startsWith("v")) model.calcVolumes();
					if(nextWord.startsWith("u")) model.calcUPLs();
				}
			}
			if(firstWord.startsWith("h")){ //help //this doesn't describe all commands at the moment
				pr("commands include: optimize, save, support, depth, gamma, thickness, area, volume, upstream_lengths, unit_length, real_length, print, calculate, and help." + 
					"\nThe smallest unique prefix of each is sufficient\n" + 
					  "for all commands and string arguments." +
					"\n--" +  
					"\n optimize takes optional arguments:" + 
					"\n  <int>: times to optimize (must come first;\n" + 
					  "         if -1 is given, optimization will run till convergence)" + 
					"\n  update: updates view as it optimizes" + 
					"\n  pause: pauses between each optimization step for visual effect" + 
					"\n--" + 
					"\n save takes zero arguments, and saves three files:" + 
					"\n  <given name>.xgmml: xgmml representation of current state of network" + 
					"\n  <given name>2.xgmml: xgmml representation with coordinates scaled up,\n" + 
					  "                       for use with certain graph viewing applications" + 
					"\n  <given name>.desc: a description file including all the output\n" + 
					  "                     from the session so far along with a user-entered\n" + 
					  "                     description" + 
					"\n--" + 
					"\n thickness, area, volume, and upstream_lengths toggle display\n" + 
					  " of respective properties" + 
					"\n--" + 
					"\n support requires one argument:" + 
					"\n  <int>: support area" +
					"\n--" + 
					"\n depth requires one argument:" + 
					"\n  <int>: desired z-axis slice of 3D network to display" +
					"\n--" + 
					"\n gamma requires one argument:" + 
					"\n  <double>: desired gamma value" +					
					"\n--" + 
					"\n unit_length and real_length set the length of links\n" + 
					  " to be units or Euclidean distances, respectively" +					
					"\n--" + 
					"\n print requires one argument from among the following,\n" + 
					  " and prints out the requested data:" + 
					"\n  a: areas" + 
					"\n  v: volumes" + 
					"\n  l: lengths" + 
					"\n  o: orders" + 
					"\n  d: drainage directions" + 
					"\n  u: upstream lengths");
			}
			}catch(NumberFormatException nfe){ pr(nfe); continue; }
		}
	}
	public static void pr(String s){ System.out.println(s); output.append(s + "\n");}
	public static void pr(Collection a){ for(Object o: a) pr(o.toString()); }
	public static void pr(int[] a){ for(int i: a) pr(i + ""); }
	public static void pr(double[] a){ for(double d: a) pr(d + ""); }
	public static void pr(Object o){ pr(o.toString()); }
	

	/**
	 * Returns the contents of a file as a string
	 */
	public static String readFile(File file){
		int i = 0;
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(file));
			for(String nextLine = reader.readLine(); nextLine!=null; nextLine = reader.readLine()){
				builder.append(nextLine);
				i++;
			}
			reader.close();
		} catch (IOException ioe){ ioe.printStackTrace(); }
		finally {
			if (reader != null)
				try { reader.close(); } 
				catch (IOException ignored) {}
		}
		return builder.toString();
	}    
	
	//returns a Model parsed from the argument String
	public static Model readXGMML(String sourceString){
		ArrayList<Cell> result = new ArrayList<Cell>();
		try{ /////////////
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser sp = spf.newSAXParser();
			XMLReader xmlReader = sp.getXMLReader();
			
			//SAXParser parser = new SAXParser();
			xmlReader.setContentHandler(new CellHandler(result));
			StringReader s = new StringReader(sourceString);
			InputSource is = new InputSource(s);
			xmlReader.parse(is);
		}
		catch(Exception e){
			System.out.println("Syntax Error in XGMML description:\n" + e.toString());
			e.printStackTrace();
			throw new RuntimeException("Syntax Error in XGMML description:\n" + e.toString());
		}
		Model toreturn = (new Model(result));
		return toreturn;
	}
	
	//makes only xgmml files show up in "open" window
    public static class GraphFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true; // to make visible directories
            }

            String extension = getExtension(f);
            return (extension != null && extension.equals("xgmml"));
        }

        //The description of this filter
        public String getDescription() {
            return "XGMML graph";
        }

        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }
    
}