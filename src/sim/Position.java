package sim;

public class Position {
	public double x,y;

	public Position(double x_, double y_) {
//		x = Math.max(Math.min(0, x_),Simulator.WIDTH);
//		y = Math.max(Math.min(0, y_),Simulator.HEIGHT);
		x = Math.max(Math.min(x_, Simulator.WIDTH + 1), 0);
		y = y_;
	}
	public Position add(Position p) {
		return new Position(x + p.x, y + p.y);
	}

}
