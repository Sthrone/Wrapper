package graphics;
import javafx.scene.image.Image;
import javafx.util.Duration;
import logic.Dot2D;
import logic.OrbPoint;

public class BlueOrb extends Orb
{
	//-----------------------------------------------

	public BlueOrb(OrbPoint orb)
	{
		super(orb);

		startAnimation();
	}

	//-----------------------------------------------

	@Override
	protected void startAnimation()
	{
		Image[] imageBlue = new Image[1];
		imageBlue[0] = new Image("images/Orbs/blue1.png");

		animation = new SpriteSheet(new Dot2D(orb.getX(), orb.getY()), imageBlue, Duration.seconds(1), 5);	// Trajanje jednog ciklusa i koliko ciklusa
        animation.play();

        animation.setOnFinished(e-> {

        	Image[] imagesYellow = new Image[6];
    		for(int i=0 ; i<6 ; i++)
    			imagesYellow[i] = new Image("images/Orbs/yellow"+i+".png");

    		animation = new SpriteSheet(new Dot2D(orb.getX(), orb.getY()), imagesYellow, Duration.seconds(1), 5);
    		animation.play();

    		orb.setValue(30);	// Promena vrednosti orba

    		animation.setOnFinished(e1-> {
    			Game.getGc().clearRect(orb.getX()-20, orb.getY()-20, 40, 40);
    			orb.setCurrentState(-1);
    		});
        });
	}

}
