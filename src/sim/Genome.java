package sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sim.Value.ValueType;

public class Genome{
	List<Genome> genes;
	List<Value> values;
	Cell cell;
	
	
	public enum GeneType {GENOME, GENOME_SR, IF, //FOR, 
		METABOLIZE, GIVECHEM, TESTVALUE, SPAWNCELL_ROOT, SPAWNCELL_UP };
	public GeneType type;
	
	public Genome(Cell c, GeneType t_) {
		cell = c;
		type = t_;
		genes = new ArrayList<Genome>();
		values = new ArrayList<Value>();
	}
	
	public int numValues() {
		switch(type) {
		case TESTVALUE:
			return 1;
		case GENOME:
		case GENOME_SR:
			return 0;
		case IF:
			return 1;
//		case FOR:
//			return 3;
		case SPAWNCELL_ROOT:
		case SPAWNCELL_UP:
			return 2;
		case METABOLIZE:
			return 1;
		case GIVECHEM:
			return 2;
		}
		return 0;
	}
	
	public int numGenes() {
		switch(type) {
		case GENOME:
		case GENOME_SR:
			return genes.size();
		case IF:
			return 1;
//		case FOR:
//			return 1;
		case SPAWNCELL_UP:
		case SPAWNCELL_ROOT:
			return 0;
		case METABOLIZE:
			return 0;
		case GIVECHEM:
			return 0;
		case TESTVALUE:
			return 0;
		}
		return 0;
	}
	
	public String toString(int tabs) {
		String out = "";
		for(int i = 0; i < tabs; i++) out += "\t";
		out += "[";
		switch(type) {
		case GENOME:
			out += "Gene";
			break;
		case GENOME_SR:
			out += "GeneSR";
			break;
		case IF:
			out += "If";
			break;
//		case FOR:
//				out += "";
//				break;		
		case SPAWNCELL_UP:
			out += "SpwnUp";
			break;
		case SPAWNCELL_ROOT:
			out += "SpwnRt";
			break;
		case METABOLIZE:
			out += "Metabolize";
			break;
		case GIVECHEM:
			out += "Give";
			break;
		case TESTVALUE:
			out += "Test";
			break;
		}
		out += "]\n";
		for(int i = 0; i < numValues(); i++) out += values.get(i).toString(tabs + 1);
		for(int i = 0; i < numGenes(); i++) out += genes.get(i).toString(tabs + 1);
		return out;
	}
	
	public void setCell(Cell c) {
		cell = c;
		for(Value v : values) v.setCell(c);
		for(Genome g : genes) g.setCell(c);
	}
	
	public boolean act() {
		if(type == GeneType.GENOME || type == GeneType.GENOME_SR) {
			for(Genome gene : genes) {
				if(gene.act() || type == GeneType.GENOME) {
					continue;
				} else return false;
			}
			return true;
		}
		
		if(values.size() != numValues() || genes.size() != numGenes()) {
			cell.log("Gene size error");
			return false;
		}
		
		double[] vals = new double[values.size()];
		for(int i = 0; i < values.size(); i++) {
			vals[i] = values.get(i).val();
		}
		
		
		switch(type) {
		case IF:
			if(vals[0] > 0) {
				return genes.get(0).act();
			}
			
		case TESTVALUE:
			 return (vals[0] > 0);
			 
//		case FOR:
//			boolean ret = true;
//			for(double i = vals[0]; i < vals[1]; i += vals[2]) {
//				ret &= genes.get(0).act();
//			}
//			return ret;
			 
		case SPAWNCELL_UP:
			double x, y;
			x = (3+vals[1])*Math.cos(vals[0] + Math.PI/2);
			y = -1*(3+vals[1])*Math.sin(vals[0] + Math.PI/2);
			Position p = new Position(x, y);
			return cell.spawnCellRel(p);
			
		case SPAWNCELL_ROOT:
			Cell root = cell.arms.get(0);
			if(root != null) {
				double theta = Math.atan((cell.pos.y-root.pos.y)/(cell.pos.x-root.pos.x));
				x = (3+vals[1])*Math.cos(vals[0] + theta);
				y = -1*(3+vals[1])*Math.sin(vals[0] + theta);
			}
			
		case GIVECHEM:
			return cell.giveChem(cell, (int)vals[0], (int)vals[1]);
			
		case METABOLIZE:
			return cell.metabolize();
			
		default:
			break;
		}	
		return false;
	}
	

	public boolean mutate(double chance) {
		Random rgen = new Random();
		boolean mutated = false;
		
		if(rgen.nextDouble() < chance) {
//			change genetype
			List<GeneType> types = Arrays.asList(GeneType.values());
			type = types.get(rgen.nextInt(types.size()));
			mutated = true;
		}
		if(rgen.nextDouble() < chance & genes.size() > 0) {
//			swap gene order
			Genome g1 = genes.get(rgen.nextInt(genes.size()));
			Genome g2 = genes.get(rgen.nextInt(genes.size()));
			Genome t = g1;
			g1 = g2;
			g2 = t;
			mutated = true;
		}//		
		
		
		for(Genome g : genes) {
			g.mutate(chance);
		}
		for(Value v : values) {
			v.mutate(chance);
		}
		
		while(values.size() < numValues()) {
			Value zero = new Value(cell,ValueType.CONSTANT);
			zero.constant = 0;
			values.add(zero);
		}
		while(values.size() > numValues()) {
			values.remove(values.size() - 1);
		}
		
		while(genes.size() < numGenes()) {
			Genome test = new Genome(cell,GeneType.TESTVALUE);
			Value one = new Value(cell,ValueType.CONSTANT);
			one.constant = 1;
			test.values.add(one);
			genes.add(test);
		}
		while(genes.size() > numGenes()) {
			genes.remove(genes.size() - 1);
		}

		return mutated;
	}

	public Genome copy() {
		Genome cp = new Genome(cell,type);
		
		for(Genome g : genes) {
			cp.genes.add(g.copy());
		}
		for(Value v : values) {
			cp.values.add(v.copy());
		}
		return cp;
	}
}
