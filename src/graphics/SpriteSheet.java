package graphics;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.image.Image;
import javafx.util.Duration;
import logic.Dot2D;

public class SpriteSheet extends Transition
{
	private Dot2D position;
	private final Image[] views;
	private int currentView;

	//-----------------------------------------

	public SpriteSheet(Dot2D position, Image[] images, Duration cycleDuration, int cycleCount)
	{
		super();
		this.views = images;
		this.position = position;
		this.currentView = 0;

		setCycleDuration(cycleDuration);
        setCycleCount(cycleCount);
        setInterpolator(Interpolator.EASE_IN);
	}

	//-----------------------------------------

	@Override
	protected void interpolate(double frac)
	{
		Game.getGc().drawImage(views[currentView/5], position.getX()-20, position.getY()-20, 40, 40);		// Iscrtava sliku na kanvasu
		currentView++;
		if (currentView == views.length * 5) currentView = 0;
	}

}
