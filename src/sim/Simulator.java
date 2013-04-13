package sim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;

public class Simulator {
	
	public static boolean LOG_CELLS = false;
	public static boolean LOG_WORLD = false;
	public static boolean LOG_GENES = true;
	public static int HEIGHT = 600;
	public static int WIDTH = 600;
	public static double SCALE = 2.0;
	public static int NUMPLANTS = 5;
	public static double NEWRATIO = 0.5;
	public static double MUTATIONCHANCE = 0.15;
    public static int stepSleepTime = 0; //ms
    public static int loyfSleepTime = 100;
    public static int stepsPerLife = 100;
	
	public static World world;
	public static Map<Genome, Double> bestPlants;
	
	public Simulator() {
		super();
	}
	
	/**
	 * @param WIDTH, HEIGHT
	 */
	public static void main(String[] args) {
		if(args.length == 2) {
			WIDTH = Integer.valueOf(args[0]);
			HEIGHT = Integer.valueOf(args[1]);
		}
		
        JFrame frame = new JFrame("BasicPanel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH,HEIGHT);

        BasicPanel panel = new BasicPanel();
        frame.setContentPane(panel);          
        frame.setVisible(true); 
        
        Graphics2D f2 = (Graphics2D) frame.getGraphics();
       
	    BufferedImage screen = new BufferedImage(WIDTH,HEIGHT,java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
	    Graphics2D g2 = (Graphics2D) screen.createGraphics();
	        
	    
		System.out.println("Starting World\n");
	    world = new World();
	    bestPlants = new HashMap<Genome,Double>();
	    

	    while(true) {
//	    	Create next round of loyf
	    	world.log("########################\nNext round");
	    	world.cells.clear();
	    	world.plants.clear();
		    System.out.println("birthing Jims\n");
		    Random rgen = new Random();
		    for(int i = 0; i < NUMPLANTS; i++) {
		    	Position seedPosition = new Position((i+.5) * WIDTH / NUMPLANTS,World.GROUND);
			    Cell jim = new Cell(null, seedPosition);
			    jim.chems.changeAmount(World.FOOD, 5);
			    jim.chems.changeAmount(World.WATER, 5);
			    
			    world.log("Best count: "+bestPlants.size());
			    if(i < NUMPLANTS * NEWRATIO) {
			    	jim.genome.mutate(MUTATIONCHANCE);
			    } else {
			    	if(bestPlants.size() > NUMPLANTS) {
				    	List<Genome> bests = new ArrayList<Genome>(bestPlants.keySet());
				    	List<Genome> b = new ArrayList<Genome>();
				    	b.add(bests.get(rgen.nextInt(bests.size() - 1)));
				    	b.add(bests.get(rgen.nextInt(bests.size() - 1)));
				    	jim.genome = world.mixGenomes(b);
			    	}
			    }
			    world.addPlant(jim);
		    }
		    
//		    Simulate loyf
			int stepCount = 0;
		    while(stepCount < stepsPerLife) {
		    	
		    	g2.setColor(Color.black);
		    	g2.fill(new Rectangle2D.Double(0,0,WIDTH,HEIGHT));
		    	
		    	world.log("\nstep " + stepCount);
		    	world.step(stepCount);
				world.draw(g2);
				
			    f2.drawImage(screen,0,0,null);
			    stepCount ++;
			    if(stepSleepTime == 0) continue;
		    	try {
					Thread.sleep(stepSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		    
//		    Score plants
		    world.tallyScore();
		    
			for (Map.Entry<Cell,Double[]> entry : world.plants.entrySet())
			{
				int score = entry.getKey().score();
				world.log("sc:"+score);
				if(score > 25) bestPlants.put(entry.getKey().genome, (double) score);
			}
	    	try {
				Thread.sleep(loyfSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		  
	    }
	    
	}
	
	
	

}
