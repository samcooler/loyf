package sim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


public class Region {
	Color color;
	Position a,b;
	ChemList chems;
	
	public Region() {
		super();
	}
	
	public Region(Position a_, Position b_) {
		a = a_;
		b = b_;
		chems = new ChemList();
		color = Color.cyan;
	}
	public Region(Position a_, Position b_, Color c_) {
		this(a_,b_);
		color = c_;
	}
	public Region(double ax, double ay, double bx, double by) {
		this();
		a = new Position(ax,ay);
		b = new Position(bx,by);
	}
	
	public boolean contains(Position p) {
		return a.x <= p.x & p.x <= b.x & a.y <= p.y & p.y <= b.y;
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(color);
		g2.fill(new Rectangle2D.Double(a.x, a.y, b.x-a.x, b.y-a.y));
//		System.out.println((a.x+b.x)/2 + " " + (a.y+b.y)/2 + " " + (b.x-a.x) + " " + (b.y-a.y));
	}

}
