package sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sim.Genome.GeneType;

public class Value{
	List<Value> values;
	public enum ValueType {CONSTANT, RANDOM, RANDOMG, NUMARMS, AGE, CHEMICAL, SUM, EXPONENT, NEGATIVE, INVERSE};
	public ValueType type;
	public double constant;
	private Cell cell;
	Random rgen;

	
	
	public Value(Cell c) {
		super();
		cell = c;
		rgen = new Random();
		values = new ArrayList<Value>();
		constant = 0;
	}
	
	public Value(Cell c_, ValueType vt) {
		this(c_);
		type = vt;
	}

	public boolean setType(ValueType t) {
		if(values.size() == numValues()) {
			type = t;
			return true;
		}
		return false;
	}
	
	public int numValues() {
		switch(type) {
		case CONSTANT: return 0;
		case RANDOM: return 2;
		case RANDOMG: return 2;
		case AGE: return 0;
		case CHEMICAL: return 1;
		case SUM: return 2;
		case EXPONENT: return 2;
		case NEGATIVE: return 1;
		case INVERSE: return 1;
		case NUMARMS: return 0;
		default:
			break;
		}
		return 0;
	}
	
	public String toString(int tabs) {
		String out = "";
		for(int i = 0; i < tabs; i++) out += "\t";
		out += "(";
		switch(type) {
		case CONSTANT:
			out += constant;
			break;
		case RANDOM:
			out += "Rand";
			break;
		case RANDOMG:
			out += "RandG";
			break;
		case AGE:
			out += "Age";
			break;
		case CHEMICAL:
			out += "Chem";
			break;
		case SUM:
			out += "Sum";
			break;
		case EXPONENT:
			out += "Exp";
			break;
		case NEGATIVE:
			out += "Neg";
			break;
		case INVERSE:
			out += "Inv";
			break;
		case NUMARMS:
			out += "Arms";
			break;
		default:
			break;
		}
		out += ")\n";
		for(int i = 0; i < numValues(); i++) out += values.get(i).toString(tabs + 1);
		return out;
	}
	

	public double val() {
		
		if(values.size() != numValues()) { return 0; }

		switch(type) {
		case CONSTANT:
			return constant;
		case RANDOM: // center and deviation
			return rgen.nextDouble()*(values.get(1).val()-values.get(0).val()) + values.get(0).val();
		case RANDOMG: // center and variance
			return rgen.nextGaussian()*values.get(1).val() + values.get(0).val();
		case NUMARMS: 
			return cell.arms.size();
		case AGE:
			return cell.age;
		case CHEMICAL:
			return cell.chems.amount((int)values.get(0).val());
		case SUM:
			return values.get(0).val()+values.get(1).val();
		case EXPONENT:
			return Math.pow(values.get(0).val(),values.get(1).val());
		case NEGATIVE:
			return -1*values.get(0).val();
		case INVERSE:
			double t = values.get(0).val();
			return t != 0 ? 1.0/t : Double.MAX_VALUE;
		}
		return 0;
	}

	public boolean mutate(double chance) {
		Random rgen = new Random();
		boolean mutated = false;
		
		if(rgen.nextDouble() < chance) {
//			change genetype
			List<ValueType> types = Arrays.asList(ValueType.values());
			type = types.get(rgen.nextInt(types.size()));
		}
		if(rgen.nextDouble() < chance & values.size() > 0) {
//			swap value order
			Value g1 = values.get(rgen.nextInt(values.size()));
			Value g2 = values.get(rgen.nextInt(values.size()));
			Value t = g1;
			g1 = g2;
			g2 = t;
		}
		if(rgen.nextDouble() < chance) {
//			change constant
			constant = 10*rgen.nextGaussian();
		}
//		
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
		return mutated;
	}
	
	public Value copy() {
		Value cp = new Value(cell,type);
		cp.constant = constant;
		for(Value v : values) {
			cp.values.add(v.copy());
		}
		return cp;
	}

	public void setCell(Cell c) {
		cell = c;
		for(Value v : values) v.setCell(c);
	}


}
