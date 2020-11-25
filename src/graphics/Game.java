package graphics;
import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import logic.Competitor;
import logic.Engine;
import logic.FileEngine;
import logic.OrbPoint;

public class Game extends Pane
{
	private BorderPane root;
	private HBox menu;
	private Pane center;
	private Canvas canvas;
	private static GraphicsContext gc;
	private Bresenham line;

	private ImageView healthBar;
	private Label scoreLabel;
	private Label timeLabel;
	private ImageView energyBar;

	private Engine engine;
	private Timeline gameLoop;
	private long timeStart;

	private int numOfMoves = 0;
	private ArrayList<Orb> shownOrbs;

	private StackPane miniMenuOverlayPause;	// Meni sa RESUME
	private StackPane miniMenuOverlayEnd;	// Meni bez RESUME - zavrsila se igra
	private StackPane miniMenuOverlayLeaderboard;
	private GridPane table;
	private TextField nameField;
	//private Button submit;

	//------------------ CONSTRUCTOR --------------------------

	public Game()
	{
		this.setId("gameBackground");

		engine = new Engine();
		shownOrbs = new ArrayList<Orb>();

		prepareGraphics();

		//----------------------------- Mouse controls ----------------------------------

		canvas.setOnMouseEntered(e-> {
			engine.move(e.getX(), e.getY(), true);
		});

		canvas.setOnMouseMoved(e-> {
			registerMovement(e.getX(), e.getY());		// Uvek crtaj trag

			int amountOfEnergy = engine.getEnergy();		// Generisi energiju
			if (amountOfEnergy % 50 == 0)
				energyBar.setImage(new Image("images/EnergyUp/energy"+amountOfEnergy/50+".png"));
		});

		canvas.setOnMousePressed(e-> {
			registerMovement(e.getX(), e.getY());

			// Da li je moguce trosenje
			if (engine.isReleasePossible() && (e.isPrimaryButtonDown()))	// Ako je puna energija, i ako je pritisnut levi klik...
				engine.depleteEnergy();
		});

		canvas.setOnMouseDragged(e-> {
			registerMovement(e.getX(), e.getY());

			if (engine.isReleasePossible() && (e.isPrimaryButtonDown()))	// Trosi
			{
				engine.depleteEnergy();
				int amountOfEnergy = engine.getEnergy();
				if (amountOfEnergy % 50 == 0)
					energyBar.setImage(new Image("images/EnergyDown/energy"+amountOfEnergy/50+".png"));
			}
			else	// Inace, kretao sam se, pa sam generisao energiju, nebitno koji je klik pritisnut
			{
				int amountOfEnergy = engine.getEnergy();
				if (amountOfEnergy % 50 == 0)
					energyBar.setImage(new Image("images/EnergyUp/energy"+amountOfEnergy/50+".png"));
			}
		});

		canvas.setOnMouseReleased(e-> {
			registerMovement(e.getX(), e.getY());

			if (engine.isReleasePossible() && (e.getButton() == MouseButton.PRIMARY))	// Ako je poslednji klik misem bio levim dugmetom
			{
				if (engine.depletionSuccessful())
				{
					// Pozitivan efekat
					engine.clearAllGoodOrbs();

					// Blink screen
					root.getStyleClass().clear();
					root.getStyleClass().add("flash");

					Timeline waitaBit = new Timeline(
						new KeyFrame(
							Duration.millis(200),
							new EventHandler<ActionEvent>()
							{
								public void handle(ActionEvent e)
								{

								}
							}
						));
					waitaBit.play();

					waitaBit.setOnFinished(e1-> {	// Vraca stare vrednosti
						root.getStyleClass().clear();
						root.getStyleClass().add("gameBackground");
					});
				}
				else
				{
					// Negativan efekat
					engine.setMaxLength(600); 	// Skracujem duzinu
					line.setNewBrush("redPoint");

					// Postavljam tajmer da nakon 10sec vrati normalne vrednosti
					Timeline waitaBit = new Timeline(
						new KeyFrame(
							Duration.seconds(10),
							new EventHandler<ActionEvent>()
							{
								public void handle(ActionEvent e)
								{

								}
							}
						));
					waitaBit.play();

					waitaBit.setOnFinished(e1-> {	// Vraca stare vrednosti
						engine.setMaxLength(1200);
						line.setNewBrush(line.getChosenBrush());
					});
				}

				engine.setReleasePossible(false);	// Moguce je ponovno punjenje
			}
		});

		//----------------------------- Game loop ----------------------------------

		gameLoop = new Timeline();
		gameLoop.setCycleCount(Timeline.INDEFINITE);

		KeyFrame kf1 = new KeyFrame(
				Duration.seconds(0.017),		// 60 FPS
				new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent e)
					{
						if (shownOrbs.size() > 0)
							checkOrbLife();			// Proveravam da li neke sfere treba obrisati... Ako su prikazane

						int timeInSeconds = (int)(Math.floor((System.currentTimeMillis() - timeStart) / 1000.0));	// Azuriram labelu da prikazuje samo sekunde
						timeLabel.setText(timeInSeconds+"s");

						int orbIndex = engine.generateOrb((double)timeInSeconds/2500.0);	// Mozda generisem jednu novu sferu.
						if (orbIndex != -1)		// Sa vremenom se povecava verovatnoca da se generise
						{
							OrbPoint orb = engine.getOrbs().get(orbIndex);

							if (orb.getType().equals("blue"))
								shownOrbs.add(new BlueOrb(orb));	// Saljem mu element liste da bi mogao da mu promeni vrednost u pravom trenutku
							else if (orb.getType().equals("red"))
								shownOrbs.add(new RedOrb(orb));
						}
					}
				});

		gameLoop.getKeyFrames().add(kf1);

		getChildren().add(root);	// Dodajem ceo BorderPane u prozor
	}

	//--------------------- WRAPPER DISPLAY ------------------------------

	private void registerMovement(double x, double y)	// Registruje novu tacku na kojoj se kursor nalazi i pravi liniju do nje - Controller
	{
		boolean reDraw = engine.move(x, y, false);
		if (reDraw)
		{
			gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

			int n = engine.getWrapper().size();
			for (int i=0 ; i<n-1 ; i++)
				if (!(engine.getWrapper().get(i+1).isOnBreak()))		//  Iscrtavanje wrapper-a
					line.bresenhamLine(engine.getWrapper().get(i).getX(), engine.getWrapper().get(i).getY(), engine.getWrapper().get(i+1).getX(), engine.getWrapper().get(i+1).getY());

			// Da li je doslo do kolizije?
			boolean collisionHappened = engine.detectCollision();
			if (collisionHappened)
			{
				numOfMoves = 1;

				gc.fillPolygon(engine.getPolygonXs(), engine.getPolygonYs(), engine.getPolygonXs().length);
				gc.strokePolygon(engine.getPolygonXs(), engine.getPolygonYs(), engine.getPolygonXs().length);

				engine.designateCaughtOrbs();	// Oznaci sfere koje su se nalazile u obuhvacenom prostoru
			}
			else if ((numOfMoves > 0) && (numOfMoves < 10))		// Sluzi da drzi obuhvaceni region obojenim nekoliko koraka
			{
				numOfMoves++;
				gc.fillPolygon(engine.getPolygonXs(), engine.getPolygonYs(), engine.getPolygonXs().length);
				gc.strokePolygon(engine.getPolygonXs(), engine.getPolygonYs(), engine.getPolygonXs().length);

				if (engine.getNumberOfCaughtOrbs() >= 2)	// x2, x3 slike...
					gc.drawImage(new Image("images/Xs/x"+engine.getNumberOfCaughtOrbs()+".png"), engine.getMaxX(), engine.getMinY());
			}
			else
			{
				if (engine.getNumberOfCaughtOrbs() >= 2)	// x2, x3 slike...
					gc.drawImage(new Image("images/Xs/x"+engine.getNumberOfCaughtOrbs()+".png"), engine.getMaxX(), engine.getMinY());

				numOfMoves = 0;
				engine.setNumberOfCaughtOrbs(0);
			}
		}
	}

	//--------------------- MENU AND ORB DISPLAY ------------------------------

	private void checkOrbLife()		// Za GameLoop
	{
		ArrayList<OrbPoint> orbs = engine.getOrbs();
		int clearFrom = -1;

		for (int i=0 ; i<orbs.size() ; i++)
		{
			if ((orbs.get(i).getCurrentState() == -1) || (orbs.get(i).getCurrentState() == 1))		// 1  ili -1 znaci da su zavrsili sa radom
			{
				int maxIndex = i;	// Kako sam oba objekta za isti entitet napravio zajedno, mora da vazi
				while (((shownOrbs.size()-1) < maxIndex) || (!(shownOrbs.get(maxIndex).getOrb().samePoint(orbs.get(i)))))
					maxIndex--;

				shownOrbs.get(maxIndex).getAnimation().stop();
				shownOrbs.remove(maxIndex);

				if (clearFrom == -1) clearFrom = i;		// Na i-tom mestu se nasla prva zavrsena sfera
			}
		}

		if (clearFrom != -1)
		{
			engine.removeDeadOrbs(clearFrom);

			// Azuriram poene
			int numOfPoints = engine.getPointsAcquired();

			if (numOfPoints > 0)	// zelena
			{
				if (!(scoreLabel.getStyleClass().toString().equals("green")))	// Ako vec nije zelena
				{
					scoreLabel.getStyleClass().clear();
					scoreLabel.getStyleClass().add("green");
				}
			}
			else if (numOfPoints < 0)	// Crvena
			{
				if (!(scoreLabel.getStyleClass().toString().equals("red")))
				{
					scoreLabel.getStyleClass().clear();
					scoreLabel.getStyleClass().add("red");
				}
			}
			else	// Plava za nulu
			{
				if (!(scoreLabel.getStyleClass().toString().equals("zero")))
				{
					scoreLabel.getStyleClass().clear();
					scoreLabel.getStyleClass().add("zero");
				}
			}

			scoreLabel.setText(numOfPoints+"");

			// Azuriram zivote
			int numOfLives = engine.getLivesLeft();
			healthBar.setImage(new Image("images/Health/health"+numOfLives+".png"));

			if (numOfLives == 0)
				bringGameEndOverlay();
		}

	}

	//------------------- GAME OVER  -------------------------

	private void bringGameEndOverlay()
	{
		// Zaustavi tajmere
		gameLoop.stop();
		for (int i=0 ; i<shownOrbs.size() ; i++)
			shownOrbs.get(i).getAnimation().stop();

		int points = engine.getPointsAcquired();
		// Citam iz labele vreme koje je proteklo
		IntegerStringConverter converter = new IntegerStringConverter();
		int time = converter.fromString(timeLabel.getText().replaceAll("[^0-9]", ""));	// Odstranjujem sve sto nije broj(s) i kastujem na int - regex

		if (FileEngine.isAmongTheLeaders(points, time))		// Treba podici leaderboard da se korisnik upise ako mu je dovoljno dobar rezultat
		{
			FileEngine.saveNewCompetitor(points, time);
			updateTable();	// Popunjavam tabelu

			// Kako ovaj overlay sadrzi samo jedan VBox, uzimam ga kao dete, pa zatim uzimam njegovu decu i stavljam svoju azuriranu tabelu medju njima
			((VBox)miniMenuOverlayLeaderboard.getChildren().get(0)).getChildren().add(1, table);
			this.getChildren().add(miniMenuOverlayLeaderboard);	// Podizem tabelu

			nameField.requestFocus();
		}
		else
		{
			createMiniMenuOverlayEnd();
			this.getChildren().add(miniMenuOverlayEnd);  // Podici mini meni bez resume
		}
	}

	//------------------- NEW GAME INIT -------------------------

	public void initializeNewGame()
	{
		engine.initEngine();

		// Inicijalizovati sve vrednosti labela (score, timer) i  slika (health, energy)
		scoreLabel.getStyleClass().clear();
		scoreLabel.getStyleClass().add("zero");
		scoreLabel.setText("0");
		timeLabel.setText("0s");
		healthBar.setImage(new Image("images/Health/health12.png"));
		energyBar.setImage(new Image("images/EnergyUp/energy0.png"));
		line.setNewBrush(line.getChosenBrush());

		// Iskljuci tajmere za sve sfere i izbrisi ih
		for (int i=0 ; i<shownOrbs.size() ; i++)
			shownOrbs.get(i).getAnimation().stop();

		shownOrbs = new ArrayList<Orb>();
		numOfMoves = 0;

		// Ocisti canvas
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		// Novo pocetno vreme
		timeStart = System.currentTimeMillis();
	}

	//------------------- DISPLAY INIT --------------------------

	private void prepareGraphics()
	{
		root = new BorderPane();

		menu = new HBox();
		createMenu();

		center = new Pane();
		canvas = new Canvas(Main.windowWidth, Main.windowHeight-35);
		center.getChildren().add(canvas);

		root.setTop(menu);
		root.setCenter(center);

		gc = canvas.getGraphicsContext2D();
		line = new Bresenham(/*2.0, Color.rgb(194, 237, 230)*/);
	}

	private void createMenu()
	{
		Label healthText = new Label("HEALTH: ");
		healthText.getStyleClass().add("text");
		healthBar = new ImageView(new Image("images/Health/health12.png"));

		Label scoreText = new Label("SCORE: ");
		scoreText.getStyleClass().add("text");
		scoreLabel = new Label("0");
		scoreLabel.getStyleClass().add("zero");
		scoreLabel.setMinWidth(40);

		Label timeText = new Label("TIME: ");
		timeText.getStyleClass().add("text");
		timeLabel = new Label("0s");
		timeLabel.getStyleClass().add("zero");
		timeLabel.setMinWidth(40);

		Label energyText = new Label("ENERGY: ");
		energyText.getStyleClass().add("text");
		energyBar = new ImageView(new Image("images/EnergyUp/energy0.png"));

		createMiniMenuOverlayPause();	// Kada se klikne ne home dugme u toku igre
		createMiniMenuOverlayEnd();		// Kada je zavrsena igra i nije probijen nijedan rekord ili posle toga
		createMiniMenuOverlayLeaderboard(); 	// Prostor oko tabele

		Button miniMenuButton = new Button();
		miniMenuButton.setId("miniMenuButton");
		miniMenuButton.setOnAction(e-> {
			bringMiniMenuOverlayPaused();			// Zaustavlja sve tajmere i podize mini meni
		});

		HBox.setMargin(healthText, new Insets(0,0,0,20));
		HBox.setMargin(healthBar, new Insets(0,10,0,0));
		HBox.setMargin(scoreLabel, new Insets(0,10,0,0));
		HBox.setMargin(timeLabel, new Insets(0,10,0,0));
		HBox.setMargin(energyBar, new Insets(0,10,0,0));
		HBox.setMargin(miniMenuButton, new Insets(0,0,0,340));

		menu.setId("menu");
		menu.setPadding(new Insets(10, 0, 10, 0));
		menu.setAlignment(Pos.CENTER_LEFT);

		menu.getChildren().addAll(healthText, healthBar, scoreText, scoreLabel, timeText, timeLabel, energyText, energyBar, miniMenuButton);
	}

	//----------------------------------------

	private void createMiniMenuOverlayPause()
	{
		miniMenuOverlayPause = new StackPane();
		miniMenuOverlayPause.getStyleClass().add("transparentBackgroundCenter");
		miniMenuOverlayPause.setAlignment(Pos.CENTER);

		VBox optionPane = new VBox(15);
		optionPane.getStyleClass().add("content-black");
		optionPane.setAlignment(Pos.CENTER);

		Label message = new Label("PAUSED");
		message.getStyleClass().add("miniMenuLabel");
		VBox.setMargin(message, new Insets(0, 0, 20, 0));

		Button resume = new Button("RESUME");
		resume.setOnAction(e-> {
			// Skida mini meni
			center.getChildren().remove(miniMenuOverlayPause);

			// Ponovo pokrenuti sve tajmere
			gameLoop.play();
			for (int i=0 ; i<shownOrbs.size() ; i++)
				shownOrbs.get(i).getAnimation().play();
		});
		resume.getStyleClass().add("miniMenuButtonOption");

		Button newGame = new Button("NEW GAME");
		newGame.setOnAction(e-> {
			gameLoop.stop();		// Bio je samo pauziran, ovako ga resetujem
			initializeNewGame();

			center.getChildren().remove(miniMenuOverlayPause);

			gameLoop.play();
		});
		newGame.getStyleClass().add("miniMenuButtonOption");

		Button toMenu = new Button("MAIN MENU");
		toMenu.setOnAction(e-> {
			center.getChildren().remove(miniMenuOverlayPause);
			Main.setToggleScenes(0);	// Property posmatra ovu vrednost i menja scene u zavisnosti od postavljene vrednosti
		});
		toMenu.getStyleClass().add("miniMenuButtonOption");

		Button exit = new Button("EXIT GAME");
		exit.setOnAction(e-> {
			System.exit(0);
		});
		exit.getStyleClass().add("miniMenuButtonOption");

		optionPane.getChildren().addAll(message, resume, newGame, toMenu, exit);
		miniMenuOverlayPause.getChildren().add(optionPane);
	}

	//----------------------------------------

	public void bringMiniMenuOverlayPaused()		// Da bi sa ESC moglo da se pristupi mini meniju preko scene
	{
		// Treba pauzirati sve tajmere i podignuti overlay za meni
		gameLoop.pause();
		for (int i=0 ; i<shownOrbs.size() ; i++)
			shownOrbs.get(i).getAnimation().pause();

		center.getChildren().add(miniMenuOverlayPause);
	}

	public void dropMiniMenuOverlayPaused()			// kao RESUME, ali sa ESC
	{
		// Skida mini meni
		center.getChildren().remove(miniMenuOverlayPause);

		// Ponovo pokrenuti sve tajmere
		gameLoop.play();
		for (int i=0 ; i<shownOrbs.size() ; i++)
			shownOrbs.get(i).getAnimation().play();
	}

	//----------------------------------------

	private void createMiniMenuOverlayEnd()
	{
		miniMenuOverlayEnd = new StackPane();
		miniMenuOverlayEnd.getStyleClass().add("transparentBackground");
		miniMenuOverlayEnd.setAlignment(Pos.CENTER);

		VBox optionPane = new VBox(15);
		optionPane.getStyleClass().add("content-black");
		optionPane.setAlignment(Pos.CENTER);

		Label message = new Label("GAME OVER");
		message.getStyleClass().add("miniMenuLabel");
		VBox.setMargin(message, new Insets(0, 0, 20, 0));

		Button newGame = new Button("NEW GAME");
		newGame.setOnAction(e-> {
			gameLoop.stop();		// Bio je samo pauziran, ovako ga resetujem
			initializeNewGame();

			this.getChildren().remove(miniMenuOverlayEnd);

			gameLoop.play();
		});
		newGame.getStyleClass().add("miniMenuButtonOption");

		Button toMenu = new Button("MAIN MENU");
		toMenu.setOnAction(e-> {
			this.getChildren().remove(miniMenuOverlayEnd);
			Main.setToggleScenes(0);	// Property posmatra ovu vrednost i menja scene u zavisnosti od postavljene vrednosti
		});
		toMenu.getStyleClass().add("miniMenuButtonOption");

		Button exit = new Button("EXIT GAME");
		exit.setOnAction(e-> {
			System.exit(0);
		});
		exit.getStyleClass().add("miniMenuButtonOption");

		optionPane.getChildren().addAll(message, newGame, toMenu, exit);
		miniMenuOverlayEnd.getChildren().add(optionPane);
	}

	//----------------------------------------

	private void createMiniMenuOverlayLeaderboard()
	{
		miniMenuOverlayLeaderboard = new StackPane();
		miniMenuOverlayLeaderboard.getStyleClass().add("transparentBackground");
		miniMenuOverlayLeaderboard.setAlignment(Pos.CENTER);

		VBox tablePane = new VBox(30);
		tablePane.getStyleClass().add("biggerContentBlack");

		Label message = new Label("LEADERBOARD");
		message.getStyleClass().add("miniMenuLabel");

		Button submit = new Button("SUBMIT");
		submit.setOnAction(e-> {
			String fieldText = nameField.getText();
			if (fieldText.length() == 0) fieldText = "Unknown";		// Potpisuje ako je ostavljeno prazno
			else if (fieldText.length() > 10) fieldText = fieldText.substring(0, 10);	// Odseca string ako je uneseno vise od 10 karaktera

			fieldText = fieldText.replace(" ", "_");

			FileEngine.getLastAdded().setName(fieldText);		// Cuvam ime takmicara iz polja

			FileEngine.writeToLeaderboardFile();	// Nakon svakog rekorda, pisem celu tabelu u fajl

			((VBox)miniMenuOverlayLeaderboard.getChildren().get(0)).getChildren().remove(table);	// Izbacujem tabelu, jer bi se pri sledecoj igri unela jos jedna u isti prostor
			this.getChildren().remove(miniMenuOverlayLeaderboard);
			this.getChildren().add(miniMenuOverlayEnd);
		});
		submit.getStyleClass().add("miniMenuButtonOption");

		nameField = new TextField();
		nameField.setId("field");
		nameField.addEventHandler(KeyEvent.KEY_PRESSED, e-> {		// Pritiskom na ENTER se okida dugme
			if (e.getCode() == KeyCode.ENTER)
				submit.fire();
		});

		tablePane.getChildren().addAll(message, submit);
		miniMenuOverlayLeaderboard.getChildren().add(tablePane);
	}

	//----------------------------------------

	private void updateTable()
	{
		table = new GridPane();
		table.setHgap(20);
		table.setVgap(5);
		table.setPadding(new Insets(15));
		table.setAlignment(Pos.CENTER);
		table.setBorder(new Border(new BorderStroke(Color.web("#15181c"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 7, 0, 7))));

		// Postavljam headere
		table.add(new Label("Rank"), 0, 0);
		table.add(new Label("Name"), 1, 0);
		table.add(new Label("Points"), 2, 0);
		table.add(new Label("Time"), 3, 0);

		table.getChildren().forEach(l-> l.getStyleClass().add("leaderMainHeader"));

		ObservableList<Competitor> competitors = FileEngine.getCompetitors();
		nameField.clear();

		int i;
		for (i=0 ; i<competitors.size() ; i++)
		{
			table.add(new Label((i+1)+"."), 0, i+1);

			if (competitors.get(i).getName() == null)
				table.add(nameField, 1, i+1);
			else
				table.add(new Label(competitors.get(i).getName()), 1, i+1);

			table.add(new Label(competitors.get(i).getPoints()+""), 2, i+1);
			table.add(new Label(competitors.get(i).getTime()+"s"), 3, i+1);
		}

		table.getChildren().forEach(l-> l.getStyleClass().add("tableLable"));
	}

	//----------------------------------------

	public static GraphicsContext getGc() {
		return gc;
	}

	public Timeline getGameLoop() {
		return gameLoop;
	}

	public Bresenham getLine() {
		return line;
	}

	public int numOfLivesLeft()		// Da bih u Main-u mogao da iskljucim mini meni na ESC
	{
		return engine.getLivesLeft();
	}

}
