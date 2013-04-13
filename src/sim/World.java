package sim;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sim.Genome.GeneType;

public class World {
	
	public Map<Cell, Double[]> plants;
	public List<Cell> cells;
	public List<Cell> newCells;
	public List<Region> regions;
	public static final int ENERGY = -1;
	public static final int FOOD = -2;
	public static final int WATER = -3;
	public static final int LIGHT = -4;
	public static final int DIFFUSIONRATE = 1;
	public static final double GROUND = Simulator.HEIGHT*0.9;

	public World() {
		plants = new HashMap<Cell,Double[]>();
		regions = new ArrayList<Region>();
		cells = new ArrayList<Cell>();
		newCells = new ArrayList<Cell>();
		
		Position origin = new Position(0,0);
		Position a = new Position(0.0,Simulator.HEIGHT/2.0);
		Position b = new Position(Simulator.WIDTH,GROUND);
		Position c = new Position(Simulator.WIDTH,Simulator.HEIGHT);
		
		Region earth = new Region(a, c, new Color(159, 139, 112));
		earth.chems.changeAmount(World.WATER,1);
		earth.chems.changeAmount(World.FOOD,1);
		Region sky = new Region(origin, b, new Color(135, 206, 250));
		sky.chems.changeAmount(World.LIGHT,1);		
		regions.add(earth);
		regions.add(sky);
	}
	
	public void addCells(List<Cell> c) {

	}
	public void removeCells(List<Cell> c) {
		cells.removeAll(c);
	}
	
	public void draw(Graphics g)
	{
		for(Region reg : regions) {
			reg.draw(g);
		}
		for(Cell cell : cells) {
			cell.draw(g);
		}
		return;
	}

	public void addCell(Cell cel) {
		cells.add(cel);
	}
	public void addPlant(Cell cel) {
		plants.put(cel, cel.shadow());
		addPlantCells(cel);
	}
	public void addPlantCells(Cell cel) {
		cells.add(cel);
		for(int i = 1; i < cel.arms.size(); i++)
		{
			if(cel.getArm(i) != null)
				addPlantCells(cel.getArm(i));
		}
	}
	
	public void tallyScore() {
		for (Map.Entry<Cell,Double[]> entry : plants.entrySet())
		{
			log("sc:"+entry.getKey().score());
		}
	}
	
	public void step(int st)
	{

		int count = 0;
		newCells.clear();
	    for(Cell c : cells) {
	    	if(Simulator.LOG_WORLD) log("Cell "+count++);
	    	environment(c);
	    	c.diffuse();
	    	c.act(false);
	    	c.age++;
			if(st == 0 & Simulator.LOG_GENES) log(c.genome.toString(0));
	    }
//		for (Map.Entry<Cell,Double[]> entry : plants.entrySet())
//		{
//			entry.getKey()
//		}
		log("adding "+newCells.size()+" cells after step");
		cells.addAll(newCells);
	    log("World now has "+cells.size()+" cells");
	    log(".");
		return;
	}
	
	
	public Genome mixGenomes(List<Genome> glist) {
		Genome child = new Genome(null, GeneType.GENOME);
		class Parent {int i; Genome g;}
		List<Parent> plist = new ArrayList<Parent>();
		for(Genome g : glist) {
			Parent p = new Parent();
			p.i = 0;
			p.g = g.copy();
			plist.add(p);
		}
		
		double chance = 1/plist.size();
		Random rgen = new Random();
		int curParent = 0;
//		loop through all parents
//			pull in genomes repeatedly while lucky
//			once unlucky next parent
		while(plist.size() > 0) {
			curParent = rgen.nextInt(plist.size() - 1);
			Parent parent = plist.get(curParent);
			while(rgen.nextDouble() < chance) { // needs to be fixed (this will draw all) and make it safer
				child.genes.add(parent.g.genes.get(parent.i));
				parent.i++;
				if(parent.i == parent.g.genes.size()) plist.remove(parent);
			}
			curParent++;
			curParent %= plist.size();
		}
		return child;
	}

	private void environment(Cell c) {
		for(Region r : regions) {
			if(r.contains(c.pos)) {
				for(int i = -9; i < ChemList.NUMCHEMS; i++) {
//					if(c.chems.amount(i) < 10)
						c.chems.changeAmount(i,r.chems.amount(i));
				}
			}
		}
		
	}

	public void updateShadows() {
		for (Map.Entry<Cell,Double[]> entry : plants.entrySet())
		{
			Double[] newShadow = entry.getKey().shadow();
		    entry.setValue(newShadow);
		 
		}
	}
	
	public boolean inShadow(double p) {
		// shittysearch
		for (Map.Entry<Cell,Double[]> entry : plants.entrySet())
		{
			if(entry.getKey().pos.x > entry.getValue()[0] &
					entry.getKey().pos.x < entry.getValue()[1]) {
				return true;
			}
		}
		return false;
	}
	
	public void log(String l) {
		System.out.println(l);
		
	}


}
