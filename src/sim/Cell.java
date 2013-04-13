package sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import sim.Genome.GeneType;
import sim.Value.ValueType;

public class Cell {
	
	ChemList chems;
	public Position pos;
	List<Cell> arms;
	Genome genome;
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
		genome = simpleGenome();
	}
	
	public Cell(Cell root, Position pos_, Genome g) {
		this(root, pos_);
		genome = g;
		genome.setCell(this);
	}
	
	public int size() {
		return (int) 0.1*chems.amount(World.WATER)+chems.amount(World.FOOD);
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
	
	public int score() {
		int sc = 2 + chems.amount(World.ENERGY);
		for(int i = 1; i < arms.size(); i++) {
			sc += arms.get(i).score();
		}
		return sc;
	}
	

	public boolean spawnCellAbs(Position p) {
//		if(Simulator.world.inShadow(p.x)) return false;
		Cell c = new Cell(this, p, genome);
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
		if(Simulator.LOG_CELLS) Simulator.world.log("\t"+t);
	}
	
	public void draw(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		int red = Math.max(100,2*Math.min(age, 30));
		int green = 255-Math.min(150,age*10);
		int blue = Math.min(255, 100+10*chems.amount(World.ENERGY)*red);
		g2.setColor(new Color(red, green, blue));
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
	private boolean giveChem(int cell, int chem) {
		return giveChem(cell,chem,1);
	}
	/**
	 * @param cell
	 * @param chem
	 * @param amount
	 * @return success
	 */
	private boolean giveChem(int cell, int chem, int amount) {
		Cell c = getArm(cell);
		return giveChem(c,chem,amount);
	}
//	private int giveChem(Cell c, int chem) {
//		return giveChem(c, chem,1);
//	}
	public boolean giveChem(Cell c, int chem, int amount) {
		if(c != null) {
			while(amount > 0) { // decreasing amount until it works
				if(chems.changeAmount(chem,-1*amount)) {
					c.chems.changeAmount(chem,amount);
					return true;
				}
				amount--;
			}
		}
		return false; // no amount worked
	}

	public void diffuse() {
		for(Cell c : arms) {
			if(c != null)
			for(int i = -9; i < ChemList.NUMCHEMS; i++) {
				if(chems.amount(i) > c.chems.amount(i) + 1) {
					giveChem(c,i,1);
				}
			}
		}
	}
	
	public Genome simpleGenome() {
		Genome gene = new Genome(this, GeneType.GENOME);

//  	if numarms < 2 spawn tip
		Genome spawn_full = new Genome(this,GeneType.IF);
		
		Value ars = new Value(this,ValueType.NUMARMS);
		Value two = new Value(this,ValueType.CONSTANT);
		two.constant = 2;
		Value minus = new Value(this,ValueType.NEGATIVE);
		minus.values.add(ars);
		Value sum = new Value(this,ValueType.SUM);
		sum.values.add(minus);
		sum.values.add(two);
		spawn_full.values.add(sum);

		Genome spawn_tip = new Genome(this, GeneType.GENOME_SR);
		
		Genome mt = new Genome(this,GeneType.METABOLIZE);
		Value mnum = new Value(this,ValueType.CONSTANT);
		mnum.constant = 1;
		mt.values.add(mnum);
		spawn_tip.genes.add(mt);
		
		Genome spawn = new Genome(this,GeneType.SPAWNCELL_UP);
		Value dist = new Value(this,ValueType.CONSTANT);
		dist.constant = 10;
		Value ang = new Value(this,ValueType.CONSTANT);
		ang.constant = 0;
		spawn.values.add(ang);
		spawn.values.add(dist);
		spawn_tip.genes.add(spawn);
		
		spawn_full.genes.add(spawn_tip);
		gene.genes.add(spawn_full);
		return gene;
	}
	
	public boolean act(boolean recurse) {
		log("Me: "+ this);
		log("Arms: "+arms);
		log(chems.listChems());
		return genome.act();
	}


	public boolean metabolize() {
		if(chems.amount(World.FOOD) > 10 &
		chems.amount(World.LIGHT) > 10 &
		chems.amount(World.WATER) > 10) {
			chems.changeAmount(World.ENERGY, 1);
			chems.changeAmount(World.FOOD, -10);
			chems.changeAmount(World.LIGHT, -10);
			chems.changeAmount(World.WATER, -10);
			return true;
		}
		return false;
	}


}