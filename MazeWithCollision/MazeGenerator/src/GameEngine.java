
/**
 * 
 * @author AmazingGroup 
 * This class is the main class where we initialize the other classes. 
 * It draws the maze game out with different levels using the Processing core.
 * It includes both the GUI and the game engine.
 *
 */

import processing.core.*;
import java.util.concurrent.ThreadLocalRandom;
import controlP5.*;

public class GameEngine extends PApplet {

	boolean goalReached = false; // goal of the current level reached

	private int size = 5; // size of maze in cells

	private int gameSize = displayWidth / 2; // size of maze in pixel

	private int cellSize = gameSize / size; // size of one cell in pixel

	private MazeCreator creat = new RecursiveBacktracker(); // chosen maze creation // strategy

	private Player p = new Player(this, cellSize); // initialize player

	private Maze maze = new Maze(size, size, this, creat, cellSize); // initialize new
	// maze

	private int allowedTimeForLevel = 20;

	private ControlP5 cp5; // library for buttons and timer

	private Bang startButton; // button to start game
	private boolean started = false; // has game been started

	private Bang continueButton; // button to continue game
	private Bang pauseButton; // button to pause game
	private boolean paused = false; // is game paused
	// private Bang resetHighScore; //button to reset highscore

	private int timeOnPause = 0; // how long was game paused

	private ControlTimer time = new ControlTimer();

	// set starting cell for maze creation
	private int startingIndex1 = 0;
	private int startingIndex2 = ThreadLocalRandom.current().nextInt(0, maze.getHeight());;

	// set end point -> goal
	private int endIndex1 = size - 1;
	private int endIndex2 = ThreadLocalRandom.current().nextInt(0, maze.getHeight());

	private int complexityClass = 12; // initial level

	private ControlTimer pauseTimer;
	// Level statistics
	int highestLevel = complexityClass;

	public static void main(String[] args) {
		PApplet.main("GameEngine");
	}

	/**
	 * Processing methods
	 */
	// set size of output window, P3D is used for enabling lightening effects
	public void settings() {
		fullScreen(P3D);

	}

	// set up buttons and place them on their initial position at the screen
	public void setup() {
		pushMatrix();
		translate(displayWidth / 4, 0);
		cp5 = new ControlP5(this);
		startButton = new Bang(cp5, "Start");
		startButton.setPosition(displayWidth / 2, 2 * displayHeight / 3).setSize(displayWidth / 12, displayHeight / 12);
		continueButton = new Bang(cp5, "Continue");
		continueButton.setPosition(-100, -100).setSize(5, 5);
		pauseButton = new Bang(cp5, "Pause");
		pauseButton.setPosition(-20, -20).setSize(5, 5);
		popMatrix();
		p = new Player(this, cellSize);
		p.setStartPosition(200, 200);
	}

	public void draw() {
		fill(0);
		background(255);
		textSize(36);

		// if paused, show pause screen
		if (paused) {
			background(0);
			continueButton.setPosition(300, 300).setSize(60, 60);
			drawControlsText();
			timeOnPause = Math.round((pauseTimer.time() / 1000));
		}

		else {
			if (!started) {
				background(0);
				startButton.setPosition(displayWidth / 2, 2 * displayHeight / 3).setSize(60, 60);
				drawControlsText();
			}

			// when game is started
			if (started) {
				fill(0);
				background(255);
				textSize(36);
				cp5.remove("Start"); // remove start button

				// resetHighScore.setPosition(displayWidth-displayWidth/25, 500).setSize(60,60);

				// initialize highscore
				// highestLevel = getHighestLevel();

				// if goal is not reached yet
				if (!(goalReached)) {
					drawCountdown();
					drawStatistics();

					pushMatrix();
					translate(displayWidth / 4, 0);
					fill(255, 255, 255);

					textSize(20);
					text(complexityClass, 500, 500);

					// view pause button*/
					pauseButton.setPosition(displayWidth / 16, displayHeight / 2).setSize(displayWidth / 10,
							displayHeight / 12);

					// spotlight following player
					spotLight(255.0f, 255.0f, 255.0f, // color of the spotlight
							// in RGB
							p.getPositionX(), p.getPositionY(), 1000, // position of
							// spotlight
							// (follows
							// player
							// position)
							0, 0, -1, // direction in which the light point
							PI / 2, // angle of the light
							gameSize / 3); // concentration of the light

					// display maze
					fill(255, 255, 255);
					stroke(255, 255, 255);
					maze.draw();

					// print starting point
					fill(255, 0, 0);
					triangle(startingIndex1, // edge 1
							startingIndex2 * cellSize, startingIndex1, // edge 2
							startingIndex2 * cellSize + cellSize - 2, startingIndex1 + cellSize - 5, // edge3
							startingIndex2 * cellSize + cellSize / 2);

					// print end point
					fill(0, 255, 0);
					triangle(endIndex1 * cellSize + 4, endIndex2 * cellSize, endIndex1 * cellSize + 4,
							endIndex2 * cellSize + cellSize - 2,

							endIndex1 * cellSize + cellSize - 5, endIndex2 * cellSize + cellSize / 2);

					// check for collision with horizontal walls
					Wall[][] horizontalWalls = maze.getHorizontalWalls();
					for (int i = 0; i < horizontalWalls.length; i++) {
						for (int j = 0; j < horizontalWalls[0].length; j++) {
							p.collisionHorizontal(horizontalWalls[i][j]);
						}
					}

					// check for collision with vertical walls
					Wall[][] verticalWalls = maze.getVerticalWalls();
					for (int i = 0; i < verticalWalls.length; i++) {
						for (int j = 0; j < verticalWalls[0].length; j++) {
							p.collisionVertical(verticalWalls[i][j]);
						}
					}

					// move player
					p.move();

					// draw player on current position
					p.drawPlayer();

					goalReached = p.goalReached(size * cellSize);
					popMatrix();
				}

				// if goal is reached, initialize new maze
				else {

					int neededTime = Math.round((time.time() / 1000)) - timeOnPause; // time player needed
					// to finish level
					timeOnPause = 0;
					System.out.println("Completed level: " + complexityClass + " in " + neededTime + " seconds (av.: "
							+ allowedTimeForLevel + ")");
					// in case player needs more than X seconds, next level should be more
					// difficult
					if (neededTime > allowedTimeForLevel && complexityClass > 1) {
						complexityClass--;
						initializeNewMaze(complexityClass);
					} else {
						complexityClass++;
						initializeNewMaze(complexityClass);
					}
				}
			}
		}
	}

	public void drawCountdown() {
		// time needed for level in seconds start from zero
		int neededTime = Math.round((time.time() / 1000) % 60 - timeOnPause);

		int barTimerWidth = displayWidth; // width of timer rectangle
		int stepSize = barTimerWidth / allowedTimeForLevel; // decreasing rectangle for this amount
		int timer = stepSize * neededTime;

		if (neededTime < allowedTimeForLevel) {

			fill(0, 255, 0);
			rect(0 + timer / 2, displayHeight - displayHeight / 11, (displayWidth) - (timer), displayHeight / 18);
			fill(0);

		} else {
			fill(255, 0, 0);
		}
		text(allowedTimeForLevel - neededTime, displayWidth / 2, displayHeight - displayHeight / 20);
	}

	public void drawStatistics() {
		textAlign(LEFT);
		int currentLevel = complexityClass;
		textSize(displayWidth / 70);
		text("Current level: " + currentLevel, displayWidth - displayWidth / 5, displayHeight / 3);

		if (currentLevel > highestLevel) {

			fill(255);
			highestLevel = complexityClass;

		}
		text("Highest level achieved : " + highestLevel, displayWidth - displayWidth / 5, displayHeight / 4);
		text("For controls and info, \n press pause!", displayWidth / 52, displayHeight / 3);

		// Draw each tier for every complexity level
		if (complexityClass <= 5) {

			text("You are in wood-league!", displayWidth - displayWidth / 5, displayHeight - displayHeight / 3);
			fill(40, 16, 2);
			ellipse(displayWidth - displayWidth / 8, displayHeight - displayHeight / 4, gameSize / 16, gameSize / 16);
		} else if (complexityClass <= 10) {

			text("You are in Bronze-league!", displayWidth - displayWidth / 5, displayHeight - displayHeight / 3);
			fill(160, 78, 30);
			ellipse(displayWidth - displayWidth / 8, displayHeight - displayHeight / 4, gameSize / 16, gameSize / 16);
		} else if (complexityClass <= 15) {

			text("You are in Silver-league!", displayWidth - displayWidth / 5, displayHeight - displayHeight / 3);
			fill(234, 231, 229);
			ellipse(displayWidth - displayWidth / 8, displayHeight - displayHeight / 4, gameSize / 16, gameSize / 16);
		} else if (complexityClass <= 20) {

			text("You are in Gold-league!", displayWidth - displayWidth / 5, displayHeight - displayHeight / 3);
			fill(252, 156, 2);
			ellipse(displayWidth - displayWidth / 8, displayHeight - displayHeight / 4, gameSize / 16, gameSize / 16);
		} else if (complexityClass <= 25) {
			text("You are in Platinum-league!", displayWidth - displayWidth / 5, displayHeight - displayHeight / 3);
			fill(28, 119, 110);
			ellipse(displayWidth - displayWidth / 8, displayHeight - displayHeight / 4, gameSize / 16, gameSize / 16);

		} else if (complexityClass <= 30) {
			text("You are in Diamond-league!", displayWidth - displayWidth / 5, displayHeight - displayHeight / 3);
			fill(68, 160, 226);
			ellipse(displayWidth - displayWidth / 8, displayHeight - displayHeight / 4, gameSize / 16, gameSize / 16);
		} else if (complexityClass <= 35) {

			text("You are in AMAZEING-league!", displayWidth - displayWidth / 5, displayHeight - displayHeight / 3);
			fill(255, 0, 0);
			ellipse(displayWidth - displayWidth / 8, displayHeight - displayHeight / 4, gameSize / 16, gameSize / 16);
		}
	}

	public void drawControlsText() {
		fill(255);
		textSize(24);
		textAlign(CENTER);
		text("--aMAZEing-- \n \n Welcome to aMAZEing \n \n " + "Control the player using either: \n "
				+ "'W' 'A' 'S' 'D' - Keys or ARROW - Keys \n \n" + "You must find the exit before the time runs out! \n"
				+ "If you make it in time, you go up a level. \n If not, you go down a level."
				+ "The corners will light up some of the map, \n to give you an idea of where to go. \n" + "Good luck!",
				(displayWidth / 2), displayHeight / 12);
	}

	/**
	 * 
	 * User inputs: Buttons and key input
	 * 
	 */

	// process button click on start
	public void Start() {
		started = true;

		// initialize first level's maze
		initializeNewMaze(complexityClass);
	}

	// process button click on pause
	public void Pause() {
		paused = true;
		pauseTimer = new ControlTimer();
		pauseButton.setPosition(-100, -100).setSize(5, 5);
		continueButton.setPosition(200, 200).setSize(5, 5);
		fill(0);

	}

	// process button click on continue
	public void Continue() {
		paused = false;
		continueButton.setPosition(-20, -20).setSize(5, 5);
		// reset time to zero (only for testing)
	}

	// process key press to move player
	public void keyPressed() {
		if (key == 'd' || key == 'D' || keyCode == RIGHT) {
			p.setRight(true);
		}
		if (key == 's' || key == 'S' || keyCode == DOWN) {
			p.setDown(true);
		}
		if (key == 'a' || key == 'A' || keyCode == LEFT) {
			p.setLeft(true);
		}
		if (key == 'w' || key == 'W' || keyCode == UP) {
			p.setUp(true);
		}
		p.move();
	}

	public void keyReleased() {
		if (key == 'd' || key == 'D' || keyCode == RIGHT) {
			p.setRight(false);
		}
		if (key == 's' || key == 'S' || keyCode == DOWN) {
			p.setDown(false);
		}
		if (key == 'a' || key == 'A' || keyCode == LEFT) {
			p.setLeft(false);
		}
		if (key == 'w' || key == 'W' || keyCode == UP) {
			p.setUp(false);
		}
		p.move();

	}

	/**
	 * 
	 * Game Level Settings
	 * 
	 */

	// create a new maze and set player to its start
	public void initializeNewMaze(int complexity) {
		gameSize = displayWidth / 2;
		goalReached = false;
		// set time to zero
		time = new ControlTimer();

		if (complexity == 1) {
			creat = new Kruskal();// set strategy for maze creation
			size = 5;
			allowedTimeForLevel = 10;
		}

		if (complexity == 2) {
			creat = new Kruskal();// set strategy for maze creation
			size = 7;
			allowedTimeForLevel = 17;
		}

		if (complexity == 3) {
			creat = new RecursiveBacktracker();// set strategy for maze creation
			size = 5;
			allowedTimeForLevel = 9;
		}

		if (complexity == 4) {
			creat = new RecursiveBacktracker();// set strategy for maze creation
			size = 9;
			allowedTimeForLevel = 16;
		}
		if (complexity == 5) {
			creat = new RecursiveBacktracker();// set strategy for maze creation
			size = 11;
			allowedTimeForLevel = 25;
		}

		if (complexity == 6) {
			creat = new RecursiveBacktracker();// set strategy for maze creation
			size = 13;
			allowedTimeForLevel = 27;
		}

		if (complexity == 7) {
			creat = new RecursiveBacktracker();// set strategy for maze creation
			size = 7;
			allowedTimeForLevel = 15;
		}
		if (complexity == 8) {
			creat = new HuntAndKill();// set strategy for maze creation
			size = 7;
			allowedTimeForLevel = 15;
		}

		if (complexity == 9) {
			creat = new Kruskal();// set strategy for maze creation
			size = 9;
			allowedTimeForLevel = 20;
		}

		if (complexity == 10) {
			creat = new Kruskal();// set strategy for maze creation
			size = 11;
			allowedTimeForLevel = 23;
		}
		if (complexity == 11) {
			creat = new RecursiveBacktracker();// set strategy for maze creation
			size = 15;
			allowedTimeForLevel = 33;
		}

		if (complexity == 12) {
			creat = new HuntAndKill();// set strategy for maze creation
			size = 5;
			allowedTimeForLevel = 14;
		}

		if (complexity == 13) {
			creat = new Kruskal();// set strategy for maze creation
			size = 13;
			allowedTimeForLevel = 32;
		}
		if (complexity == 14) {
			creat = new Kruskal();// set strategy for maze creation
			size = 15;
			allowedTimeForLevel = 36;
		}

		if (complexity == 15) {
			creat = new HuntAndKill();// set strategy for maze creation
			size = 11;
			allowedTimeForLevel = 39;
		}

		if (complexity == 16) {
			creat = new HuntAndKill();// set strategy for maze creation
			size = 9;
			allowedTimeForLevel = 36;
		}

		if (complexity == 17) {
			creat = new HuntAndKill();// set strategy for maze creation
			size = 13;
			allowedTimeForLevel = 35;
		}

		if (complexity == 18) {
			creat = new HuntAndKill();// set strategy for maze creation
			size = 15;
			allowedTimeForLevel = 44;
		}
		if (complexity == 19) {
			creat = new HuntAndKill();// set strategy for maze creation
			size = 17;
			allowedTimeForLevel = 55;
		}

		if (complexity == 20) {
			creat = new Kruskal();// set strategy for maze creation
			size = 17;
			allowedTimeForLevel = 43;
		}

		if (complexity == 21) {
			creat = new Kruskal();// set strategy for maze creation
			size = 19;
			allowedTimeForLevel = 50;
		}
		if (complexity > 21) {
			creat = new HuntAndKill();// set strategy for maze creation
			size = size + 2;
			allowedTimeForLevel = allowedTimeForLevel + 2;
		}

		cellSize = gameSize / size;
		p = new Player(this, cellSize);// initialize player

		maze = new Maze(size, size, this, creat, cellSize); // initialize new maze grid

		startingIndex1 = 0;
		startingIndex2 = ThreadLocalRandom.current().nextInt(0, maze.getHeight());

		// set end point -> goal
		endIndex1 = size - 1;
		endIndex2 = ThreadLocalRandom.current().nextInt(0, maze.getHeight());
		// create maze with initialized criteria
		maze.getMazeCreator().createMaze(maze.getCell(startingIndex1, startingIndex2),
				maze.getCell(endIndex1, endIndex2));

		// set startingPosition of player
		p.setStartPosition(startingIndex1 * maze.getCellSize() + maze.getCellSize() / 3 + 2,
				startingIndex2 * maze.getCellSize() + maze.getCellSize() / 2);

	}

}
