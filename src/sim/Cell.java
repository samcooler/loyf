package sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Cell {
	
	ChemList chems;
	public Position pos;
	List<Cell> arms;
	public int age;
	public Double[] shadowRange;

	public Cell(Cell root, Position pos_) {
		chems = new ChemList();
		pos = pos_;
		arms = new ArrayList<Cell>();
		arms.add(root);
		age = 0;
		shadowRange = new Double[2];
		shadowRange[0] = pos.x;
		shadowRange[1] = pos.x;
	}
	
	public int size() {
		return chems.amount(World.WATER)+chems.amount(World.FOOD)+chems.amount(World.ENERGY);
	}
	
	public Double[] shadow() {
		for(Cell c : arms) {
			if(c != null) {
				shadowRange[1] = Math.max(pos.x, c.shadow()[1]);
				shadowRange[0] = Math.min(pos.x, c.shadow()[0]);
			}
		}
		return shadowRange;
	}
	

	public boolean spawnCellAbs(Position p) {
		if(Simulator.world.inShadow(p.x)) return false;
		Cell c = new Cell(this, p);
		log("spawning arm at "+p.x+","+p.y);
		Simulator.world.newCells.add(c);
		arms.add(c);
		
		return true;
	}
	public boolean spawnCellRel(Position p) {
		if(chems.amount(World.ENERGY) > 0)
		{
			if(spawnCellAbs(pos.add(p)))
			{
				chems.changeAmount(World.ENERGY, -1);
				return true;
			}
		}
		return false;
	}
	
	public void log(String t){
		Simulator.world.log("\t"+t);
	}
	
	public void draw(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(new Color(Math.max(100,2*Math.min(age, 30)), 255-Math.min(150,age*10), 100));
//		log(String.valueOf(age));
		g2.fill(new Ellipse2D.Double(pos.x-size()/2, pos.y-size()/2, size(), size()));
//		log("Drawing cell. diam:"+diameter+" at (" + pos.x +"," + pos.y+")");
		
		for (int i = 1; i < arms.size(); i++) {
//			draw arms
//			log("Drawing arm " + i);
			Cell c = arms.get(i);
			float strokeWidth = Math.min(size(), c.size());
			Stroke stroke = new BasicStroke(strokeWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			g2.setStroke(stroke);
			g2.draw(new Line2D.Double(pos.x, pos.y, c.pos.x, c.pos.y));
		}
		
//		g2.setColor(Color.black);
//		g2.drawString(chems.listChems(), (float)pos.x, (float)pos.y);
		
		return;
	}

	
	Cell getArm(int index) {
		try {
			return arms.get(index);
		}
		catch(java.lang.IndexOutOfBoundsException ex) {
		}
		return null;
	}

	/**
	 * @param cell
	 * @param chem
	 * @return success
	 */
	private int giveChem(int cell, int chem) {
		return giveChem(cell,chem,1);
	}
	/**
	 * @param cell
	 * @param chem
	 * @param amount
	 * @return success
	 */
	private int giveChem(int cell, int chem, int amount) {
		Cell c = getArm(cell);
		return giveChem(c,chem,amount);
	}
	private int giveChem(Cell c, int chem) {
		return giveChem(c, chem,1);
	}
	private int giveChem(Cell c, int chem, int amount) {
		if(c != null) {
			while(amount > 0) { // decreasing amount until it works
				if(chems.changeAmount(chem,-1*amount)) {
					c.chems.changeAmount(chem,amount);
					return amount;
				}
				amount--;
			}
		}
		return 0; // no amount worked
	}

	public void diffuse() {
		for(Cell c : arms) {
			if(c != null)
			for(int i = -9; i < 0; i++) {
				if(chems.amount(i) > c.chems.amount(i)) {
					giveChem(c,i,World.DIFFUSIONRATE);
				}
			}
		}
	}
	
	public void act(boolean recurse) {
//			log("Acting");
			log("Arms: "+arms.size());
			log(chems.listChems());

			// make chemical at base only, and only if none above
			if(getArm(0) == null & getArm(1) == null) {
				log("making growth signal at base");
				chems.changeAmount(1,1); // base
				chems.changeAmount(20,1); // upward tip
				chems.changeAmount(30,1); // downward tip
			}

			// diffusion
			diffuse();
			
			// pushes
			if(chems.amount(1) < 1)
			{
				if(chems.amount(2) > 0) {
					giveChem(0, World.LIGHT,2);
					giveChem(1, World.FOOD,1);
					giveChem(1, World.WATER,1);
				}
				if(chems.amount(3) > 0) {
					giveChem(1, World.LIGHT,1);
					giveChem(0, World.FOOD,1);
					giveChem(0, World.WATER,1);
				}
			}

					
	//		if I am the tip, make a new cell in front of me
			Random rgen = new Random();
			if(chems.amount(20) > 0) {
				metabolize();
				int x = rgen.nextInt(4)-2;
				Position p = new Position(x, -10);
				if(spawnCellRel(p)) {
	//				give to cell 1 (toward tip) chemical 1 (tip marker)
					if(giveChem(1,20) > 0)
						chems.changeAmount(2, 1);
				}
			}
			// upward
			if(chems.amount(30) > 0) {
				metabolize();
				Position p = new Position(rgen.nextInt(20)-10, 10);
				if(spawnCellRel(p)) {
		//			give to cell 2 (toward tip) chemical 2 (tip marker)
					int child = 1;
					if(chems.amount(1) == 1) child = 2; // only base has 3 "arms"
					if(giveChem(child,30) > 0)
						chems.changeAmount(3,1);
				}
				
			}
			log(String.valueOf(chems.amount(World.ENERGY)));
			if(arms.size() == 2) {
				if(chems.amount(World.ENERGY) == 0) metabolize();
				Position p = new Position(rgen.nextInt(100)-50, 0);
				spawnCellRel(p);
			}

				
			
			if(recurse) for (int i = 1; i < arms.size(); i++) {
				getArm(i).act(true);
			}
	
		}


	private boolean metabolize() {
		if(chems.amount(World.FOOD) > 0 &
		chems.amount(World.LIGHT) > 0 &
		chems.amount(World.WATER) > 0) {
			chems.changeAmount(World.ENERGY, 1);
			chems.changeAmount(World.FOOD, -1);
			chems.changeAmount(World.LIGHT, -1);
			chems.changeAmount(World.WATER, -1);
			return true;
		}
		return false;
	}


}