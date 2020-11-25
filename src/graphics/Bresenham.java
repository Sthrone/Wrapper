package graphics;

import javafx.scene.image.Image;/*
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.SnapshotParameters;*/
import javafx.scene.paint.Color;

public class Bresenham
{
    private Image brush;
    private double brushWidthHalf;
    private double brushHeightHalf;
    private String chosenBrush;	// Sluzi da kada zbog greske obojim u crveno, znam koja je bila originalna boja

    //---------------------------------------------------

    public Bresenham(/*double brushSize, Color brushColor*/)
    {
    	super();

    	//brush = createBrush(brushSize, brushColor);
    	setNewBrush("bluePoint");
    	brushWidthHalf = brush.getWidth() / 2.0;
    	brushHeightHalf = brush.getHeight() / 2.0;

		Game.getGc().setLineWidth(6);
    }

    //---------------------------------------------------

	// https://de.wikipedia.org/wiki/Bresenham-Algorithmus
    public void bresenhamLine(double x0, double y0, double x1, double y1)	// Ovo je algoritam za crtanje duzi od tacke A do tacke B, nema veze sa logikom igre
    {
    	double dx =  Math.abs(x1-x0);
    	double dy = -Math.abs(y1-y0);
    	double err = dx+dy; /* error value e_xy */
    	double e2;
    	double sx = x0<x1 ? 1. : -1.;		// Vredosti koje dodaje na trenutne koordinate da bi stigao do krajnje
    	double sy = y0<y1 ? 1. : -1.;

    	while(true)
    	{
    		Game.getGc().drawImage(brush, x0 - brushWidthHalf, y0 - brushHeightHalf);	// Crta po pola kruga napred
    		if (x0==x1 && y0==y1) break;
    		e2 = 2.*err;
    	  	if (e2 > dy) { err += dy; x0 += sx; } 	/* e_xy+e_x > 0 */		// Odredjuje da li da se pomeri za piksel (koordinatu) ili ne - Bresenham
    	  	if (e2 < dx) { err += dx; y0 += sy; } 	/* e_xy+e_y < 0 */
    	}
    }

    //---------------------------------------------------

    public void setNewBrush(String imageName)
    {
    	brush = new Image("images/Points/"+imageName+".png");

    	if (imageName.equals("bluePoint"))
    	{
    		// Oba zavise od boje same cetkice
    		Game.getGc().setFill(Color.DARKSLATEGRAY);		// Boja poligona
    		Game.getGc().setStroke(Color.AQUAMARINE);		// Konture poligona
    		chosenBrush = "bluePoint";
    	}
    	else if (imageName.equals("redPoint"))
    	{
    		Game.getGc().setFill(Color.web("#ad780f"));
    		Game.getGc().setStroke(Color.web("#edab49"));
    	}
    	else if (imageName.equals("yellowPoint"))
    	{
    		Game.getGc().setFill(Color.web("#7a1f0b"));
    		Game.getGc().setStroke(Color.web("#e8b051"));
    		chosenBrush = "yellowPoint";
    	}
    	else if (imageName.equals("greenPoint"))
    	{
    		Game.getGc().setFill(Color.web("#4b3a4c"));
    		Game.getGc().setStroke(Color.web("#81a889"));
    		chosenBrush = "greenPoint";
    	}

    }

    //---------------------------------------------------

	public String getChosenBrush() {
		return chosenBrush;
	}

    //---------------------------------------------------
    /*
    private Image createImage(Circle node)
    {
        WritableImage wi;

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        int imageWidth = (int) node.getBoundsInLocal().getWidth();
        int imageHeight = (int) node.getBoundsInLocal().getHeight();

        wi = new WritableImage(imageWidth, imageHeight);
        node.snapshot(parameters, wi);

        return wi;
    }

    //---------------------------------------------------

    private Image createBrush(double radius, Color color)
    {
        // create gradient image with given color
        Circle brush = new Circle(radius);

        RadialGradient gradient1 = new RadialGradient(0, 0, 0, 0, radius, false, CycleMethod.NO_CYCLE, new Stop(0, color.deriveColor(1, 1, 1, 0.8)));
        brush.setFill(gradient1);

        // create image
        return createImage(brush);
    }
	*/

}
