package logic;
import static java.lang.Math.ceil;

import java.util.ArrayList;
import java.util.Random;


public class Engine
{
	private ArrayList<Dot2D> wrapper;
	private double maxLength;
	private double totalLength;
	private Random rand;

	private double[] polygonXs;
	private double[] polygonYs;
	private double maxX;
	private double minX;
	private double maxY;
	private double minY;
	private int numberOfCaughtOrbs;

	private ArrayList<OrbPoint> orbs;

	private int livesLeft;
	private int pointsAcquired;
	private int energy;
	private boolean releasePossible;	// Radi poput kljuca, ne dozvoljava trosenje energije dok se potpuno ne napuni i ne dozvoljava punjenje dok se trosi ili dok je puno

	private static boolean difficult = true;	// Da li se igra na expert modu

	//------------------------------------------------

	public Engine()
	{
		super();

		initEngine();
	}

	//------------------------------------------------

	public void initEngine()
	{
		this.maxLength = 1200;		// Najveca duzina
		this.totalLength = 0;		// Trenutna duzina

		rand = new Random();
		wrapper = new ArrayList<Dot2D>();
		orbs = new ArrayList<OrbPoint>();
		energy = 0;
		releasePossible = false;
		pointsAcquired = 0;
		livesLeft = 12;
		numberOfCaughtOrbs = 0;
	}

	//------------------------------------------------

	public boolean move(double x, double y, boolean onBreak)
	{
		Dot2D newPoint = new Dot2D(x, y, onBreak);
		int n = wrapper.size()-1;

		if (n <= 0)
		{
			wrapper.add(newPoint);
			return false;
		}

		if (wrapper.get(n).samePoint(newPoint)) return false;


		// Ako pripada istoj liniji, onda samo produzujem... Cuvam duge linije
		if (wrapper.get(n).getX() == newPoint.getX())
		{
			totalLength -= wrapper.get(n).distance(wrapper.get(n-1));
			wrapper.get(n).setY(newPoint.getY());
		}
		else if (wrapper.get(n).getY() == newPoint.getY())
		{
			totalLength -= wrapper.get(n).distance(wrapper.get(n-1));
			wrapper.get(n).setX(newPoint.getX());
		}
		else   // U suprotnom, samo je dodajem na kraj
		{
			wrapper.add(newPoint);
			n++;
		}

		totalLength += wrapper.get(n).distance(wrapper.get(n-1));

		while (totalLength > maxLength)		// Treba je skratiti, ali kako cuvam duge linije...
		{
			if ((wrapper.get(0).distance(wrapper.get(1)) > 20) && (!(wrapper.get(1).isOnBreak())))
			{
				wrapper.get(0).setBoth(ceil((wrapper.get(0).getX() + wrapper.get(1).getX()) / 2.0), ceil((wrapper.get(0).getY() + wrapper.get(1).getY()) / 2.0));	// Kratim liniju na pola
				totalLength -= wrapper.get(0).distance(wrapper.get(1));
			}
			else
			{
				totalLength -= wrapper.get(0).distance(wrapper.get(1));
				wrapper.remove(0);			// Brisem najstariju liniju koja nije duza od 20px
			}
		}

		// Energija se povecava kretanjem, tj. svakom novom linijom, ali samo ako se u tom trenutku ne trosi
		if (!(releasePossible))
		{
			energy++;
			if (energy == 1000) releasePossible = true;		// Moguce je iskoristiti energiju posto je puna
		}

		return true;
	}

	//------------------------------------------------

	public boolean detectCollision()
	{
		int n = wrapper.size()-1;
		Segment newSegment = new Segment(wrapper.get(n-1), wrapper.get(n));
		Segment otherSegment = new Segment();

		int i;
		for (i=0 ; i<n-2 ; i++)
		{
			Dot2D collisionPoint = newSegment.hasIntersection(otherSegment.setBothPoints(wrapper.get(i), wrapper.get(i+1)));
			if (collisionPoint != null)
			{
				formPolygon(i, collisionPoint);
				return true;
			}
		}

		return false;
	}

	//------------------------------------------------

	private void formPolygon(int from, Dot2D collisionPoint)
	{
		// Napravi nizove koordinata za poligon
		int numOfPoints = wrapper.size()-from+1;
		polygonXs = new double[numOfPoints];
		polygonYs = new double[numOfPoints];

		maxX = collisionPoint.getX();
		maxY = collisionPoint.getY();
		minX = collisionPoint.getX();
		minY = collisionPoint.getY();

		int i = from;
		for ( ; i<wrapper.size() ; i++)
		{
			polygonXs[i-from] = wrapper.get(i).getX();
			polygonYs[i-from] = wrapper.get(i).getY();

			// Odredjujem ekstremne vrednosti
			if (polygonXs[i-from] > maxX) maxX = polygonXs[i-from];
			if (polygonXs[i-from] < minX) minX = polygonXs[i-from];
			if (polygonYs[i-from] > maxY) maxY = polygonYs[i-from];
			if (polygonYs[i-from] < minY) minY = polygonYs[i-from];
		}

		polygonXs[i-from] = collisionPoint.getX();
		polygonYs[i-from] = collisionPoint.getY();
	}

	//------------------------------------------------

	public int generateOrb(double probInc)	// Vraca indeks nove sfere u listi sfera, ili -1 ako nije izgenerisao
	{
		double generatedNum = Math.random();
		int r = rand.nextInt(10);

		if (generatedNum < (0.01+probInc))	// Probability increase
		{
			if (r < 7)
				orbs.add(new OrbPoint(Math.random()*1100+30, Math.random()*700+30, 10, "blue"));	// Generisem plavi
			else
				orbs.add(new OrbPoint(Math.random()*1100+30, Math.random()*700+30, -50, "red"));	// Generisem crveni

			return orbs.size()-1;
		}

		return -1;
	}

	//------------------------------------------------

	/*
	 * 	Trazi sferu koja se u trenutku nastanka nalazila na maxIndex mestu u listi,
	 * 	gde ne mora uvek biti, jer se lista menja, ali ce svakako biti samo iza tog mesta,
	 * 	jer nikada ne dodajem preko reda u listu, nego uvek na kraj.
	 *
	 */
	/*
	public static OrbPoint findOrbAt(int maxIndex, Dot2D position)
	{
		while (((orbs.size()-1) < maxIndex) || (!(orbs.get(maxIndex).samePoint(position))))
			maxIndex--;

		return orbs.get(maxIndex);
	}
	*/
	//------------------------------------------------

	public void removeDeadOrbs(int from)
	{
		// Gledam koliko sam uhvatio, sabiram vrednosti uhvacenih (caught) i tu vrednost mnozim sa njihovim brojem
		int caughtPoints = 0;
		numberOfCaughtOrbs = 0;

		for (int i=from ; i < orbs.size() ; i++)
			if (orbs.get(i).getCurrentState() == -1 || orbs.get(i).getCurrentState() == 1)
			{
				if (orbs.get(i).getType() == "blue")
				{
					if (orbs.get(i).getCurrentState() == 1)
					{
						caughtPoints += orbs.get(i).getValue();	// Uhvatio sam ga, dobijam poene
						numberOfCaughtOrbs++;		// Brojim koliko sam uhatio, da bih posle izracunao poene
					}
					else if (difficult) livesLeft-=2;	// Istekao mu je timer, gubim 2 zivota AKO JE EXPERT MODE
				}
				else if (orbs.get(i).getType() == "red")
				{
					if (orbs.get(i).getCurrentState() == 1)		// Ako sam uhvatio crveni, gubim 4 zivota i 50 poena AKO JE EXPERT MODE, 2 ZA BEGINNER MODE
					{
						caughtPoints += orbs.get(i).getValue();
						numberOfCaughtOrbs++;

						if (difficult) livesLeft-=4;
						else livesLeft-=2;
					}
				}

				orbs.remove(i);
				i--;
			}

		if (livesLeft < 0) livesLeft = 0;
		if (numberOfCaughtOrbs > 7) numberOfCaughtOrbs = 7;		// Jer imam 7 slika :D
		pointsAcquired += numberOfCaughtOrbs*caughtPoints;

	}

	//------------------------------------------------

	public void designateCaughtOrbs()
	{
		Segment ray = new Segment();
		Segment polygonSide = new Segment();
		Dot2D currentOrb = new Dot2D(0,0);
		double startPadding = ((maxX - minX) / 100.0);	// Za dodavanje udaljenosti izmedju tacke iz koje povlacim liniju i prve strane poligona. Velicina je 1% sirine poligona

		for (int i=0 ; i<orbs.size() ; i++)
		{
			currentOrb.setBoth(orbs.get(i).getX(), orbs.get(i).getY());

			// Proveravam da li se data sfera ne nalazi u granicnom okviru
			if (currentOrb.getX() > maxX || currentOrb.getX() < minX || currentOrb.getY() > maxY || currentOrb.getY() < minY) continue;	 // sigurno se ne nalazi

			// Ray-casting algorithm
			// https://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon

			// Segment... (minX - padding, p.y) : (p.x, p.y)
			ray.setBothPoints(new Dot2D(minX - startPadding, currentOrb.getY()), currentOrb);

			int numOfIntersections = 0;

			for (int j=0 ; j<polygonXs.length-1 ; j++)
				if (((polygonXs[j] <= currentOrb.getX()) || (polygonXs[j+1] <= currentOrb.getX())) && (Math.max(polygonYs[j], polygonYs[j+1]) >= currentOrb.getY()) && (Math.min(polygonYs[j], polygonYs[j+1]) <= currentOrb.getY()))
				{
					if (ray.hasIntersection(polygonSide.setBothPoints(new Dot2D(polygonXs[j], polygonYs[j]), new Dot2D(polygonXs[j+1], polygonYs[j+1]))) != null)
					{		// Ako se segment nalazi levo od tacke na kojoj se nalazi sfera i ako nije sasvim iznad/ispod zraka i ako se sece sa zrakom...
							numOfIntersections++;
					}
				}

			if (numOfIntersections % 2 != 0)	// Ako je neparan broj preseka, onda se ta sfera nalazi u uhvacenoj oblasti
				orbs.get(i).setCurrentState(1);
		}
	}

	//------------------------------------------------

	public void depleteEnergy()
	{
		if (energy > 0) energy -= 10;
	}

	//------------------------------------------------

	public boolean depletionSuccessful()
	{
		if ((energy > 0) && (energy < 200)) return true;
		else return false;
	}

	//------------------------------------------------

	public void clearAllGoodOrbs()	// Postavlja sve postojece dobre sfere da su uhvacene kao dobar ishod koriscenja energije + 2 nova zivota
	{
		for (int i=0 ; i<orbs.size() ; i++)
			if (orbs.get(i).getType().equals("blue"))
				orbs.get(i).setCurrentState(1);

		if (livesLeft < 12) livesLeft += 2;
	}

	//------------------------------------------------

	public ArrayList<Dot2D> getWrapper() {
		return wrapper;
	}

	public double[] getPolygonXs() {
		return polygonXs;
	}

	public double[] getPolygonYs() {
		return polygonYs;
	}

	public ArrayList<OrbPoint> getOrbs() {
		return orbs;
	}

	public int getLivesLeft() {
		return livesLeft;
	}

	public void setLivesLeft(int livesLeft) {
		this.livesLeft = livesLeft;
	}

	public int getPointsAcquired() {
		return pointsAcquired;
	}

	public void setPointsAcquired(int pointsAcquired) {
		this.pointsAcquired = pointsAcquired;
	}

	public int getNumberOfCaughtOrbs() {
		return numberOfCaughtOrbs;
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMinY() {
		return minY;
	}

	public void setNumberOfCaughtOrbs(int numberOfCaughtOrbs) {
		this.numberOfCaughtOrbs = numberOfCaughtOrbs;
	}

	public double getMinX() {
		return minX;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxLength(double maxLength) {
		this.maxLength = maxLength;
	}

	public int getEnergy() {
		return energy;
	}

	public boolean isReleasePossible() {
		return releasePossible;
	}

	public void setReleasePossible(boolean releasePossible) {
		this.releasePossible = releasePossible;
	}

	public static void setDifficult(boolean difficult) {
		Engine.difficult = difficult;
	}

	public static boolean isDifficult() {
		return difficult;
	}

}
