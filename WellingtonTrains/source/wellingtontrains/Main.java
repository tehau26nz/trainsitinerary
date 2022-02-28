package wellingtontrains;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import ecs100.UI;

public class Main extends JFrame {

	Image img;
	private HashMap<String, TrainLine> trainLines = new HashMap<>();
	private HashMap<String, Station> listStations = new HashMap<>();

	public void loadImage() throws IOException {
		String path = "lib/sytem-map.png";
		File file = new File(path);
		try {
			img = ImageIO.read(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void pain(Graphics g) {
		super.paint(g);
		g.drawImage(img, 0, 0, this);
	}

	public void loadStations() {
		try {
			Scanner scan = new Scanner(new File("stations.data"));
			while (scan.hasNext()) {
				String name = scan.next();
				int zone = scan.nextInt();
				double distance = scan.nextDouble();
				Station station = new Station(name, zone, distance);
				listStations.put(name, station);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void loadTrainLines() throws Exception {
		try {
			Scanner scan = new Scanner(new File("train-lines.data"));
			while (scan.hasNext()) {
				String name = scan.next();
				TrainLine trainLine = new TrainLine(name);
				addStationsToTrainLine(trainLine);
				trainLines.put(name, trainLine);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void loadAllData() throws Exception {
		loadStations();
		loadTrainLines();
	}

	public void addStationsToTrainLine(TrainLine tl) throws Exception {
		try {
			String stationFile = tl.getName() + "-stations.data";
			Scanner scan = new Scanner(new File(stationFile));
			while (scan.hasNextLine()) {
				String name = scan.nextLine();
				Station st = listStations.get(name);
				if (st == null) {
					throwInvalidStationError(name);
				}
				tl.addStation(st);
				st.addTrainLine(tl);
				addTrainService(tl);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addTrainService(TrainLine tl) {
		try {
			String fileName = tl.getName() + "-services.data";
			Scanner scan = new Scanner(new File(fileName));
			while (scan.hasNextLine()) {
				Scanner scan1 = new Scanner(scan.nextLine());
				TrainService ts = new TrainService(tl);
				boolean isFirstStop = true;
				while (scan1.hasNextInt()) {
					ts.addTime(scan1.nextInt(), isFirstStop);
					isFirstStop = false;
				}
				tl.addTrainService(ts);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void listStations() throws Exception {
		loadAllData();
		UI.clearText();
		UI.println("======= Here is a list of all the stations =======");
		for (Station s : listStations.values()) {
			UI.println(s.toString());
		}
	}

	public void listTrainLines() throws Exception {
		loadAllData();
		UI.clearText();
		UI.println("======= Here is a list of all the train lines =======");
		for (TrainLine tl : trainLines.values()) {
			UI.println(tl.toString());
		}
	}

	public void listTrainLinesByStationInput() throws Exception {
		loadAllData();
		UI.clearText();
		String station = UI.askString("====> Please enter a station name: ");
		Station st = listStations.get(station);
		if (st == null) {
			throwInvalidStationError(station);
		}

		for (TrainLine tl : st.getTrainLines()) {
			UI.println("Line " + tl.toString() + " connects with this station.");
		}
	}

	public void listStationsByTrainLineInput() throws Exception {
		loadAllData();
		UI.clearText();
		String trainLine = UI.askString("====> Please enter a train line: ");
		TrainLine tl = trainLines.get(trainLine);

		if (tl == null) {
			throwInvalidTrainLine(trainLine);
		}

		for (Station st : tl.getStations()) {
			UI.println("Station " + st.toString() + " is on this train line.");
		}
	}

	public void findNextTrainServices() throws Exception {
		loadAllData();
		UI.clearText();
		Integer input = UI.askInt("====>Please enter desired time: ");

		if (input < 0 || input % 100 > 59 || input > 2359) {
			throwInvalidEntryError(input);
		}

		HashMap<TrainService, Boolean> nextServices = findNextTrainServicesByTrainLines(trainLines.values(), input);

		for (Map.Entry<TrainService, Boolean> entry : nextServices.entrySet()) {
			TrainService service = entry.getKey();
			boolean isOnSameDay = entry.getValue();

			if (isOnSameDay) {
				UI.println("The train line " + service.getTrainLine().getName() + " starts at " + service.getStart());
			} else {
				UI.println("The train line " + service.getTrainLine().getName() + " starts at " + service.getStart()
						+ " the next day");
			}
		}
	}

	public void findNextTrainServiceByStartToEndStation() throws Exception {
		loadAllData();
		UI.clearText();
		String start = UI.askString("====>Please enter a departure station: ");
		String end = UI.askString("===>Please enter an arrival station: ");
		Integer inputTime = UI.askInt("==>Please enter desired time: ");

		if (inputTime < 0 || inputTime % 100 > 59 || inputTime > 2359) {
			throwInvalidEntryError(inputTime);
		}

		List<TrainLine> trainLines = findTrainLinesByStartToEndStation(start, end);

		if (trainLines.size() == 0) {
			UI.println("Sorry, there is no direct train line between '" + start + "' and '" + end + "'" + "\n"
					+ "You can try to use our bus services.");
			return;
		}

		HashMap<TrainService, Boolean> nextServices = findNextTrainServicesByTrainLines(trainLines, inputTime);

		Integer earliestServiceTime = 0;
		TrainService earliestService = null;

		for (Map.Entry<TrainService, Boolean> entry : nextServices.entrySet()) {
			TrainService service = entry.getKey();
			boolean isOnSameDay = entry.getValue();

			Integer serviceTime = service.getStart() + (isOnSameDay ? 0 : 2400);

			if (earliestService == null || serviceTime < earliestServiceTime) {
				earliestService = service;
				earliestServiceTime = serviceTime;
			}
		}

		if (earliestServiceTime < 2400) {
			UI.println("The train line " + earliestService.getTrainLine().getName() + " starts at "
					+ earliestService.getStart());
		} else {
			UI.println("The train line " + earliestService.getTrainLine().getName() + " starts at "
					+ earliestService.getStart() + " the next day");
		}
	}

	public HashMap<TrainService, Boolean> findNextTrainServicesByTrainLines(Collection<TrainLine> trainLines,
			Integer from) {
		HashMap<TrainService, Boolean> services = new HashMap<TrainService, Boolean>();

		for (TrainLine trainLine : trainLines) {
			List<TrainService> trainLineServices = trainLine.getTrainServices();

			TrainService nextService = null;
			TrainService firstServiceOfDay = trainLineServices.get(0);

			for (TrainService service : trainLineServices) {
				Integer start = service.getStart();

				if (start >= from) {
					nextService = service;
					break;
				}
			}

			if (nextService == null) {
				services.put(firstServiceOfDay, false);
			} else {
				services.put(nextService, true);
			}
		}

		return services;
	}

	public List<TrainLine> findTrainLinesByStartToEndStation(String start, String end) throws Exception {
		Station startStation = listStations.get(start);
		Station endStation = listStations.get(end);

		if (startStation == null) {
			throwInvalidStationError(start);
		}
		if (endStation == null) {
			throwInvalidStationError(end);
		}

		List<TrainLine> trainLines = new ArrayList<TrainLine>();

		for (TrainLine trainLine : startStation.getTrainLines()) {
			List<Station> stations = trainLine.getStations();

			Integer startIndex = stations.indexOf(startStation);
			Integer endIndex = stations.indexOf(endStation);

			if (startIndex < endIndex) {
				trainLines.add(trainLine);
			}
		}

		return trainLines;
	}

	public void listTrainLinesByStartToEndStationInput() throws Exception {
		loadAllData();
		UI.clearText();
		String start = UI.askString("Enter your departure station:");
		String end = UI.askString("Enter your arrival station:");

		List<TrainLine> trainLines = findTrainLinesByStartToEndStation(start, end);

		if (trainLines.size() == 0) {
			UI.println("Sorry, there is no direct train line between '" + start + "' and '" + end + "'." + "\n"
					+ "You can try to use our bus services.");
			return;
		}

		for (TrainLine trainLine : trainLines) {
			UI.println(trainLine);
		}
	}

	public void throwInvalidStationError(String name) throws Exception {
		UI.println("Invalid station " + name + ", must be part of Wellington Region");
	}

	public void throwInvalidTrainLine(String line) throws Exception {
		UI.println("Invalid train line" + line + ", must be part of Wellington Region");
	}

	public void throwInvalidEntryError(Integer input) throws Exception {
		UI.println("Invalid entry " + input + ", it must be an appropriate time value" + "\n"
				+ "** Example between [0,59], or [100,159], 60 will be 100 as 1:00. **");
	}

	public void throwErrorLoadingData(String station) throws Exception {
		UI.println("There is no data " + station + " loaded, please load all the data first");
	}

	public Main() throws IOException {
		UI.initialise();
		UI.addButton("List of all stations", () -> {
			try {
				listStations();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		UI.addButton("List of all the train lines", () -> {
			try {
				listTrainLines();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		UI.addButton("Details of a station", () -> {
			try {
				listTrainLinesByStationInput();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		UI.addButton("Details of a train line", () -> {
			try {
				listStationsByTrainLineInput();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		UI.addButton("Station itinerary", () -> {
			try {
				listTrainLinesByStartToEndStationInput();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		UI.addButton("Find train given time", () -> {
			try {
				findNextTrainServices();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		UI.addButton("Find train by itinerary", () -> {
			try {
				findNextTrainServiceByStartToEndStation();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		UI.addButton("Clear", UI::clearPanes);
		UI.addButton("Quit", UI::quit);
		UI.setWindowSize(1000, 500);
		UI.setDivider(0.5);
	}

	public static void main(String[] args) throws IOException {
		new Main();

	}

}
