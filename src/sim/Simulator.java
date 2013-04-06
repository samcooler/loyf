package sim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;

public class Simulator {
	
	public static int HEIGHT = 1000;
	public static int WIDTH = 600;
	public static double SCALE = 2.0;
	public static int NUMPLANTS = 10;
	
	public static World world;
	
	public Simulator() {
		super();
	}
	
	/**
	 * @param WITDH, HEIGHT
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
	    
	    System.out.println("birthing Jims\n");
	    Random rgen = new Random();
	    for(int i = 0; i < NUMPLANTS; i++) {
	    	Position seedPosition = new Position(WIDTH*rgen.nextDouble(),HEIGHT/2.0-1);
		    Cell jim = new Cell(null, seedPosition);
		    jim.chems.changeAmount(World.FOOD, 5);
		    jim.chems.changeAmount(World.WATER, 5);
		    world.addPlant(jim);
	    }

    
	    int st = 300;
		int stepCount = 0;
	    while(true) {
	    	stepCount ++;
	    	g2.setColor(Color.black);
	    	g2.fill(new Rectangle2D.Double(0,0,WIDTH,HEIGHT));
	    	
	    	world.log("\nstep " + stepCount);
	    	world.step();
			world.draw(g2);
			
		    f2.drawImage(screen,0,0,null);
		    
	    	try {
				Thread.sleep(st);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	  
    
	    
	}
	
	
	

}
