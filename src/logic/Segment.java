package logic;

public class Segment
{
	private Dot2D start;
	private Dot2D end;
	private double slope;

	//-----------------------------------------

	public Segment() { }

	public Segment(Dot2D start, Dot2D end)
	{
		super();
		this.start = start;
		this.end = end;

		calculateSlope();
	}

	public String toString()
	{
		return start+":"+end;
	}

	//-----------------------------------------

	private void calculateSlope()
	{
		if (start.getX() != end.getX())  // Ako postoji nagib...
			slope = (start.getY()-end.getY())/(start.getX()-end.getX());
		else slope = Double.MAX_VALUE;
	}

	//-----------------------------------------

	public Dot2D hasIntersection(Segment segment)
	{
		if (this.getSlope() == segment.getSlope()) return null;  // Paralelne su, nema preseka

		// A*x + B*y + C = 0
		double d1, d2;

		double a1 = this.getEnd().getY() - this.getStart().getY();
		double b1 = this.getStart().getX() - this.getEnd().getX();
		double c1 = (this.getEnd().getX() * this.getStart().getY()) - ((this.getStart().getX() * this.getEnd().getY()));

		// Menjam tacke druge duzi u jednacini prave prve duzi
		d1 = (a1 * segment.getStart().getX()) + (b1 * segment.getStart().getY()) + c1;
		d2 = (a1 * segment.getEnd().getX()) + (b1 * segment.getEnd().getY()) + c1;

		// Rezultat jednacina je pozitivna ili negativna vrednost, zavisno od toga sa koje strane prave se proveravana tacka nalazi
		// Ako su d1 i d2 istog znaka, tada se tacke nalaze sa iste strane prave i tada nema preseka

		if ((d1>0 && d2>0) || (d1<0 && d2<0)) return null;

		// Sada znamo da druga duz sece pravu prve duzi, ali ne i da sece samu tu duz
		// Radimo isto za drugu duz

		double a2 = segment.getEnd().getY() - segment.getStart().getY();
		double b2 = segment.getStart().getX() - segment.getEnd().getX();
		double c2 = (segment.getEnd().getX() * segment.getStart().getY()) - ((segment.getStart().getX() * segment.getEnd().getY()));

		d1 = (a2 * this.getStart().getX()) + (b2 * this.getStart().getY()) + c2;
		d2 = (a2 * this.getEnd().getX()) + (b2 * this.getEnd().getY()) + c2;

		if ((d1>0 && d2>0) || (d1<0 && d2<0)) return null;

		// Sigurno se seku, vracam presecnu tacku
		double interX = (b1*c2 - b2*c1)/(a1*b2 - a2*b1);
		double interY = ((-1)*a1*interX - c1)/b1;

		return new Dot2D(interX, interY);
	}

	//-----------------------------------------

	public Segment setBothPoints(Dot2D start, Dot2D end)
	{
		this.start = start;
		this.end = end;
		calculateSlope();

		return this;
	}

	//-----------------------------------------

	public Dot2D getStart() {
		return start;
	}

	public Dot2D getEnd() {
		return end;
	}

	public double getSlope() {
		return slope;
	}

}
