package graphics;
import javafx.scene.image.Image;
import javafx.util.Duration;
import logic.Dot2D;
import logic.OrbPoint;

public class RedOrb extends Orb
{
	//-----------------------------------------------

	public RedOrb(OrbPoint orb)
	{
		super(orb);

		startAnimation();
	}

	//-----------------------------------------------

	@Override
	protected void startAnimation()
	{
		Image[] imagesRed = new Image[4];
		for (int i=0 ; i<4 ; i++)
			imagesRed[i] = new Image("images/Orbs/red"+i+".png");

		animation = new SpriteSheet(new Dot2D(orb.getX(), orb.getY()), imagesRed, Duration.seconds(1), 10);
        animation.play();

        animation.setOnFinished(e-> {
        	Game.getGc().clearRect(orb.getX()-20, orb.getY()-20, 40, 40);
        	orb.setCurrentState(-1);
    	});
	}

}
