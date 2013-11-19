import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
public class RunTests{

	static Tuple[] dimensions;
	static int[] volumes;
	static double[] energies;
	static int[] lengths;
	static int[] amounts;
	static int[] amountsLengths;
	static double[] cdfArea;
	static double[] cdfLength;
	static double gamma = 0.5;

	public static void main(String[] args){
	//int[] Ls = {/*10, 20, 30, 40, 50, 70,*/ 80};
	//int[] Ls = {8, 10, 12, 14, 16, 18, 20};
	int[] Ls = {16};
	for(int t = 0; t<Ls.length; t++){
		Tuple dim = new Tuple(Ls[t], Ls[t], Ls[t]);
		Tuple outlet = new Tuple(0, 0, 0);
		int runTimes = 4;///////
		int optTimes = -1;////////this makes the model run till convergence
		int maxArea = (dim.x)*(dim.y)*(dim.z==0?1:dim.z);
		volumes = new int[maxArea+1];
		energies = new double[maxArea+1];
		amounts = new int[maxArea+1]; //area frequency
		lengths = new int[maxArea+1]; //lengths according to areas
		amountsLengths = new int[maxArea+1]; //length frequency
		cdfArea = new double[maxArea+1];
		cdfLength = new double[maxArea+1];
		//Model eve = new Model(dim.x, dim.y, dim.z, outlet);
		for(int i = 0; i<runTimes; i++){
			Main.pr("turn " + i);
			Model mod = new Model(dim.x, dim.y, dim.z, outlet);
			//View view = new View(mod);
			mod.randomSeed();
			mod.setGamma(gamma);
			mod.optimize(optTimes, false, false, false);
			mod.calcAreas();
			mod.calcUPLs();
			mod.calcVolumes();
			gamma = mod.getGamma();
			calcEnergy(mod.getCell(outlet.x, outlet.y, outlet.z));
			//Main.pr(mod.evaluate() + "\t" +eMap.get(mod.getCell(outlet.x, outlet.y, outlet.z)));
			for(Cell c : mod.getCells()){
				int a = c.area();
				volumes[a] += c.volume();
				energies[a] += eMap.get(c);
				amounts[a]++;
				int el = c.upl();
				amountsLengths[el]++;
				lengths[a]+=el;
			}
		}
		for(int i  = 1; i<= maxArea; i++){
			for(int j = i-1; j>=0; j--){
				cdfArea[j] += amounts[i];
				cdfLength[j] += amountsLengths[i];
			}
		}
		for(int i  = 1; i<= maxArea; i++){
			cdfArea[i] = cdfArea[i]/(maxArea*runTimes);
			cdfLength[i] = cdfLength[i]/(maxArea*runTimes);
		}
		Main.pr("dim\trunTimes\toptTimes\tgamma");
		Main.pr(dim.x+"x"+dim.y+"x"+dim.z + "\t" + runTimes + "\t" + optTimes + "\t" + gamma);
		Main.pr("area\tvolumes\tenergies\tlengths\tamounts\tcumulative probability (area)\tfrequency (length)\tcumulative probability (length)");
		for(int i = 1; i<= maxArea; i++){
			Main.pr(i + "\t" + (double)volumes[i]/(double)amounts[i] 
					  + "\t" + (double)energies[i]/(double)amounts[i]
					  + "\t" + (double)lengths[i]/(double)amounts[i]
					  + "\t" + amounts[i]
					  + "\t" + cdfArea[i]
					  + "\t" + (double)amountsLengths[i]/(double)(maxArea*runTimes)
					  + "\t" + cdfLength[i]);
		}
	}
		JFrame jf = new JFrame("SIMULATION IS FINISHED");	
		jf.add(new JLabel("SIMULATION IS FINISHED"));
		jf.setLocation(400, 200);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(300, 100);
		jf.setVisible(true);
	}
	static HashMap<Cell, Double> eMap = new HashMap<Cell, Double>();
	public static double calcEnergy(Cell c){
		Double en = eMap.get(c);
		if(!(en==null)) return en;
		double e = 0;
		String toprint = c.place.toString();
		for(Cell source : c.upstream){
			e += Math.pow(source.area, gamma) + calcEnergy(source);
			//toprint += " " + source.place.toString();
		}
		//Main.pr(toprint);
		eMap.put(c, e);
		return e;		
	}
}