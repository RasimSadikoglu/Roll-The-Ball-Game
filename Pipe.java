// This class extends imageview class and add some more properties to work with

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class Pipe extends ImageView {
	
	// Textures
	public static final Image STARTER = new Image("images/starter.png");
	public static final Image END = new Image("images/end.png");
	public static final Image PIPESTATIC = new Image("images/pipestatic.png");
	public static final Image PIPESTATICCURVED = new Image("images/pipestaticcurved.png");
	public static final Image PIPECURVED = new Image("images/pipecurved.png");
	public static final Image EMPTY = new Image("images/empty.png");
	public static final Image PIPE = new Image("images/pipe.png");
	
	boolean movable, isStarter, isEnd;
	boolean isFree = false;
	
	/* This represents the endpoints of the pipes (1 for Right, -1 for Left, 4 for Bottom, -4 for Top)
	 * if you divide that number by four you will get the displacement on y-axis,
	 * if you modulo that number by four you will get the displacement on x-axis */
	int[] exits = new int[2];
	
	Pipe(String line, int x, int y) {
		
		String[] properties = line.split(",");
		createImageView(properties[1], properties[2]);
		
		setX(x);
		setY(y);
		setTranslateX(-45);
		setTranslateY(-45);
	}
	
	private void createImageView(String type, String alignment) {

		switch (type) {
			case "Starter":
				isStarter = true;
				setImage(STARTER);
				if (alignment.equals("Horizontal")) {
					setRotate(90);
					exits[0] = -1; exits[1] = -1;
				} else if (alignment.equals("Vertical")) {
					exits[0] = 4; exits[1] = 4;
				} else if (alignment.equals("HorizontalI")) {
					setRotate(-90);
					exits[0] = 1; exits[1] = 1;
				} else if (alignment.equals("VerticalI")) {
					setRotate(180);
					exits[0] = -4; exits[1] = -4;
				}
				break;
			case "End":
				isEnd = true;
				setImage(END);
				if (alignment.equals("Horizontal")) {
					setRotate(90);
					exits[0] = -1; exits[1] = -1;
				} else if (alignment.equals("Vertical")) {
					exits[0] = 4; exits[1] = 4;
				} else if (alignment.equals("HorizontalI")) {
					setRotate(-90);
					exits[0] = 1; exits[1] = 1;
				} else if (alignment.equals("VerticalI")) {
					setRotate(180);
					exits[0] = -4; exits[1] = -4;
				}
				break;
			case "PipeStatic":
				if (alignment.length() > 2) {
					setImage(PIPESTATIC);
					if (alignment.equals("Horizontal")) {
						setRotate(90);
						exits[0] = 1; exits[1] = -1;
					} else if (alignment.equals("Vertical")) {
						exits[0] = -4; exits[1] = 4;
					}
				} else {
					setImage(PIPESTATICCURVED);
					if (alignment.equals("01")) {
						setRotate(90);
						exits[0] = -4; exits[1] = 1;
					} else if (alignment.equals("10")) {
						setRotate(-90);
						exits[0] = 4; exits[1] = -1;
					} else if (alignment.equals("11")) {
						setRotate(180);
						exits[0] = 1; exits[1] = 4;
					} else if (alignment.equals("00")) {
						exits[0] = -4; exits[1] = -1;
					}
				}
				break;
			case "Empty":
				if (alignment.equals("none")) {
					setImage(EMPTY);
					movable = true;
				} else if (alignment.equals("Free")) {
					isFree = true;
				}
				break;
			case "Pipe":
				movable = true;
				if (alignment.length() > 2) {
					setImage(PIPE);
					if (alignment.equals("Horizontal")) {
						setRotate(90);
						exits[0] = 1; exits[1] = -1;
					} else if (alignment.equals("Vertical")) {
						exits[0] = -4; exits[1] = 4;
					}
				} else {
					setImage(PIPECURVED);
					if (alignment.equals("01")) {
						setRotate(90);
						exits[0] = -4; exits[1] = 1;
					} else if (alignment.equals("10")) {
						setRotate(-90);
						exits[0] = 4; exits[1] = -1;
					} else if (alignment.equals("11")) {
						setRotate(180);
						exits[0] = 1; exits[1] = 4;
					} else if (alignment.equals("00")) {
						exits[0] = -4; exits[1] = -1;
					}
				}
				break;
		}
	}
}