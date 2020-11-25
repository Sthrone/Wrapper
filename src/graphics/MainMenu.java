package graphics;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import logic.Competitor;
import logic.Engine;
import logic.FileEngine;

public class MainMenu extends StackPane
{
    private	StackPane container;	// Samo jedan od VBox-ova se moze nalaziti u prozoru i taj je u kontejneru
    private	VBox mainMenu;
    private VBox difficultySubmenu;
    private VBox looksSubmenu;
    private VBox soundtrackSubmenu;
    private VBox leaderboardSubmenu;

    private MediaPlayer mediaPlayer;
    private String currentSoundtrack;
    private GridPane table;

    //------------------------------------------------------------

    public MainMenu()
    {
    	this.setId("mainMenu");		// Da bih postavio sliku u pozadini

        container = new StackPane();
        container.setMaxSize(400, 400);
        container.setAlignment(Pos.CENTER);
        container.setVisible(true);		// Kontrolisem njegovu vidljivost

        FileEngine.createUserFolder();
        FileEngine.readLeaderboardFile();		// Ucitavam top10 tabelu iz fajla i to radim jedino ovde I kada se promeni tezina

        // Kreiram meni i sve podmenije
        buildMainMenu();
        //buildDifficultySubmenu();
        //buildLookSubmenu();
        //buildSoundtrackSubmenu();
        //buildLeaderboardSubmenu();

        setNewSoundtrack("classicalPiano.mp3");		// Pustam muziku

        // Dodajem u prozor glavni meni
        container.getChildren().add(mainMenu);
        getChildren().add(container);
    }

    //------------------------------------------------------------

	private void buildMainMenu()
	{
		mainMenu = new VBox(12);

		Button newGame = new Button("NEW GAME");
		newGame.setOnAction(e-> {
			Main.setToggleScenes(1);
		});

		Button difficulty = new Button("DIFFICULTY");
		difficulty.setOnAction(e-> {
			buildDifficultySubmenu();
			translateElements(mainMenu, difficultySubmenu, 600);
		});

		Button looks = new Button("CHOOSE YOUR LOOK");
		looks.setOnAction(e-> {
			buildLookSubmenu();
			translateElements(mainMenu, looksSubmenu, 600);
		});

		Button soundtrack = new Button("SOUNDTRACK");
		soundtrack.setOnAction(e-> {
			buildSoundtrackSubmenu();
			translateElements(mainMenu, soundtrackSubmenu, 600);
		});

		Button leaderboard = new Button("LEADERBOARD");
		leaderboard.setOnAction(e-> {
			/*if (FileEngine.isListChanged())
			{
				buildLeaderboardSubmenu();	// Ako je doslo do promene tabele, azuriraj
				FileEngine.setListChanged(false);	// Ceka na sledecu promenu da bi azurirao
			}*/
			buildLeaderboardSubmenu();
			translateElements(mainMenu, leaderboardSubmenu, 600);
		});

		Button exit = new Button("EXIT");
		exit.setOnAction(e-> {
			System.exit(0);
		});

		mainMenu.getChildren().addAll(newGame, difficulty, looks, soundtrack, leaderboard, exit);
	}

	//------------------------------------------------------------

	private void translateElements(VBox remove, VBox insert, double distanceToMove)		// Metod koji se bavi animacijom pomeranja menija pri kliku
	{
		container.getChildren().add(insert);

		TranslateTransition removeOld = new TranslateTransition(Duration.seconds(0.5), remove);
        removeOld.setToX(remove.getTranslateX() - distanceToMove);

        TranslateTransition insertNew = new TranslateTransition(Duration.seconds(0.5), insert);
        insertNew.setFromX(remove.getTranslateX() + distanceToMove);
        insertNew.setToX(remove.getTranslateX());

        ParallelTransition pt = new ParallelTransition(removeOld, insertNew);
        pt.play();

        pt.setOnFinished(e-> {
            container.getChildren().remove(remove);
        });
	}

	//------------------------------------------------------------

	private void buildDifficultySubmenu()
	{
		difficultySubmenu = new VBox(12);

		RadioButton beginner = new RadioButton("BEGINNER");
		beginner.setOnAction(e-> {
			if (Engine.isDifficult())
			{
				Engine.setDifficult(false);
				FileEngine.readLeaderboardFile();
			}
		});
		beginner.getStyleClass().remove("radio-button");
		beginner.getStyleClass().add("toggle-button");
		//beginner.setSelected(true);

		RadioButton expert = new RadioButton("EXPERT");
		expert.setOnAction(e-> {
			if (!(Engine.isDifficult()))
			{
				Engine.setDifficult(true);
				FileEngine.readLeaderboardFile();
			}
		});
		expert.getStyleClass().remove("radio-button");
		expert.getStyleClass().add("toggle-button");

		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(beginner, expert);

		if (Engine.isDifficult()) expert.setSelected(true);
		else beginner.setSelected(true);

		Button back = new Button("BACK");
		back.setOnAction(e-> {
			translateElements(difficultySubmenu, mainMenu, -600);
		});
		VBox.setMargin(back, new Insets(15,0,0,0));

		difficultySubmenu.getChildren().addAll(beginner, expert, back);
	}

	//------------------------------------------------------------

	private void buildLookSubmenu()
	{
		looksSubmenu = new VBox(12);

		HBox row1 = new HBox();
		row1.setAlignment(Pos.CENTER);

		RadioButton blue = new RadioButton();
		blue.setOnAction(e-> {
			setNewWrapper("bluePoint");
		});
		blue.getStyleClass().remove("radio-button");
		blue.getStyleClass().add("blue-wrapper");
		//blue.setSelected(true);

		Label blueText = new Label("  Blue Wrapper");
		blueText.getStyleClass().add("menuLabel");
		row1.getChildren().addAll(blue, blueText);


		HBox row2 = new HBox();
		row2.setAlignment(Pos.CENTER);

		RadioButton yellow = new RadioButton();
		yellow.setOnAction(e-> {
			setNewWrapper("yellowPoint");
		});
		yellow.getStyleClass().remove("radio-button");
		yellow.getStyleClass().add("yellow-wrapper");

		Label yellowText = new Label("  Yellow Wrapper");
		yellowText.getStyleClass().add("menuLabel");
		row2.getChildren().addAll(yellow, yellowText);


		HBox row3 = new HBox();
		row3.setAlignment(Pos.CENTER);

		RadioButton green = new RadioButton();
		green.setOnAction(e-> {
			setNewWrapper("greenPoint");
		});
		green.getStyleClass().remove("radio-button");
		green.getStyleClass().add("green-wrapper");

		Label greenText = new Label("  Green Wrapper");
		greenText.getStyleClass().add("menuLabel");
		row3.getChildren().addAll(green, greenText);

		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(blue, yellow, green);

		// Koji je trenutno izabran
		String activeBrush = Main.getGame().getLine().getChosenBrush();
		if (activeBrush.equals("bluePoint")) blue.setSelected(true);
		else if (activeBrush.equals("yellowPoint")) yellow.setSelected(true);
		else if (activeBrush.equals("greenPoint")) green.setSelected(true);


		Button back = new Button("BACK");
		back.setOnAction(e-> {
			translateElements(looksSubmenu, mainMenu, -600);
		});
		VBox.setMargin(back, new Insets(20,0,0,0));

		looksSubmenu.getChildren().addAll(row1, row2, row3, back);
	}


	private void setNewWrapper(String pointName)
	{
		Main.getGame().getLine().setNewBrush(pointName);
	}

	//------------------------------------------------------------

	private void buildSoundtrackSubmenu()
	{
		soundtrackSubmenu = new VBox(6);

		RadioButton piano = new RadioButton("Classical piano");
		piano.setOnAction(e-> {
			setNewSoundtrack("classicalPiano.mp3");
		});
		piano.getStyleClass().remove("radio-button");
		piano.getStyleClass().add("toggle-button");
		//piano.setSelected(true);

		RadioButton french = new RadioButton("Accordion variations");
		french.setOnAction(e-> {
			setNewSoundtrack("frenchAccordion.mp3");
		});
		french.getStyleClass().remove("radio-button");
		french.getStyleClass().add("toggle-button");

		RadioButton serbian = new RadioButton("Serbian flute");
		serbian.setOnAction(e-> {
			setNewSoundtrack("serbianTraditional.mp3");
		});
		serbian.getStyleClass().remove("radio-button");
		serbian.getStyleClass().add("toggle-button");

		RadioButton jazz = new RadioButton("Jazz piano");
		jazz.setOnAction(e-> {
			setNewSoundtrack("jazzPiano.mp3");
		});
		jazz.getStyleClass().remove("radio-button");
		jazz.getStyleClass().add("toggle-button");

		RadioButton none = new RadioButton("Mute");
		none.setOnAction(e-> {
			mediaPlayer.stop();
			currentSoundtrack = "";
		});
		none.getStyleClass().remove("radio-button");
		none.getStyleClass().add("toggle-button");

		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(piano, french, serbian, jazz, none);

		if (currentSoundtrack.equals("classicalPiano.mp3")) piano.setSelected(true);
		else if (currentSoundtrack.equals("frenchAccordion.mp3")) french.setSelected(true);
		else if (currentSoundtrack.equals("serbianTraditional.mp3")) serbian.setSelected(true);
		else if (currentSoundtrack.equals("jazzPiano.mp3")) jazz.setSelected(true);
		else none.setSelected(true);

		Label volumeText = new Label("Adjust music volume: ");
		volumeText.getStyleClass().add("menuLabel");

		// Volume slider
		Slider volumeSlider = new Slider();
		volumeSlider.setPrefWidth(250);
		volumeSlider.setMaxWidth(300);
		volumeSlider.setMinWidth(250);
		volumeSlider.valueProperty().addListener(new InvalidationListener()
		{
		    public void invalidated(Observable ov)
		    {
		       if (volumeSlider.isValueChanging())
		       {
		           mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);	// Player ima glasnocu od 0 do 1
		       }
		    }
		});

		Button back = new Button("BACK");
		back.setOnAction(e-> {
			translateElements(soundtrackSubmenu, mainMenu, -600);
		});
		VBox.setMargin(back, new Insets(15,0,0,0));

		soundtrackSubmenu.getChildren().addAll(piano, french, serbian, jazz, none, volumeText, volumeSlider, back);
	}


	private void setNewSoundtrack(String fileName)
	{
		if (mediaPlayer != null) mediaPlayer.stop();
		Media music = new Media(MainMenu.class.getResource("/music/"+fileName).toString());
		mediaPlayer = new MediaPlayer(music);
		mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
		mediaPlayer.play();
		currentSoundtrack = fileName;
	}

    //------------------------------------------------------------

	private void buildLeaderboardSubmenu()		// Pravi skelet izgleda i upisuje ono sto se prvo nalazilo u fajlu
	{
		leaderboardSubmenu = new VBox(5);
		leaderboardSubmenu.setMinSize(600, 600);

		// Ucitavam trenutne vrednosti polja u tabelu (GridPane)
		table = new GridPane();
		table.setPadding(new Insets(30));
		table.setHgap(30);
		table.setVgap(5);
		table.setId("leaderMain");

		// Postavljam headere
		table.add(new Label("Rank"), 0, 0);
		table.add(new Label("Name"), 1, 0);
		table.add(new Label("Points"), 2, 0);
		table.add(new Label("Time"), 3, 0);
		table.add(new Label("Date"), 4, 0);

		table.getChildren().forEach(l-> l.getStyleClass().add("leaderMainHeader"));

		ObservableList<Competitor> competitors = FileEngine.getCompetitors();
		int i;
		for (i=0 ; i<competitors.size() ; i++)
		{
			table.add(new Label((i+1)+"."), 0, i+1);
			table.add(new Label(competitors.get(i).getName()), 1, i+1);
			table.add(new Label(competitors.get(i).getPoints()+""), 2, i+1);
			table.add(new Label(competitors.get(i).getTime()+"s"), 3, i+1);
			table.add(new Label(competitors.get(i).getDate().replace("_", " ")), 4, i+1);
		}

		if (i != 10)	// Ako nije puna lista, napravi prazna polja
		{
			while (i != 10)
			{
				table.add(new Label((i+1)+"."), 0, i+1);
				table.add(new Label(""), 1, i+1);
				table.add(new Label(""), 2, i+1);
				table.add(new Label(""), 3, i+1);
				table.add(new Label(""), 4, i+1);

				i++;
			}
		}

		table.getChildren().forEach(l-> l.getStyleClass().add("tableLable"));

		Button back = new Button("BACK");
		back.setOnAction(e-> {
			translateElements(leaderboardSubmenu, mainMenu, -600);
		});
		VBox.setMargin(back, new Insets(20,0,0,0));

		leaderboardSubmenu.getChildren().addAll(table, back);
	}

    //------------------------------------------------------------

	public StackPane getContainer() {
		return container;
	}

}
