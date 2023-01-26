import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents an abstract cell, either a cell or an empty cell
abstract class ACell {
  // the four adjacent cells to this one
  ACell left;
  ACell top;
  ACell right;
  ACell bottom;

  // mutates this cell's connections using the given cells
  // no effect on an empty cell
  void connectCell(ACell l, ACell t, ACell r, ACell b) {
    //empty here because an abstract cell has no data
  }

  // Draws a singular cell
  // Does nothing for an empty cell
  void draw(WorldScene scene) { 
    //empty here because an abstract cell has no data
  }

  //recursively floods immediately floodable cells
  // Does nothing on an empty cell
  void flood() {
    // Does nothing on an empty cell
  }

  // changes flooded cells to the given color, 
  // changes nonflooded cells of the same color to become flooded
  // returns 1 if this cell was both flooded and the same color
  // returns 0 for an empty cell as a recursive base case
  // returns 2 if an unflooded cell becomes flooded
  int makeFlooded(Color c) {
    return 1;
  }
}

// Represents a single square of the game area
class Cell extends ACell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;

  // Constructor used for testing
  // allows you to set the flooded field to true
  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.left = new EmptyCell();
    this.top = new EmptyCell();
    this.right = new EmptyCell(); 
    this.bottom = new EmptyCell();
    this.flooded = flooded;
  }

  // this is used as a placeholder for testing cells
  Cell() {
    this(0, 0, null, false);
  }

  // makes a cell with an x, y, and color but no connections
  Cell(int x, int y, Color color) {
    this(x, y, color, false);
  }

  //Draws a singular cell on top of a given scene at a specified logical coordinate
  void draw(WorldScene scene) {
    scene.placeImageXY(new RectangleImage(
        FloodItWorld.CELLSIZE, FloodItWorld.CELLSIZE, OutlineMode.SOLID, this.color),
        this.x * FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2),
        this.y * FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2));
  }

  //EFFECT: mutates this cell's connections using the given cells
  void connectCell(ACell l, ACell t, ACell r, ACell b) {
    this.left = l;
    this.right = r;
    this.top = t;
    this.bottom = b;
  }

  // EFFECT: recursively floods immediately floodable cells
  public void flood() {
    if (this.right.makeFlooded(this.color) == 1) {
      this.right.flood();
    }
    if (this.left.makeFlooded(this.color) == 2) {
      this.left.flood();
    }
    if (this.bottom.makeFlooded(this.color) == 1) {
      this.bottom.flood();
    }
    if (this.top.makeFlooded(this.color) == 2) {
      this.top.flood();
    }
  }

  // changes flooded cells to the given color, 
  // changes nonflooded cells of the same color to become flooded
  // returns 1 if this cell was both flooded and the same color
  // returns 0 as a recursive base case
  // returns 2 if an unflooded cell becomews flooded
  int makeFlooded(Color c) {
    if (this.flooded) {
      if (this.color.equals(c)) {
        return 1;
      }
      else {
        this.color = c;
        return 2;
      }
    }
    else if (this.color.equals(c)) {
      this.flooded = true;
      return 1;
    }
    return 0;
  }
}

// represents an empty cell with no data
//this is used for the surroundings of edge cells
// because it is a different class, we can take advantage
// of dymanic dispatch to make the methods work without adding any extra logic
class EmptyCell extends ACell {
  EmptyCell() {
    // empty constructor
  }

  //
  // Base case
  int makeFlooded(Color c) {
    return 1;
  }

  // EmptyCell does not override any other methods from ACell because
  // they would not do anything
}

// to represent the game world
class FloodItWorld extends World {

  // All the cells of the game
  ArrayList<Cell> board;
  static ArrayList<Color> colorList = new ArrayList<Color>(Arrays.asList(
      Color.BLUE, Color.CYAN, Color.GREEN,
      Color.MAGENTA, Color.RED, Color.YELLOW,
      Color.ORANGE, Color.LIGHT_GRAY));
  int size;
  int width;
  int height;
  static int CELLSIZE = 40;
  int colors;
  int guesses;
  Random rand;

  // constructs a new FloodItWorld with all necessary fields given
  FloodItWorld(int size, int colors, ArrayList<Cell> board, Random random) {
    if (size < 2) {
      throw new IllegalArgumentException("Board size must be greater than 1");
    }
    if (colors > 8) {
      throw new IllegalArgumentException("Maximum number of colors is 8");
    }
    this.size = size;
    this.colors = colors;
    this.rand = random;
    this.width = size * FloodItWorld.CELLSIZE;
    this.height = size * FloodItWorld.CELLSIZE;
    this.guesses = size + colors;

    if (board.size() > 0) {
      this.board = board;
    }
    else {
      this.board = makeBoard();
      this.connect();
    }
  }

  // Constructor for gameplay
  FloodItWorld(int size, int colors) {
    this(size, colors, new ArrayList<Cell>(0), new Random());
  }

  // Constructor that takes a sample board for testing
  FloodItWorld(ArrayList<Cell> board, int colors, Random random) {
    this((int)Math.sqrt(board.size()), colors, board,  random);
  }

  // Constructor with a pseudorandom seed used for testing
  FloodItWorld(int size, int colors, Random random) {
    this(size, colors, new ArrayList<Cell>(0), random);
  }

  // Creates an arrayList representing the cells on the board of this FloodITWorld
  public ArrayList<Cell> makeBoard() {
    ArrayList<Cell> b = new ArrayList<Cell>(this.size * this.size);

    b.add(new Cell(0, 0, randomColor())); // top left corner cell
    for (int x = 1; x < this.size - 1; x++) {
      b.add(new Cell(x, 0, randomColor()));  // this is the top row
    }
    b.add(new Cell(this.size - 1, 0, randomColor()));  // top right corner cell

    for (int y = 1; y < this.size - 1; y++) {
      b.add(new Cell(0, y, randomColor()));  //these are all the left edges
      for (int x = 1; x < this.size - 1; x++) {
        b.add(new Cell(x, y, randomColor()));  // these are the non-edge cells
      }
      b.add(new Cell(this.size - 1, y, randomColor()));  //these are all the right edges
    }

    b.add(new Cell(0, this.size - 1, randomColor()));  // bottom left corner 
    for (int x = 1; x < this.size - 1; x++) {
      b.add(new Cell(x, this.size - 1, randomColor()));  // this is the bottom row
    }
    b.add(new Cell(this.size - 1, this.size - 1, randomColor())); // bottom right corner

    return b;
  }

  // connects the cells of this board together
  public void connect() {
    this.board.get(0).connectCell(
        new EmptyCell(),new EmptyCell(),this.board.get(1),this.board.get(this.size));
    for (int z = 1; z < this.size - 1; z++) {
      this.board.get(z).connectCell(
          this.board.get(z - 1), new EmptyCell(),
          this.board.get(z + 1),this.board.get(z + this.size));
    }
    this.board.get(this.size - 1).connectCell(
        this.board.get(this.size - 2), new EmptyCell(),
        new EmptyCell(),this.board.get(this.size + this.size - 1));

    for (int y = 1; y < this.size - 1; y++) {
      this.board.get(y * this.size).connectCell(
          new EmptyCell(), this.board.get(this.size * (y - 1)),
          this.board.get(y * this.size + 1), this.board.get(y * this.size + this.size));
      for (int x = 1; x < this.size - 1; x++) {
        this.board.get(y * this.size + x).connectCell(
            this.board.get(y * this.size + x - 1), this.board.get(this.size * (y - 1) + x),
            this.board.get(y * this.size + x + 1), this.board.get(y * this.size + this.size + x));
      }
      this.board.get(y * this.size + this.size - 1).connectCell(
          this.board.get(y * this.size + this.size - 2), this.board.get(y * this.size - 1),
          new EmptyCell(), this.board.get(y * this.size + (this.size * 2) - 1));
    }

    this.board.get(this.size * this.size - this.size).connectCell(
        new EmptyCell(), this.board.get(this.size * this.size - (this.size * 2)), 
        this.board.get(this.size * this.size - this.size  + 1), new EmptyCell());
    for (int i = this.size * this.size - this.size + 1; i < this.size * this.size - 2; i++) {
      this.board.get(i).connectCell(
          this.board.get(i - 1),this.board.get(i - this.size),
          this.board.get(i + 1), new EmptyCell());
    }
    this.board.get(this.size * this.size - 1).connectCell(
        this.board.get(this.size * this.size - 2),
        this.board.get(this.size * this.size - this.size - 1), new EmptyCell(), new EmptyCell());
    this.board.get(0).flooded = true;
  }


  @Override
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width, this.height);

    for (int i = 0; i < this.board.size(); i++) {
      this.board.get(i).draw(scene);
    }
    drawInfo(scene);
    return scene;
  }

  // Adds the information (number of guesses) to the scene
  public void drawInfo(WorldScene s) {
    s.placeImageXY(new TextImage(("Guesses Remaining: " 
        + this.guesses), Color.black), FloodItWorld.CELLSIZE * this.size + 90, 100);
    if (!(this.isWin()) && this.guesses <= 0) {
      s.placeImageXY(new TextImage(("Game Over"), 18, FontStyle.BOLD, Color.BLACK),
          FloodItWorld.CELLSIZE * this.size / 2, FloodItWorld.CELLSIZE * this.size / 2);
      s.placeImageXY(new TextImage(("Press r to play again"), 18, FontStyle.BOLD, Color.BLACK),
          FloodItWorld.CELLSIZE * this.size / 2, FloodItWorld.CELLSIZE * this.size / 2 + 30);
    }
    if (this.isWin() && this.guesses >= 0) {
      s.placeImageXY(new TextImage(("Congratulations,"), 18, FontStyle.BOLD, Color.BLACK),
          FloodItWorld.CELLSIZE * this.size / 2, FloodItWorld.CELLSIZE * this.size / 2);
      s.placeImageXY(new TextImage(("You won!"), 18, FontStyle.BOLD, Color.BLACK),
          FloodItWorld.CELLSIZE * this.size / 2, FloodItWorld.CELLSIZE * this.size / 2 + 30);
    }
  }

  // Returns true if all the cells have been flooded
  public boolean isWin() {
    int count = 0;
    for (int i = 0; i < this.size * this.size; i++) {
      if (this.board.get(i).flooded) {
        count++;
      }
    }
    return count >= this.size * this.size;
  }

  // returns a random color (up to 10 different colors)
  public Color randomColor() {
    return FloodItWorld.colorList.get(this.rand.nextInt(this.colors));
  }

  @Override
  public void onTick() {
    this.board.get(0).flood();
  }

  @Override
  public void onMouseClicked(Posn pos) {
    this.board.get(0).color = this.find(pos).color;
    this.guesses--;
  }

  @Override
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = makeBoard();
      this.connect();
      // need to set this to same as whatever we make guesses calculated by
      this.guesses = this.colors + this.size;
    }
  }

  // returns the cell at the given posn
  public Cell find(Posn pos) {
    return this.board.get((int)Math.floor(pos.x / FloodItWorld.CELLSIZE)
       + ((int)Math.floor(pos.y / FloodItWorld.CELLSIZE) * this.size));
  }
}

// to represent examples of FloodIt
class ExamplesFloodIt {

  EmptyCell empty;

  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  Cell cell6;
  Cell cell7;
  Cell cell8;
  Cell cell9;

  ArrayList<Cell> exampleBoard1;
  ArrayList<Cell> exampleBoard2;
  FloodItWorld exampleWorld1;
  FloodItWorld exampleWorld2;
  FloodItWorld exampleWorld3;

  FloodItWorld nonRandomWorld1;
  FloodItWorld nonRandomWorld2;
  FloodItWorld nonRandomWorld3;

  FloodItWorld endLose;
  FloodItWorld endWin;

  WorldScene emptyScene;
  WorldScene emptyScene2;
  WorldScene sceneWithOneCell;
  WorldScene sceneWithTwoCell;
  WorldScene sceneWithThreeCell;
  WorldScene nonRandomWorld2Scene;
  WorldScene emptySceneWithText;
  WorldScene emptySceneLose;
  WorldScene emptySceneWin;
  WorldScene nonRandomWorld2SceneLose;
  WorldScene nonRandomWorld2SceneWin;
  

  // Sets the initial conditions for testing
  void initConditions() {

    this.empty = new EmptyCell();

    this.cell1 = new Cell(0, 0, Color.RED); // top left
    this.cell2 = new Cell(1, 0, Color.BLUE); // top middle
    this.cell3 = new Cell(2, 0, Color.BLUE); // top right
    this.cell4 = new Cell(0, 1, Color.CYAN); // middle left
    this.cell5 = new Cell(1, 1, Color.MAGENTA); // middle middle
    this.cell6 = new Cell(2, 1, Color.GREEN); // middle right
    this.cell7 = new Cell(0, 2, Color.RED); // bottom left
    this.cell8 = new Cell(1, 2, Color.RED); // bottom middle
    this.cell9 = new Cell(2, 2, Color.RED); // bottom right

    this.exampleBoard1 = new ArrayList<Cell>(
        Arrays.asList(
            this.cell1, this.cell2, this.cell3,
            this.cell4, this.cell5, this.cell6,
            this.cell7, this.cell8, this.cell9));
    this.exampleWorld1 = new FloodItWorld(this.exampleBoard1, 5, new Random(3));
    this.exampleWorld1.connect();

    // placeholder
    this.exampleBoard2 = new ArrayList<Cell>(
        Arrays.asList(
            new Cell(), new Cell(), new Cell(),
            new Cell(), new Cell(), new Cell(),
            new Cell(), new Cell(), new Cell()));
    this.nonRandomWorld3 = new FloodItWorld(this.exampleBoard2, 5, new Random(3));


    this.exampleWorld2 = new FloodItWorld(4, 5);
    this.exampleWorld3 = new FloodItWorld(8, 8);

    this.nonRandomWorld1 = new FloodItWorld(4, 5, new Random(3));
    this.nonRandomWorld2 = new FloodItWorld(2, 3, new Random(1));

    this.endLose = new FloodItWorld(2, 3, new Random(1));
    this.endLose.guesses = 0;

    this.endWin = new FloodItWorld(2, 3, new Random(1));
    // Sets the flooded field of each cell in endWin to true
    // Used for testing the end of the game where a player has won
    for (int i = 0; i < 4; i++) {
      this.endWin.board.get(i).flooded = true;
    }

    this.emptyScene = new WorldScene(80, 80);
    this.emptyScene2 = new WorldScene(80, 80);

    this.sceneWithOneCell = new WorldScene(80, 80);
    this.sceneWithOneCell.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.RED),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE / 2);

    this.sceneWithTwoCell = new WorldScene(80, 80);
    this.sceneWithTwoCell.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.RED),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE / 2);
    this.sceneWithTwoCell.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.BLUE),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2), FloodItWorld.CELLSIZE / 2);

    this.sceneWithThreeCell = new WorldScene(80, 80);
    this.sceneWithThreeCell.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.RED),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE / 2);
    this.sceneWithThreeCell.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.BLUE),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2), FloodItWorld.CELLSIZE / 2);
    this.sceneWithThreeCell.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.CYAN),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2));

    this.nonRandomWorld2Scene = new WorldScene(80, 80);
    this.nonRandomWorld2Scene.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.BLUE),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE / 2);
    this.nonRandomWorld2Scene.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.CYAN),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2), FloodItWorld.CELLSIZE / 2);
    this.nonRandomWorld2Scene.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.CYAN),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2));
    this.nonRandomWorld2Scene.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.BLUE),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2));    

    this.emptySceneWithText = new WorldScene(80, 80);
    this.emptySceneWithText.placeImageXY(new TextImage(("Guesses Remaining: 30"), 
        Color.black), FloodItWorld.CELLSIZE * 2 + 90, 100);

    this.emptySceneLose = new WorldScene(80, 80);
    this.emptySceneLose.placeImageXY(new TextImage(("Game Over"), 18, FontStyle.BOLD, Color.BLACK),
        FloodItWorld.CELLSIZE * 2 / 2, FloodItWorld.CELLSIZE * 2 / 2);
    this.emptySceneLose.placeImageXY(new TextImage(("Press r to play again"), 
        18, FontStyle.BOLD, Color.BLACK),
        FloodItWorld.CELLSIZE * 2 / 2, FloodItWorld.CELLSIZE * 2 / 2 + 30);

    this.emptySceneWin = new WorldScene(80, 80);
    this.emptySceneWin.placeImageXY(new TextImage(("Congratulations,"),
        18, FontStyle.BOLD, Color.BLACK),
        FloodItWorld.CELLSIZE * 2 / 2, FloodItWorld.CELLSIZE * 2 / 2);
    this.emptySceneWin.placeImageXY(new TextImage(("You won!"), 18, FontStyle.BOLD, Color.BLACK),
        FloodItWorld.CELLSIZE * 2 / 2, FloodItWorld.CELLSIZE * 2 / 2 + 30);
    
    this.nonRandomWorld2SceneLose = new WorldScene(80, 80);
    this.nonRandomWorld2SceneLose.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.BLUE),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE / 2);
    this.nonRandomWorld2SceneLose.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.CYAN),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2), FloodItWorld.CELLSIZE / 2);
    this.nonRandomWorld2SceneLose.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.CYAN),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2));
    this.nonRandomWorld2SceneLose.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.BLUE),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2));
    this.nonRandomWorld2SceneLose.placeImageXY(new TextImage(("Game Over"),
        18, FontStyle.BOLD, Color.BLACK),
        FloodItWorld.CELLSIZE * 2 / 2, FloodItWorld.CELLSIZE * 2 / 2);
    this.nonRandomWorld2SceneLose.placeImageXY(new TextImage(("Press r to play again"),
        18, FontStyle.BOLD, Color.BLACK),
        FloodItWorld.CELLSIZE * 2 / 2, FloodItWorld.CELLSIZE * 2 / 2 + 30);
    
    this.nonRandomWorld2SceneWin = new WorldScene(80, 80);
    this.nonRandomWorld2SceneWin.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.BLUE),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE / 2);
    this.nonRandomWorld2SceneWin.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.CYAN),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2), FloodItWorld.CELLSIZE / 2);
    this.nonRandomWorld2SceneWin.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.CYAN),
        FloodItWorld.CELLSIZE / 2, FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2));
    this.nonRandomWorld2SceneWin.placeImageXY(
        new RectangleImage(FloodItWorld.CELLSIZE,
            FloodItWorld.CELLSIZE, OutlineMode.SOLID, Color.BLUE),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2),
        FloodItWorld.CELLSIZE + (FloodItWorld.CELLSIZE / 2));
    this.nonRandomWorld2SceneWin.placeImageXY(new TextImage(("Congratulations,"),
        18, FontStyle.BOLD, Color.BLACK),
        FloodItWorld.CELLSIZE * 2 / 2, FloodItWorld.CELLSIZE * 2 / 2);
    this.nonRandomWorld2SceneWin.placeImageXY(new TextImage(("You won!"),
        18, FontStyle.BOLD, Color.BLACK),
        FloodItWorld.CELLSIZE * 2 / 2, FloodItWorld.CELLSIZE * 2 / 2 + 30); 
  }

  // used to test bigbang
  void testWorld(Tester t) {
    this.initConditions();
    FloodItWorld world = this.exampleWorld3;
    world.bigBang(world.size * FloodItWorld.CELLSIZE + 175,
        world.size * FloodItWorld.CELLSIZE, 1 / 4.0);
  }

  // tests for the FloodItWorld constructor
  void testFloodItWorldConstructor(Tester t) {
    t.checkConstructorException(
        new IllegalArgumentException("Maximum number of colors is 8"), "FloodItWorld",
        4, 9);
    t.checkConstructorException(
        new IllegalArgumentException("Maximum number of colors is 8"), "FloodItWorld",
        4, 10, new Random(3));
    t.checkConstructorException(
        new IllegalArgumentException("Board size must be greater than 1"), "FloodItWorld",
        1, 2);
  }

  // test for the makeBoard() method
  //  here we compare a handmade board to a board made by the makeBoard() method
  void testMakeBoard(Tester t) {
    this.initConditions();

    this.nonRandomWorld3.board = this.nonRandomWorld3.makeBoard();
    this.nonRandomWorld3.connect();

    t.checkExpect(this.nonRandomWorld3.board, this.exampleWorld1.board);
  }

  // tests for the connect() method
  void testConnect(Tester t) {
    this.initConditions();

    this.exampleWorld2.connect();

    t.checkExpect(this.exampleWorld2.board.get(4).right, this.exampleWorld2.board.get(5));
    t.checkExpect(this.exampleWorld2.board.get(9).left, this.exampleWorld2.board.get(8));
    t.checkExpect(this.exampleWorld2.board.get(4).top, this.exampleWorld2.board.get(0));
    t.checkExpect(this.exampleWorld2.board.get(15).top.top.left,
        this.exampleWorld2.board.get(6));
    t.checkExpect(this.exampleWorld2.board.get(15).top.top.left.right,
        this.exampleWorld2.board.get(15).top.top);
    t.checkExpect(this.exampleWorld2.board.get(0).bottom.right.right.left,
        this.exampleWorld2.board.get(4).right);
    t.checkExpect(this.exampleWorld2.board.get(0).left, new EmptyCell());
  }

  // tests for the connectCell() method
  void testConnectCell(Tester t) {
    this.initConditions();

    this.cell1.connectCell(this.cell5, this.cell2, this.cell1, null);
    this.empty.connectCell(this.cell1, this.cell2, this.cell6, this.cell3);

    t.checkExpect(this.cell1.left, this.cell5);
    t.checkExpect(this.cell1.bottom, null);
    t.checkExpect(this.cell1.right, this.cell1);
    t.checkExpect(this.empty.left, null);
  }

  // tests for the randomColor() method
  void testRandomColor(Tester t) {
    this.initConditions();

    t.checkExpect(this.nonRandomWorld1.randomColor(), Color.CYAN);
    t.checkExpect(this.nonRandomWorld1.randomColor(), Color.GREEN);
    t.checkExpect(this.nonRandomWorld1.randomColor(), Color.CYAN);
    t.checkExpect(this.nonRandomWorld1.randomColor(), Color.RED);
    t.checkExpect(this.nonRandomWorld1.randomColor(), Color.CYAN);
  }

  // tests for the draw() method
  void testDraw(Tester t) {
    this.initConditions();

    cell1.draw(emptyScene);
    t.checkExpect(this.emptyScene, this.sceneWithOneCell);

    cell2.draw(this.sceneWithOneCell);
    t.checkExpect(this.sceneWithOneCell, this.sceneWithTwoCell);

    cell4.draw(this.sceneWithTwoCell);
    t.checkExpect(this.sceneWithTwoCell, this.sceneWithThreeCell);
  }

  // tests for flood()
  void testFlood(Tester t) {
    this.initConditions();
    
    t.checkExpect(nonRandomWorld1.board.get(1).flooded, false); // flooded is false
    nonRandomWorld1.board.get(0).color = Color.red; //set color of adjacent cell to diff color
    nonRandomWorld1.board.get(0).flood(); // flood adjacent cell
    t.checkExpect(nonRandomWorld1.board.get(1).flooded, false); // now flooded should stay false
    
    nonRandomWorld1.board.get(0).color = Color.blue; //set color of adjacent cell to same color
    nonRandomWorld1.board.get(0).flood(); // flood adjacent cell
    t.checkExpect(nonRandomWorld1.board.get(1).flooded, true); // now flooded should be true
    
    t.checkExpect(nonRandomWorld1.board.get(1).color, Color.blue); // color before flooding
    nonRandomWorld1.board.get(0).color = Color.red; //set color of adjacent cell diff color
    nonRandomWorld1.board.get(0).flood(); // flood adjacent cell
    t.checkExpect(nonRandomWorld1.board.get(1).color, Color.red); // now color should change
  }
  
  // tests for makeFlooded()
  void testMakeFlooded(Tester t) {
    this.initConditions();
    
    t.checkExpect(this.cell4.color, Color.cyan); // color is originally cyan
    this.cell4.makeFlooded(Color.green); // makeflooded using green color 
    t.checkExpect(this.cell4.color, Color.cyan); // cell4.flooded == false so nothing will happen
    this.cell4.flooded = true; // set flooded to true
    this.cell4.makeFlooded(Color.green); // makeFlooded using green color
    t.checkExpect(this.cell4.color, Color.green); // now the cell's color should be changed
    
    this.initConditions();
    t.checkExpect(this.cell4.flooded, false); // flooded is originally false
    this.cell4.makeFlooded(Color.green); // makeflooded using green color 
    t.checkExpect(this.cell4.flooded, false); // cell4.color == cyan so nothing will happen
    this.cell4.makeFlooded(Color.cyan); // makeFlooded using cyan color
    t.checkExpect(this.cell4.flooded, true); // now the cell's flooded should be true
  }

  // tests for find()
  void testFind(Tester t) {
    this.initConditions();
    t.checkExpect(this.nonRandomWorld1.find(new Posn(40, 50)), this.nonRandomWorld1.board.get(5));
    t.checkExpect(this.nonRandomWorld1.find(new Posn(90, 0)), this.nonRandomWorld1.board.get(2));
    t.checkExpect(this.nonRandomWorld1.find(new Posn(90, 90)), this.nonRandomWorld1.board.get(10));
  }

  //tests for drawInfo()
  void testDrawInfo(Tester t) {
    this.initConditions();

    this.exampleWorld3.drawInfo(emptyScene);
    t.checkExpect(this.emptyScene, this.emptySceneWithText);

    this.endLose.drawInfo(emptyScene);
    t.checkExpect(this.emptyScene, this.emptySceneLose);

    this.endWin.drawInfo(emptyScene2);
    t.checkExpect(this.emptyScene2, this.emptySceneWin);
  }

  // tests for the makeScene() method
  void testMakeScene(Tester t) {
    this.initConditions();

    // tests making the scene of a starting board
    t.checkExpect(this.nonRandomWorld2.makeScene(), this.nonRandomWorld2Scene);

    // tests making the scene when a player loses
    t.checkExpect(endLose.makeScene(), this.nonRandomWorld2SceneLose);
    
    // tests making the scene when a player wins
    t.checkExpect(endWin.makeScene(), this.nonRandomWorld2SceneWin);
  }

  // tests for isWin()
  void testIsWin(Tester t) {
    this.initConditions();

    t.checkExpect(this.nonRandomWorld2.isWin(), false);
    t.checkExpect(this.endWin.isWin(), true);
  }
}