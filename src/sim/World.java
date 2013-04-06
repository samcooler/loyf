package sim;

import java.awt.Color;
import java.awt.Graphics;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public World() {
		plants = new HashMap<Cell,Double[]>();
		regions = new ArrayList<Region>();
		cells = new ArrayList<Cell>();
		newCells = new ArrayList<Cell>();
		
		Position origin = new Position(0,0);
		Position a = new Position(0.0,Simulator.HEIGHT/2.0);
		Position b = new Position(Simulator.WIDTH,Simulator.HEIGHT/2.0);
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
		cells.addAll(c);
		log("adding "+c.size()+" cells after step");
	}
//	public void removeCells(List<Cell> c) {
//		plants.removeAll(c);
//	}
	
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
	
	
	
	public void step()
	{
		int count = 0;
		newCells.clear();
	    for(Cell c : cells) {
	    	log("Cell "+count++);
	    	environment(c);
	    	
	    	c.act(false);
	    	c.age++;
	    }
	    addCells(newCells);
	    log("World now has "+count+" cells");
	    log(".");
		return;
	}

	private void environment(Cell c) {
		for(Region r : regions) {
			if(r.contains(c.pos)) {
				for(int i = -9; i < ChemList.NUMCHEMS; i++) {
					if(c.chems.amount(i) < 10)
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
