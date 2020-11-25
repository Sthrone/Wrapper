package logic;

public class Competitor
{
	private String name;
	private int points;
	private int time;
	private String date;

	//----------------------------------------------

	public Competitor(String name, int points, int time, String date)
	{
		super();
		this.name = name;
		this.points = points;
		this.time = time;
		this.date = date;
	}

	public String toString()
	{
		return name+" "+points+" "+time+" "+date;
	}

	//----------------------------------------------

	public boolean hasBetterScoreThan(int compPoints, int compTime)
	{
		if (this.points > compPoints) return true;			// Poredim po poenima, pa ako imaju isto, uzimam onog sa kracim vremenom
		else if (this.points < compPoints) return false;

		if (this.time < compTime) return true;
		else if (this.time > compTime) return false;

		return false;	// Ako su potpuno isti, uzimam novijeg
	}

	//----------------------------------------------

	public String getName() {
		return name;
	}

	public int getPoints() {
		return points;
	}

	public int getTime() {
		return time;
	}

	public String getDate() {
		return date;
	}

	public void setName(String name) {
		this.name = name;
	}

}
