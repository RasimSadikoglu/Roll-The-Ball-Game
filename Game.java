import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Game extends Application {

	// Game Variables
	private Pipe[][] pipes = new Pipe[4][4];
	private int levelCount = 1;
	private Pane gamePane = new Pane();
	private int score = 0;
	private long time = 0;
	private Text scoreText = new Text(10, 20, String.format("Score: %d", score));
	private int starterLocation = -1;
	private ImageView ball;

	// Event Variables
	private Pipe chosenPipe;
	private int chosenX, chosenY;
	private int mouseFirstX, mouseFirstY;
	private boolean firstClick = true;

	// Score Variables
	private TextArea scores = new TextArea();
	private TextArea timings = new TextArea();

	// End Screen Variables
	private boolean isEnd;
	
	// Animation Variables
	private Timeline control;
	private boolean animate;
	private int animationX, animationY;
	private int count = 1;
	private int dir;
	private int ballSpeed = 5; // 100 % ballSpeed needs to be 0 in order to prevent animation bugs.

	@Override
	public void start(Stage primaryStage) throws Exception {
		initStart();

		// When mouse is dragging, do movement
		gamePane.setOnMouseDragged(e -> {
			if (firstClick) { // In first click choose the pipe that is clicked
				mouseFirstX = (int) e.getSceneX();
				mouseFirstY = (int) e.getSceneY();
				int x = mouseFirstX;
				int y = mouseFirstY;
				if (x % 100 > 5 && x % 100 < 95 && y % 100 > 5 && y % 100 < 95) { // Check the click location if is the border or not
					x /= 100;
					y /= 100;
					if (pipes[y][x].movable) {
						chosenPipe = pipes[y][x];
						chosenX = x;
						chosenY = y;
					}
				}
				firstClick = false;
			} else if (chosenPipe != null) { // When mouse is dragging move the chosen pipe if desired location is empty
				
				// Find the directions that pipe can go
				boolean[] dir = findDirections(); 
	
				// Check the distance from the center in order to prevent bugs, for example otherwise you can go non empty areas.
				double distanceFromCenterX = Math.abs(chosenPipe.getX() - (chosenX * 100 + 50));
				double distanceFromCenterY = Math.abs(chosenPipe.getY() - (chosenY * 100 + 50));
				
				// Check the distance that how far did mouse moved after the first click
				double moveX = e.getSceneX() - mouseFirstX;
				double moveY = e.getSceneY() - mouseFirstY;
				
				// Set new location to the pipe
				if (dir[0] && distanceFromCenterX < 5 && Math.abs(moveY + 50) < 55) {
					chosenPipe.setY(chosenY * 100 + 50 + moveY);
				}
				if (dir[1] && distanceFromCenterY < 5 && Math.abs(moveX - 50) < 55) {
					chosenPipe.setX(chosenX * 100 + 50 + moveX);
				}
				if (dir[2] && distanceFromCenterX < 5 && Math.abs(moveY - 50) < 55) {
					chosenPipe.setY(chosenY * 100 + 50 + moveY);
				}
				if (dir[3] && distanceFromCenterY < 5 && Math.abs(moveX + 50) < 55) {
					chosenPipe.setX(chosenX * 100 + 50 + moveX);
				}
			}
		});

		// When mouse released, do calibration
		gamePane.setOnMouseReleased(e -> {
			if (chosenPipe != null) { // If any pipe is chosen
				
				// Find it array index
				int x = (int) chosenPipe.getX() / 100;
				int y = (int) chosenPipe.getY() / 100;
				
				/* Check the location if it is valid or not same to the initial phase, 
				 * if it is swap the pipes, otherwise reset its location. */
				if (pipes[y][x].isFree && (chosenX != x || chosenY != y)) {
					chosenPipe.setX(x * 100 + 50);
					chosenPipe.setY(y * 100 + 50);
					swap(chosenX, chosenY, x, y);
				} else {
					chosenPipe.setX(chosenX * 100 + 50);
					chosenPipe.setY(chosenY * 100 + 50);
				}
			}
			chosenPipe = null;
			firstClick = true;
		});

		// Check level status and do ball animation
		control = new Timeline(new KeyFrame(Duration.millis(1000 / 60), e -> {
			
			// Check level is solved and animation has not started yet
			if (!animate && isSolved()) {
				animate = true;
				time = System.currentTimeMillis() - time;
				timings.setText(String.format("%sLevel %d: %s%n", timings.getText(), levelCount, calculateTime()));
				scores.setText(String.format("%sLevel %d: %d%n", scores.getText(), levelCount, score));
				score = 0;
				levelCount++;
				
				// This will prevent movement while animating
				for (int i = 0; i < 16; i++) {
					pipes[i / 4][i % 4].movable = false;
				}

				// Set direction for ball animaton
				dir = pipes[animationY][animationX].exits[0];
				animationX += dir % 4;
				animationY += dir / 4;
			}
			
			// When level is solved, animation starts
			if (animate) {
				if (count == 100 / ballSpeed + 1) { // Set new direction or initialize new level every fixed frames
					if (pipes[animationY][animationX].isEnd) {
						animate = false;
						count = 0;
						initLevel();
						initBoard();
						scoreText.setText(String.format("Score: %d", score));
					} else { // Set new direction
						int[] exits = pipes[animationY][animationX].exits;
						dir = exits[0] == dir * -1 ? exits[1] : exits[0];
						animationX += dir % 4;
						animationY += dir / 4;
						count = 0;
					}
				} else if (Math.abs(dir) == 1) { // Do horizontal movement
					ball.setX(ball.getX() + dir * ballSpeed);
				} else if (Math.abs(dir) == 4) { // Do vertical movement
					ball.setY(ball.getY() + (dir / 4) * ballSpeed);
				}
				count++;
			}
		}));
		control.setCycleCount(Timeline.INDEFINITE);

		// Set stage
		primaryStage.setScene(new Scene(gamePane, 400, 400));
		primaryStage.setTitle("Roll The Ball");
		primaryStage.show();
	}

	private void initStart() {
		// Create ball and locate
		ball = new ImageView("images/ball.png");
		ball.setTranslateX(-15);
		ball.setTranslateY(-15);
		
		// Set text and textareas
		scoreText.setFont(Font.font("", FontWeight.BLACK, 15));
		scores.setText("Scores:\n\n");
		timings.setText("Timings:\n\n");
		
		// Add start background
		gamePane.getChildren().add(new ImageView(new Image("images/start.png")));

		// Create start button
		Button start = new Button("START");
		start.setPrefSize(160, 50);
		start.setLayoutX(120);
		start.setLayoutY(200);
		start.setOnMouseClicked(e -> {
			initLevel();
			initBoard();
		});
		gamePane.getChildren().add(start);
	}

	private void initEnd() {
		// Clear the pane
		gamePane.getChildren().clear();
		
		// Add end screen background
		gamePane.getChildren().add(new ImageView(new Image("images/scores.png")));

		// Create main menu button
		Button mainMenu = new Button("MAIN MENU");
		mainMenu.setPrefSize(90, 30);
		mainMenu.setLayoutX(90);
		mainMenu.setLayoutY(320);
		mainMenu.setOpacity(0.9);
		mainMenu.setOnMouseClicked(e -> {
			levelCount = 1;
			scores.clear();
			timings.clear();
			initStart();
		});

		// Create exit button
		Button exit = new Button("EXIT");
		exit.setPrefSize(90, 30);
		exit.setLayoutX(220);
		exit.setLayoutY(320);
		exit.setOpacity(0.9);
		exit.setOnMouseClicked(e -> System.exit(0));

		// Calibrate scores and timings textareas
		scores.setPrefSize(120, 200);
		scores.setLayoutX(60);
		scores.setLayoutY(100);
		scores.setEditable(false);
		timings.setPrefSize(120, 200);
		timings.setLayoutX(220);
		timings.setLayoutY(100);
		timings.setEditable(false);

		gamePane.getChildren().addAll(mainMenu, exit, scores, timings);
	}

	// Read level file and create pipe objects
	private void initLevel() {
		// Stop animation while creating level in order to prevent bugs (neven encountered but may cause some bugs)
		control.stop();
		
		// Create file with given level
		File levelText = new File(String.format("levels/level%d.txt", levelCount));
		try {
			Scanner fileScanner = new Scanner(levelText);
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					pipes[i][j] = new Pipe(fileScanner.nextLine(), j * 100 + 50, i * 100 + 50);
					if (pipes[i][j].isStarter) {
						starterLocation = i * 4 + j;
						animationX = j;
						animationY = i;
					}
				}
			}
			fileScanner.close();
			time = System.currentTimeMillis(); // Start timer for timings
		} catch (FileNotFoundException e) {
			isEnd = true; // If there is no file for that level initialize end
		}
	}

	// Edit pane according to pipes array and check that if it is level solved
	private void initBoard() {
		if (isEnd) {
			isEnd = false;
			control.stop();
			initEnd();
		} else {
			// Clear the pane
			gamePane.getChildren().clear();
			
			// Add level background
			gamePane.getChildren().add(new ImageView(new Image("images/background.png")));
			
			// Add pipe objects to the pane
			for (int i = 0; i < 4; i++) {
				gamePane.getChildren().addAll(pipes[i]);
			}

			// Create Ball
			int ballX = (starterLocation % 4) * 100 + 50;
			int ballY = (starterLocation / 4) * 100 + 50;
			ball.setX(ballX);
			ball.setY(ballY);

			gamePane.getChildren().addAll(scoreText, ball);
			
			control.play();
		}
	}

	// Look level is solved or not
	private boolean isSolved() {
		// If there is no starter return false
		if (starterLocation == -1) return false;
		
		// Find starter location on two dimension
		int x = starterLocation % 4;
		int y = starterLocation / 4;
		
		// Take first direction of starter pipe (further explanation is on pipe class!)
		int dir = pipes[y][x].exits[0];
		
		// While ball can move check the movement is valid or not
		while (true) {
			// Add direction to the location
			x += dir % 4;
			y += dir / 4;
			
			// If new location is invalid return false
			if (x < 0 || x > 3 || y < 0 || y > 3) return false;
			
			// Multiply the direction with -1
			dir *= -1;
			
			// Take new direction of new location
			int[] exits = pipes[y][x].exits;
			
			// Check the reverse that can ball go to the previous location
			if (exits[0] != dir && exits[1] != dir) return false;
			
			// Check is the new location end
			if (pipes[y][x].isEnd) {
				starterLocation = -1;
				return true;
			}
			
			// Take new direction
			dir = exits[0] != dir ? exits[0] : exits[1];
		}
	}

	// Find possible directions that pipe can go (0 for top, 1 for right, 2 for bottom, 3 for left)
	private boolean[] findDirections() {
		boolean[] dir = new boolean[4];
		if (chosenY > 0) dir[0] = pipes[chosenY - 1][chosenX].isFree;
		if (chosenX < 3) dir[1] = pipes[chosenY][chosenX + 1].isFree;
		if (chosenY < 3) dir[2] = pipes[chosenY + 1][chosenX].isFree;
		if (chosenX > 0) dir[3] = pipes[chosenY][chosenX - 1].isFree;
		return dir;
	}

	// Calculate time that passes on each level
	public String calculateTime() {
		int splitSecond = ((int) time / 10) % 100;
		int second = ((int) time / 1000) % 60;
		int minute = ((int) time / 60000) % 60;
		return String.format("%02d:%02d:%02d", minute, second, splitSecond);
	}

	// Swap two pipes in the array and update score
	public void swap(int x1, int y1, int x2, int y2) {
		Pipe temp = pipes[y1][x1];
		pipes[y1][x1] = pipes[y2][x2];
		pipes[y2][x2] = temp;
		score += Math.abs(x1 - x2) + Math.abs(y1 - y2);
		scoreText.setText(String.format("Score: %d", score));
	}

	public static void main(String[] args) {
		launch();
	}
}