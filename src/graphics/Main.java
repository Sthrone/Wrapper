package graphics;

import javafx.animation.Animation;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Main extends Application
{
	private static MainMenu menu;
	private static Game game;
	private Scene menuScene;
	private Scene gameScene;
	public static final int windowWidth = 1200;
	public static final int windowHeight = 800;

	private static SimpleIntegerProperty toggleScenes;
	/*
	 * 	0 - Meni
	 *  1 - Nova igra
	 *
	 */

	//--------------------------------------------------------

	public static void main(String[] args)
	{
		launch(args);
	}

	//--------------------------------------------------------

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		menu = new MainMenu();
		game = new Game();

		menuScene = new Scene(menu, windowWidth, windowHeight);
		gameScene = new Scene(game, windowWidth, windowHeight);

		menuScene.getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());	// Vezuje sa .CSS fajlom
		gameScene.getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());

		controlMenuDisplay();

		toggleScenes = new SimpleIntegerProperty(0);			// Simple - ne moram da implementiram sve getter-e, setter-e... Poput adaptera
		toggleScenes.addListener(new ChangeListener<Number>()
		{
		      @Override
		      public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal)
		      {
		    	  if (newVal.intValue() == 1)			// Postavi scenu za novu igru
		    	  {
		    		  game.initializeNewGame();
		    		  primaryStage.setScene(gameScene);
		    		  game.getGameLoop().play();
		    	  }
		    	  else if (newVal.intValue() == 0)		// Postavi scenu za meni
		    	  {
		    		  game.getGameLoop().stop();
		    		  primaryStage.setScene(menuScene);
		    	  }
		      }
		});

		primaryStage.setScene(menuScene);
		primaryStage.setTitle("Wrapper");
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	//--------------------------------------------------------

	public static void setToggleScenes(int newVal)
	{
		toggleScenes.set(newVal);
	}

	//--------------------------------------------------------

	private void controlMenuDisplay()
	{
		/*
        //	Klikom na odredjene tastere se pojavljuje ili sklanja meni u sceni
        menuScene.setOnKeyPressed(e-> {
            if ((e.getCode() == KeyCode.ENTER) || (e.getCode() == KeyCode.ESCAPE) || (e.getCode() == KeyCode.SPACE))
            {
            	StackPane container = menu.getContainer();
            	if (!container.isVisible())
            	{
            		 FadeTransition ft = new FadeTransition(Duration.seconds(0.5), container);
                     ft.setFromValue(0);
                     ft.setToValue(1);

                     container.setVisible(true);
                     ft.play();
            	}
            	else
            	{
            		FadeTransition ft = new FadeTransition(Duration.seconds(0.5), container);
                    ft.setFromValue(1);
                    ft.setToValue(0);

                    ft.setOnFinished(e1-> container.setVisible(false));
                    ft.play();
            	}
            }
        });
		*/
        // Klikom na ESC se otvara mini meni za vreme same igre
        gameScene.setOnKeyPressed(e-> {
        	if (e.getCode() == KeyCode.ESCAPE)
        	{
        		if (game.numOfLivesLeft() > 0)
        		{
        			if (game.getGameLoop().getStatus() == Animation.Status.PAUSED)
        				game.dropMiniMenuOverlayPaused();
        			else
        				game.bringMiniMenuOverlayPaused();
        		}
        	}
        });
	}

	//--------------------------------------------------------

	public static MainMenu getMenu() {
		return menu;
	}

	public static Game getGame() {
		return game;
	}

}
