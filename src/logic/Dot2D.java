package logic;

public class Dot2D
{
	private double x;
	private double y;
	private boolean onBreak;

	//-------------------------------

	public Dot2D(double x, double y, boolean onBreak)
	{
		super();
		this.x = x;
		this.y = y;
		this.onBreak = onBreak;
	}

	public Dot2D(double x, double y)
	{
		this(x, y, false);
	}

	public String toString()
	{
		return "("+x+", "+y+")";
	}

	//-------------------------------

	public boolean samePoint(Dot2D other)
	{
		if (x == other.getX() && y == other.getY()) return true;
		return false;
	}

	//-------------------------------

	public double distance(Dot2D other)
	{
		return Math.sqrt(Math.pow(x-other.getX(), 2) + Math.pow(y-other.getY(), 2));
	}

	//-------------------------------

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setBoth(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	public boolean isOnBreak() {
		return onBreak;
	}

	public void setOnBreak(boolean onBreak) {
		this.onBreak = onBreak;
	}

}
