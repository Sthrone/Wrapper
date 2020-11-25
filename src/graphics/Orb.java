package graphics;
import logic.OrbPoint;;

public abstract class Orb
{
	protected OrbPoint orb;
	protected SpriteSheet animation;

	//-------------------------------

	protected Orb(OrbPoint orb)
	{
		super();
		this.orb = orb;
	}

	//-------------------------------

	protected abstract void startAnimation();

	//-------------------------------

	public SpriteSheet getAnimation() {
		return animation;
	}

	public OrbPoint getOrb() {
		return orb;
	}

}
