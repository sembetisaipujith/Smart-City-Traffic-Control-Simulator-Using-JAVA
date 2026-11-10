# Smart City Traffic Control System

**Java Swing desktop application — B.Tech project**

This submission contains the Java implementation only. It does not require Node.js, npm, React, a browser, a Gemini API key, or an internet connection.

## Requirements

- JDK 17 or newer (the project uses modern Java switch expressions).
- Windows: PowerShell or Command Prompt.
- macOS/Linux: a desktop environment for launching the Swing GUI.

Verify Java is installed:

```text
java -version
javac -version
```

Both commands should report version 17 or newer.

## Run on Windows

1. Extract the ZIP file.
2. Open the extracted `smart-city-traffic-control-system` folder.
3. Double-click `run.bat`, or run these commands in PowerShell:

```powershell
.\build.bat
.\run.bat
```

`run.bat` compiles the project automatically if the compiled classes are missing.

## Run on macOS/Linux

From the extracted project directory:

```bash
chmod +x build.sh run.sh test.sh
./build.sh
./run.sh
```

## Run the included smoke tests

Windows:

```powershell
.\test.bat
```

macOS/Linux:

```bash
./test.sh
```

The tests cover traffic-density boundaries, adaptive green-time limits, road counters/reset behavior, emergency FIFO ordering, report metadata, and headless rendering of the map panel. They are lightweight smoke tests, not a substitute for manually testing the complete interactive simulation.

## Main features

- Four-way city intersection rendered with Java Swing and `Graphics2D`.
- Vehicle hierarchy using an abstract `Vehicle` base class and concrete vehicle types.
- Traffic lights with RED, YELLOW, and GREEN states.
- Adaptive signal allocation based on remaining traffic, average wait time, and starvation counters.
- Emergency vehicle requests managed in FIFO order.
- Vehicle movement, queue spacing, and exit counters.
- CSV simulation report export.
- Start, pause, resume, and reset controls in the desktop dashboard.

## Java package and entry point

All application classes are in:

```text
src/com/smartcity/traffic/
```

The application entry point is:

```text
com.smartcity.traffic.Main
```

The build output is written to `bin/`.

## Project layout

```text
smart-city-traffic-control-system/
├── src/
│   └── com/smartcity/traffic/
│       ├── Main.java
│       ├── Dashboard.java
│       ├── CityMapPanel.java
│       ├── TrafficController.java
│       ├── TrafficSignal.java
│       ├── TrafficDensityCalculator.java
│       ├── Vehicle.java
│       ├── VehicleManager.java
│       ├── EmergencyManager.java
│       ├── ReportManager.java
│       └── ... vehicle and road classes
├── tests/
│   └── com/smartcity/traffic/SmokeTests.java
├── build.bat
├── run.bat
├── test.bat
├── build.sh
├── run.sh
└── test.sh
```

## Notes for demonstration

1. Start the application and click **START**.
2. Observe vehicle movement and signal changes on the four approaches.
3. Use the emergency dispatch controls to enqueue an ambulance, fire engine, or police vehicle.
4. Pause/resume or reset the simulation.
5. Generate a CSV report and choose a location to save it.

The application is an educational traffic simulation. It models traffic behavior locally; it is not connected to live road sensors, traffic cameras, GPS feeds, or municipal signal infrastructure.

## OOP concepts demonstrated

- **Abstraction:** `Vehicle` and `EmergencyVehicle` define common behavior.
- **Inheritance:** specialized vehicles extend the common vehicle classes.
- **Polymorphism:** vehicle subclasses implement their own `draw(Graphics2D)` rendering.
- **Encapsulation:** state and behavior are grouped within road, signal, vehicle, and manager classes.
- **Enums and collections:** directions, signal states, and FIFO emergency requests are modeled with Java enums and collections.
