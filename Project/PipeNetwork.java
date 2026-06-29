/**
 * Primary class for the program.
 * Manages the graphics, flow model, pipe grid, etc.
 * 
 * @author Atreya Pandit
 * @version 29/06/2026
 */

// Graphics and GUI
import javax.swing.*;
import java.awt.*;
// import java.awt.event.*; Unused for now
// import java.awt.geom.*; Unused for now
import java.awt.image.BufferedImage;

public class PipeNetwork extends JFrame{
    // Window offsets
    final int OFFSETX =
        // Windows: 08 | MacOS: 00
        (System.getProperty("os.name").equals("Mac OS X") ? 0 : 8);
    final int OFFSETY =
        // Windows: 54 | MacOS: 50
        (System.getProperty("os.name").equals("Mac OS X") ? 50 : 54);
    
    // Tile list dimensions
    final int TILE_COLS = 16; // Cols can be considered X direction
    final int TILE_ROWS = 16; // Rows can be considered Y direction

    // Screen dimensions
    Rectangle screenSize = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .getMaximumWindowBounds();
    
    // The game board should be a perfect square
    int squareSize = (int)
        Math.min(screenSize.getWidth(), screenSize.getHeight()) - OFFSETY * 2;
    
    // Dividing then multiplying rouunds to a factor of the tile array length
    final int SCREEN_WIDTH = (squareSize / TILE_COLS) * TILE_COLS;
    final int SCREEN_HEIGHT = (squareSize / TILE_ROWS) * TILE_ROWS;
    // Tile list
    final int TILE_WIDTH = SCREEN_WIDTH / TILE_COLS;
    final int TILE_HEIGHT = SCREEN_HEIGHT / TILE_ROWS;
    Pipe[][] tileList = new Pipe[TILE_ROWS][TILE_COLS];

    // Interface variables
    boolean showGrid = true;
    boolean debugMode = false;
    BufferedImage offScreenImage;
    boolean firstPaint = true;
    boolean mouseDown = false;

    // MenuBar variables
    JMenuBar menuBar;
    JMenu menu;
    JMenu subMenu;
    JMenuItem menuItem;

    // Model edit variables
    String editType = "TWO";
    String editDirection = "UP";
    boolean pipeSelector = false;
    final Pipe[][] GUI = {
        {new Pipe(false, "TWO", "UP"), new Pipe(false, "CORNER", "UP")},
        {new Pipe(false, "THREE", "UP"), new Pipe(false, "FOUR", "UP")},
        {new Pipe(false, "SINK", "UP"), new Pipe(true, "SOURCE", "UP")},
    };

    // Timing
    int gameTimer = 0;

    /**
     * Constructor for new PipeNetworks
     */
    public PipeNetwork()
    {
        this.setTitle("Pipe Network");
        this.getContentPane()
            .setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.getContentPane().setLayout(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        // createMenuBars(); // Make menu bars
        // addMouseListener(this); // For mouse events

        this.pack();
        this.toFront();
        this.setVisible(true);

        // Sample pipe layout
        addPipe(2, 1, pipe(true, "SOURCE", "DOWN"));
        addPipe(2, 2, pipe(false, "TWO", "DOWN"));
        addPipe(2, 3, pipe(false, "THREE", "RIGHT"));
        addPipe(3, 3, pipe(false, "CORNER", "UP"));
        addPipe(3, 2, pipe(false, "CORNER", "DOWN"));
        addPipe(4, 2, pipe(false, "END", "LEFT"));
        addPipe(2, 4, pipe(false, "TWO", "DOWN"));
        addPipe(2, 5, pipe(false, "SINK", "UP"));

        // Initialise
        // updateConnections();
        // repaint();
    }

    /**
     * An alias/shortcut for constructing pipes, simply calls the default Pipe
     * constructor with the given parameters
     * 
     * @param water - Whether the pipe has water
     * @param pipeType - What type the pipe is, valid types:
     *                   TWO, CORNER, THREE, FOUR, SINK, SOURCE
     * @param pipeDirection - What direction the pipe faces, valid directions:
     *                        UP, RIGHT, DOWN, LEFT
     * 
     * @return The new constructed pipe object
     */
    public static Pipe pipe(boolean water, String pipeType, String pipeDirection)
    {
        return new Pipe(water, pipeType, pipeDirection);
    }

    /**
     * Constructs then adds or sets the pipe at the given coordinates. Will
     * return null if the given coordinates do not exist in the tilemap
     * 
     * @param x - x coordinate of the pipe
     * @param y - y coordinate of the pipe
     * @param water - Whether the pipe has water
     * @param pipeType - What type of pipe is being placed, valid types:
     *                   TWO, CORNER, THREE, FOUR, SINK, SOURCE
     * @param pipeDirection - What direction the pipe faces, valid directions:
     *                        UP, RIGHT, DOWN, LEFT
     * 
     * @return The pipe that was constucted and placed into the tilemap
     */
    public Pipe addPipe(
        int x, int y, boolean water, String pipeType, String pipeDirection)
    {
        if (x < 0 || x >= TILE_COLS || y < 0 || y >= TILE_ROWS)
        {
            return null;
        }

        Pipe pipe;
        if (pipeType == null)
        {
            pipe = null;
        }
        else
        {
            pipe = pipe(water, pipeType, pipeDirection);
        }

        tileList[y][x] = pipe;
        return pipe;
    }

    /**
     * Adds or sets the pipe at the given coordinates. Will do nothing if the
     * coordinates are not within the tilemap
     * 
     * @param x - x coordinate of the pipe
     * @param y - y coordinate of the pipe
     * @param pipe - The pipe to place into the tilemap
     */
    public void addPipe(int x, int y, Pipe pipe)
    {
        if (x < 0 || x >= TILE_COLS || y < 0 || y >= TILE_ROWS)
        {
            return; // Exit
        }

        tileList[y][x] = pipe;
    }

    /**
     * Removes and returns the pipe at the given coordinates. In effect, sets
     * the point at the given coordinates in the tilemap to null. Will return
     * null if no pipe exists at the specified coordinates, or if the
     * coordinates do not lie within the tilemap
     * 
     * @param x - x coordinate of the pipe
     * @param y - y coordinate of the pipe
     * 
     * @return The pipe that was at the given coordinates, or null
     */
    public Pipe removePipe(int x, int y)
    {
        if (x < 0 || x >= TILE_COLS || y < 0 || y >= TILE_ROWS)
        {
            return null;
        }

        Pipe pipe = tileList[y][x];
        tileList[y][x] = null;
        return pipe;
    }
}