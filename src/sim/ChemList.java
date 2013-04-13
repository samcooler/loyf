package sim;

public class ChemList {
	private int[] list;
	private int[] resourceList;
	public static int NUMCHEMS = 100;
	

	public ChemList() {
		NUMCHEMS = 100;
		list = new int[NUMCHEMS];
		resourceList = new int[10];
	}
	public int amount(int index) {
		if(index < -9 || index > NUMCHEMS) return 0;
		if(index < 0) return resourceList[-1*index];
		return list[index];
	}
	public boolean changeAmount(int index, int amt) {
		if(0 <= index & index < NUMCHEMS) {
			if(list[index] + amt >= 0) {
				list[index] += amt;
				return true;
			}
		} else if(0 > index & index > -10) {
			if(resourceList[-1*index] + amt >= 0) {
				resourceList[-1*index] += amt;
				return true;
			}
		}
		return false;
	}
	public String listChems() {
		String out = "res(";
		for(int i = -9; i < NUMCHEMS; i++) {
			if(amount(i) > 0)
				out += name(i) + ":"+amount(i)+",";
		}
		
		return out+")";
	}
	
	public String name(int type)
	{
		switch(type) {
		case sim.World.ENERGY:
			return "En";
		case sim.World.FOOD:
			return "Fd";
		case sim.World.WATER:
			return "Wt";
		case sim.World.LIGHT:
			return "Li";
		default:
			return "Sg" + type;
		}
	}

}