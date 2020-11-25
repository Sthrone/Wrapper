package logic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.converter.IntegerStringConverter;

public class FileEngine
{
	private static ObservableList<Competitor> competitors;
	private static boolean listChanged;		// Ako se u listu unese novi takmicar, zelecu da u meniju azuriram stanje
	private static Competitor lastAdded;	// Da bih u meniju mogao da mu setujem ime
	private static String path;		// Gde mi se nalaze tekstualni fajlovi
	private static File beginner;
	private static File expert;

	//------------------------------

	public static void createUserFolder()
	{
		String s = System.getProperty("os.name");

		if (s.charAt(0) == 'W')		// U pitanju je WindowsOS
			path = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Wrapper";
		else						// U pitanju je LinuxOS
			path = System.getProperty("user.home") + File.separator + "Wrapper";

		File dir = new File(path);	// Direktorijum

		if (!(dir.exists()))
		{
			dir.mkdirs();		// Napravljen folder

			// Pravim tekstualne fajlove za top10 (beginner i expert)
			beginner = new File(path + File.separator + "leaderboard_beginner.txt");

			try {
				beginner.createNewFile();
			} catch (IOException e) {
				System.out.println("FAILED AT MAKING: leaderboard_beginner.txt");
				e.printStackTrace();
			}

			expert = new File(path + File.separator + "leaderboard_expert.txt");

			try {
				expert.createNewFile();
			} catch (IOException e) {
				System.out.println("FAILED AT MAKING: leaderboard_expert.txt");
				e.printStackTrace();
			}
		}
		else
		{
			beginner = new File(path + File.separator + "leaderboard_beginner.txt");
			expert = new File(path + File.separator + "leaderboard_expert.txt");
		}
	}

	//------------------------------------------------

	public static void readLeaderboardFile()		// Cita se samo jednom, pri prvom pokretanju aplikacije i to glavni meni
	{
		competitors = FXCollections.observableList(new ArrayList<Competitor>());

		IntegerStringConverter converter = new IntegerStringConverter();

		Scanner scanner = new Scanner("");
		if (Engine.isDifficult())
			try {
				scanner = new Scanner(expert);
			} catch (FileNotFoundException e) {
				System.out.println("FAILED AT READING: leaderboard_expert.txt");
				e.printStackTrace();
			}
		else
			try {
				scanner = new Scanner(beginner);
			} catch (FileNotFoundException e) {
				System.out.println("FAILED AT READING: leaderboard_beginner.txt");
				e.printStackTrace();
			}

		while (scanner.hasNext())
		{
			competitors.add(new Competitor(scanner.next(), converter.fromString(scanner.next()), converter.fromString(scanner.next()), scanner.next()));
		}

		scanner.close();

		setListListener();		// Ovo koristi glavni meni kako bi znao kada da azurira svoju tabelu, tj. da ne bi pri svakom otvaranju azurirao
	}

	//------------------------------------------------

	private static void setListListener()	// Doslo je do promene
	{
		competitors.addListener((ListChangeListener.Change<? extends Competitor> c) -> {
			listChanged = true;		// Setuje se na false tek kada GUI obradi ovu informaciju
		});
	}

	//------------------------------------------------

	public static boolean isAmongTheLeaders(int compPoints, int compTime)		// Proveravam da li je novi rezultat medju najboljim
	{
		if (competitors.size() < 10) return true;	// Top 10, ima mesta u listi
		else if (!(competitors.get(9).hasBetterScoreThan(compPoints, compTime))) return true;	// Ako je puna lista i ako je bolji od poslednjeg, sigurno se moze naci na listi

		return false;
	}

	//------------------------------------------------

	public static void saveNewCompetitor(int compPoints, int compTime)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
		Date date = new Date();

		int i;
		for (i=0 ; i<competitors.size() ; i++)
			if (!(competitors.get(i).hasBetterScoreThan(compPoints, compTime))) break;	// Trazim mu mesto

		competitors.add(i, new Competitor(null, compPoints, compTime, dateFormat.format(date)));

		if (competitors.size() > 10) competitors.remove(10);	// Izbacujem poslednjeg, ako nema mesta

		lastAdded = competitors.get(i);
	}

	//------------------------------------------------

	public static void writeToLeaderboardFile()
	{
		BufferedWriter out;

		try
		{
			if (Engine.isDifficult())
				//out = new BufferedWriter(new FileWriter(FileEngine.class.getResource("/text/leaderboard_expert.txt").toExternalForm().toString().substring(5)));	// Ovaj URI ima file: na pocetku, njega sam izostavio
				out = new BufferedWriter(new FileWriter(expert));
			else
				//out = new BufferedWriter(new FileWriter(FileEngine.class.getResource("/text/leaderboard_beginner.txt").toExternalForm().toString().substring(5)));	// Ovaj URI ima file: na pocetku, njega sam izostavio
				out = new BufferedWriter(new FileWriter(beginner));

			for (int i=0 ; i<competitors.size() ; i++)
				out.write(competitors.get(i).toString()+"\n");

			out.close();
		}
		catch (IOException e)
		{
			System.out.println("FILE WRITING FAILED!");
			e.printStackTrace();
		}
	}

	//------------------------------------------------

	public static ObservableList<Competitor> getCompetitors() {
		return competitors;
	}

	public static boolean isListChanged() {
		return listChanged;
	}

	public static void setListChanged(boolean listChanged) {
		FileEngine.listChanged = listChanged;
	}

	public static Competitor getLastAdded() {
		return lastAdded;
	}

}
