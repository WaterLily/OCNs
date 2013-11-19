
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.event.*;

public class View extends JPanel implements ActionListener{

	private int tileD;
	private Model mod;
	private final int W;
	private final int H;
	
	private boolean varyThickness = true;
	private boolean showArea = false;
	private boolean showVolume = false;
	private boolean showUPL = false;
	private int supportArea = 1;
	private int viewDepth = 0; //slice of 3D network to view
	
	public void toggleThickness(){
		varyThickness = !varyThickness;
		repaint();
	}
	
	public void toggleAreas(){
		showArea = !showArea;
		repaint();
	}
	public void toggleVolumes(){
		showVolume = !showVolume;
		repaint();
	}
	public void toggleUPLs(){
		showUPL = !showUPL;
		repaint();
	}
	
	public void support(int area){
		supportArea = area;
		repaint();
	}
	public void viewDepth(int d){
		viewDepth = d;
		repaint();
	}

	public View(Model mod) {
		this.mod = mod;
		mod.addActionListener(this);
		W = mod.width;
		H = mod.height;
		setSize(new Dimension(500, 500));
		
		tileD = Math.min(getWidth()/W, getHeight()/H);
		
		setPreferredSize(new Dimension(W*tileD, H*tileD));
		
		JFrame window = new JFrame(W + " by " + H);
		window.setSize(new Dimension(W*tileD, H*tileD));
		//Main.pr("dimensions: " + W*tileD + ", " + H*tileD);
		//window.setPreferredSize(new Dimension(W*tileD, H*tileD));
		window.setLocationRelativeTo(null);
		window.add(this);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void actionPerformed(ActionEvent ae) {
		//Main.pr("performing action");
		repaint();
	}
	
	public void paint(Graphics g) {
		super.paintComponent(g);
		//g.setColor(Color.LIGHT_GRAY);
		//g.fillRect(0, 0, W, H);
		//Main.pr("painting");
		
		tileD = Math.min(getWidth()/W, getHeight()/H);
		/*
		g.setColor(Color.LIGHT_GRAY);
		for (int column = 0; column<W; ++column)
			g.drawLine(column*tileD, 0, column*tileD, H*tileD);
		
		for (int row = 0; row<H; ++row)
			g.drawLine(0, row*tileD, W*tileD, row*tileD);
		*/
		
		g.setColor(Color.BLACK);
		Graphics2D g2 = (Graphics2D) g;
		
		
		if(viewDepth==mod.outlet.z)
			g.fillOval(mod.outlet.x*tileD + tileD/2 - 10, mod.outlet.y*tileD + tileD/2 - 10, 20, 20);
		
		for (int column = 0; column<W; ++column) {
			for (int row = 0; row<H; ++row) {
				int startX = column*tileD + tileD/2;
				int startY = row*tileD + tileD/2;
				Cell cell = mod.getCell(column, row, viewDepth);
				if(varyThickness){
					float thickness = (float)(Math.log(cell.area()));
					//assert(thickness>=0): "neg thickness: " + cell.area + "; " + thickness;
					if(thickness<0) Main.pr("neg thickness: " + cell + ", " + cell.area + "; " + thickness);
					//Main.pr(thickness);
					g2.setStroke(new BasicStroke(thickness));
				}
				//Main.pr(cell.area + ", " + thickness);
				if(cell.area<supportArea) continue;
				Tuple pair = cell.getDD().place.minus(cell.place); //drainage direction
				if(cell.getDD().place.z==viewDepth)g2.drawLine(startX, startY, startX + pair.x*tileD, startY + pair.y*tileD);
				if(showArea) g2.drawString(cell.area() + "", startX, startY);
				if(showVolume) g2.drawString(cell.volume() + "", startX, startY);
				if(showUPL) g2.drawString(cell.upl() + "", startX, startY);
				//g.fillOval(startX + pair.x*tileD/10 -3, startY + pair.y*tileD/10 - 3, 5, 5);
			}
		}
	}
}
