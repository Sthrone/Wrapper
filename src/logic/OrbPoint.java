package logic;

public class OrbPoint extends Dot2D
{
	private int value;
	private String type;		//	blue, red...
	private int currentState;	/*  (0) - ziv
								 *  (-1) - istekao timer
								 *  (1) - uhvatio sam ga
								 */
	//-------------------------------------------

	public OrbPoint(double x, double y, int value, String type)
	{
		super(x, y);
		this.value = value;
		this.type = type;
		this.currentState = 0;
	}

	//-------------------------------------------

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}

}
