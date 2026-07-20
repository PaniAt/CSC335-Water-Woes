/**
 * Primary class for the program.
 * Manages the graphics, flow model, pipe grid, etc.
 * 
 * @author Atreya Pandit
 * @version 20/07/2026
 */

// Graphics and GUI
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class PipeNetwork extends JFrame implements ActionListener, MouseListener
{
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
    final String[] EDIT_DIRECTIONS = {"UP", "RIGHT", "DOWN", "LEFT"};
    int editDirection = 0; // Index of EDIT_DIRECTIONS
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
        createMenuBars(); // Make menu bars
        addMouseListener(this); // For mouse events

        this.pack();
        this.toFront();
        this.setVisible(true);

        // Sample pipe layout
        addPipe(2, 1, pipe(false, "SOURCE", "DOWN"));
        addPipe(2, 2, pipe(false, "TWO", "DOWN"));
        addPipe(2, 3, pipe(false, "THREE", "RIGHT"));
        addPipe(3, 3, pipe(false, "CORNER", "UP"));
        addPipe(3, 2, pipe(false, "CORNER", "DOWN"));
        addPipe(4, 2, pipe(false, "SINK", "LEFT"));
        addPipe(2, 4, pipe(false, "TWO", "DOWN"));
        addPipe(2, 5, pipe(false, "SINK", "UP"));

        // Make sure it works
        updateConnections();
        repaint();

        // Don't instantly exit
        while (true);
    }

    /**
     * An alias/shortcut for constructing pipes, simply calls the default Pipe
     * constructor with the given parameters
     * 
     * @param water - Whether the pipe has water
     * @param type - What type the pipe is, valid types:
     *                   TWO, CORNER, THREE, FOUR, SINK, SOURCE
     * @param direction - What direction the pipe faces, valid directions:
     *                        UP, RIGHT, DOWN, LEFT
     * 
     * @return The new constructed pipe object
     */
    public static Pipe pipe(boolean water, String type, String direction)
    {
        return new Pipe(water, type, direction);
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

    /**
     * Returns the amount of pipes in the pipe network, namely the number of
     * non-null tiles in the tile map
     * 
     * @return Pipe count
     */
    public int countPipes()
    {
        int count = 0;
        for (int y = 0; y < TILE_ROWS; ++y)
        {
            for (int x = 0; x < TILE_COLS; ++x)
            {
                if (tileList[y][x] != null)
                {
                    ++count;
                }
            }
        }
        return count;
    }

    /**
     * Updates the flow model by 1 step. <br></br>Every pipe with water will
     * fill every connecting pipe with water if that pipe is not already full
     */
    public void updateFlow()
    {
        // Calculate them changes...
        for (int y = 0; y < TILE_ROWS; ++y)
        {
            // Nobody move.
            for (int x = 0; x < TILE_COLS; ++x)
            {
                // There's blood on the floor.
                Pipe current = tileList[y][x];
                Pipe.flow(current);
            }
        }

        // Update water in pipes
        for (int y = 0; y < TILE_ROWS; ++y)
        {
            for (int x = 0; x < TILE_COLS; ++x)
            {
                Pipe current = tileList[y][x];
                if (current != null)
                {
                    current.setWater( // Update water value
                        current.getWater() | current.getWillHaveWater()
                    );
                    // Do not preserve this
                    current.setWillHaveWater(false);
                }
            }
        }
    }

    /**
     * Drains the pipe at the specified coordinates, provieded that the pipe is
     * not a source or null
     * 
     * @param x - The x coordinate
     * @param y - The y coordinate
     */
    public void drainPipe(int x, int y)
    {
        if (x < 0 || x >= TILE_COLS || y < 0 || y >= TILE_ROWS)
        {
            // null, do nothing
        }
        else if (tileList[y][x] == null)
        {
            // also null, do nothing
        }
        else if (tileList[y][x].getType().equals("SOURCE"))
        {
            // can't unfill sources
        }
        else
        {
            tileList[y][x].setWater(false);
        }
    }

    /**
     * Drains the entire pipe network, except for sources
     */
    public void drainAll()
    {
        for (int y = 0; y < TILE_ROWS; ++y)
        {
            for (int x = 0; x < TILE_COLS; ++x)
            {
                if (tileList[y][x] == null)
                {
                    // Nothing
                }
                else if (tileList[y][x].getType().equals("SOURCE"))
                {
                    // Can't unfill sources
                }
                else
                {
                    tileList[y][x].setWater(false);
                }
            }
        }
    }

    /**
     * Automatically updates all of the connections between pipes, making sure
     * that every connection is valid and reciprocal
     */
    public void updateConnections()
    {
        // Initial "approximation" for connections
        for (int y = 0; y < TILE_ROWS; ++y)
        {
            for (int x = 0; x < TILE_COLS; ++x)
            {
                Pipe current = tileList[y][x];
                if (current == null)
                {
                    // pass
                }
                else
                {
                    int trans = current.getTrans();
                    current.disconnect("UP");
                    current.disconnect("RIGHT");
                    current.disconnect("DOWN");
                    current.disconnect("LEFT");
                    // What the actual hell?
                    if ((trans & 0xFF000000) != 0 && y - 1 >= 0)
                    {
                        current.setConnect("UP", tileList[y - 1][x]);
                    }
                    if ((trans & 0x00FF0000) != 0 && x + 1 < TILE_COLS)
                    {
                        current.setConnect("RIGHT", tileList[y][x + 1]);
                    }
                    if ((trans & 0x0000FF00) != 0 && y + 1 < TILE_ROWS)
                    {
                        current.setConnect("DOWN", tileList[y + 1][x]);
                    }
                    if ((trans & 0x000000FF) != 0 && x - 1 >= 0)
                    {
                        current.setConnect("LEFT", tileList[y][x - 1]);
                    }
                }
            }
        }

        // Ensure every connection is reciprocal
        final String[] DIRECTIONS = {"UP", "RIGHT", "DOWN", "LEFT"};
        for (int y = 0; y < TILE_ROWS; ++y)
        {
            for (int x = 0; x < TILE_COLS; ++x)
            {
                Pipe current = tileList[y][x];
                if (current == null)
                {
                    // Nope! (your too late i already died)
                }
                else
                {
                    for (String dir: DIRECTIONS)
                    {
                        if (current.getConnect(dir) == null)
                        {
                            // Well don't do stupid null stuff idiot
                        }
                        else if (current.getConnect(dir).connectsTo(current))
                        {
                            // I should've never been given the "gift" of life
                        }
                        else
                        {
                            current.disconnect(dir);
                        }
                    }
                }
            }
        }
    }

    /**
     * Visualises the state of the PipeNetwork
     * 
     * @param g - Pre-defined Graphics
     */
    public void paint(Graphics g)
    {
        // Initialise
        if (firstPaint)
        {
            super.paint(g);
            firstPaint = false;
        }
        if (offScreenImage == null)
        {
            offScreenImage = new BufferedImage(
                getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D ctx = (Graphics2D) offScreenImage.getGraphics();
        ctx.translate(OFFSETX, OFFSETY);
        ctx.setColor(new Color(200, 200, 200));
        ctx.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        paintTiles(ctx);

        // Pipe selector menu
        if (pipeSelector)
        {
            paintSelector(ctx);
        }

        // Draw image from buffer
        g.drawImage(offScreenImage, 0, 0, null);
    }

    /**
     * Draws out the tileList
     * 
     * @param ctx - Supplied Graphics2D from the paint() function
     */
    public void paintTiles(Graphics2D ctx)
    {
        Image img;
        final int SPACER = (showGrid ? 1 : 0);
        for (int y = 0; y < TILE_ROWS; ++y)
        {
            for (int x = 0; x < TILE_COLS; ++x)
            {
                ctx.setColor(Color.BLACK);
                ctx.fillRect(
                    x * TILE_WIDTH, y * TILE_HEIGHT,
                    TILE_WIDTH - SPACER, TILE_HEIGHT - SPACER
                );
                
                if (tileList[y][x] != null)
                {
                    img = tileList[y][x].getImage(TILE_WIDTH, TILE_HEIGHT);

                    // Unholy matrix math go!
                    double
                        theta  = tileList[y][x].getAngle(),
                        sin    = Math.sin(theta),
                        cos    = Math.cos(theta),
                        vsh    = 0.5 * (1.0 - cos), // Versine halfed
                        sh     = 0.5 * sin;         // Sine halfed
                    
                    double[] m = new double[]{
                        +cos, +sin,
                        -sin, +cos,
                        TILE_WIDTH * (x + vsh) + (TILE_HEIGHT * sh),
                        TILE_HEIGHT * (y + vsh) - (TILE_WIDTH * sh),
                    };

                    AffineTransform trans = new AffineTransform(m);
                    
                    // Unholy matrix math complete, render
                    ctx.drawImage(img, trans, null);
                }
            }
        }
    }

    /**
     * Draws out the selector menu
     * 
     * @param ctx - Supplied Graphics2D from the paint() function
     */
    public void paintSelector(Graphics2D ctx)
    {
        final int HALF_WIDTH = SCREEN_WIDTH / 2;
        final int HALF_HEIGHT = SCREEN_HEIGHT / 2;
        final int GUI_ROWS = GUI.length;
        final int GUI_COLS = GUI[0].length;
        
        Image img;
        ctx.setColor(new Color(200, 200, 200));
        ctx.fillRect(HALF_WIDTH, 0, HALF_WIDTH, SCREEN_HEIGHT);

        final int GUI_TILE_WIDTH = (HALF_WIDTH / 2) / GUI_COLS;
        final int GUI_TILE_HEIGHT =
            GUI_TILE_WIDTH * (SCREEN_WIDTH / SCREEN_HEIGHT);
        ctx.translate(HALF_WIDTH + GUI_TILE_WIDTH, HALF_HEIGHT);

        // Information menu
        ctx.setColor(Color.BLACK);
        String[] lines = {
            "-- Statistics --",
            "Model Dimensions: " + TILE_COLS + "x" + TILE_ROWS,
            "Total Squares: " + TILE_ROWS * TILE_COLS,
            "Total Pipes: " + countPipes(),
            "",
            "-- How to use --",
            "Left Click to place pipes",
            "Right Click to delete pipes",
            "Alt + Right Click to drain pipes",
            "X to rotate placement right",
            "Z to rotate placement left",
            "S to open pipe selector",
            "F to step model by 1 frame",
            "G to toggle the model grid",
        };
        for (int i = 0; i < lines.length; ++i)
        {
            ctx.drawString(
                lines[i], 0, (HALF_HEIGHT * -15 / 16) + (i * 16)
            );
        }


        // Unholy matrix math go!
        final double
            theta = Math.PI / 2.0 * editDirection,
            sin = Math.sin(theta),
            cos = Math.cos(theta),
            vsh = 0.5 * (1.0 - cos),
            sh = 0.5 * sin;
        
        for (int y = 0; y < GUI_ROWS; ++y)
        {
            for (int x = 0; x < GUI_COLS; ++x)
            {
                ctx.setColor(new Color(165, 165, 165));
                ctx.fillRect(
                    x * GUI_TILE_WIDTH, y * GUI_TILE_HEIGHT,
                    GUI_TILE_WIDTH - 1, GUI_TILE_HEIGHT - 1
                );
                img = GUI[y][x].getImage(GUI_TILE_WIDTH, GUI_TILE_HEIGHT);

                double[] m = new double[]{
                    +cos, +sin,
                    -sin, +cos,
                    GUI_TILE_WIDTH * (x + vsh) + (GUI_TILE_HEIGHT * sh),
                    GUI_TILE_HEIGHT * (y + vsh) - (GUI_TILE_WIDTH * sh),
                };
                AffineTransform trans = new AffineTransform(m);

                ctx.drawImage(img, trans, null);
            }
        }

        ctx.translate(-HALF_WIDTH, -HALF_HEIGHT);
    }

    /**
     * Invoked when the mouse button has been clicked, pressed and released, on
     * a component
     * 
     * @param evt - The event to be processed
     */
    public void mouseClicked(MouseEvent evt) {}

    /**
     * Invoked when a mouse button has been pressed on a component
     * 
     * @param evt - The event to be processed
     */
    public void mousePressed(MouseEvent evt)
    {
        if (evt.getButton() == MouseEvent.BUTTON1)
        {
            mouseDown = true;
        }
    }

    /**
     * Invoked when a mouse button has been released on a component
     * 
     * @param evt - The event to be processed
     */
    public void mouseReleased(MouseEvent evt)
    {
        int x = evt.getX() - OFFSETX,
            y = evt.getY() - OFFSETY;
        if (evt.getButton() == MouseEvent.BUTTON1 && mouseDown)
        {
            mouseDown = false;
            
            if (pipeSelector)
            {
                x -= SCREEN_WIDTH / 2 + (SCREEN_WIDTH / 4) / GUI[0].length;
                y -= SCREEN_HEIGHT / 2;
                x /= (SCREEN_WIDTH / 4) / GUI[0].length;
                y /= (SCREEN_WIDTH / 4) / GUI[0].length * (SCREEN_WIDTH / SCREEN_HEIGHT);
                if (x >= 0 && x < GUI[0].length && y >= 0 && y < GUI.length)
                {
                    editType = GUI[y][x].getType();
                }
            }
            else
            {
                x /= TILE_WIDTH;
                y /= TILE_HEIGHT;
                if (editType.equals("SOURCE"))
                {
                    addPipe(x, y, true, editType, EDIT_DIRECTIONS[editDirection]);
                }
                else
                {
                    addPipe(x, y, false, editType, EDIT_DIRECTIONS[editDirection]);
                }
                updateConnections();
            }
            repaint();
        }
        else if (evt.getButton() == MouseEvent.BUTTON3)
        {
            x /= TILE_WIDTH;
            y /= TILE_HEIGHT;

            // This is some evil horrible code. Not to mention how badly coded
            // it is, it also is bad UI design. Anyways, it suffices for now.
            if (evt.isAltDown())
            {
                drainPipe(x, y);
            }
            else
            {
                removePipe(x, y);
            }
            updateConnections();
            repaint();
        }
    }

    /**
     * Invoked when the mouse enters a component
     * 
     * @param evt - The event to be processed
     */
    public void mouseEntered(MouseEvent evt) {}

    /**
     * Invoked when the mouse exits a component
     * 
     * @param evt - The event to be processed
     */
    public void mouseExited(MouseEvent evt)  {}

    /**
     * Invoked when an action occurs
     * 
     * @param evt - The event to be processed
     */
    public void actionPerformed(ActionEvent evt)
    {
        String cmd = evt.getActionCommand();
        switch (cmd)
        {
            case "Quit":
                System.exit(0);
                break;
            case "Reset":
                tileList = new Pipe[TILE_ROWS][TILE_COLS];
                repaint();
                break;
            case "Step 1 Frame":
                updateFlow();
                repaint();
                break;
            case "Drain":
                drainAll();
                repaint();
                break;
            case "Toggle Grid":
                showGrid = !showGrid;
                repaint();
                break;
            case "Pipe Selector":
                pipeSelector = !pipeSelector;
                repaint();
                break;
            case "Rotate Right":
                editDirection = ++editDirection % 4;
                repaint();
                break;
            case "Rotate Left":
                --editDirection;
                if (editDirection < 0)
                {
                    editDirection = 3;
                }
                repaint();
                break;
            default:
                System.out.printf(
                    "Invalid action detected. Recieved: \"%s\"",
                    cmd
                );
        }
    }

    public void createMenuBars()
    {
        // Define the menu bar
        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        // The Model menu
        menu = new JMenu("Model");
        menuBar.add(menu);
        menu.getPopupMenu().setLightWeightPopupEnabled(false); // Fix rendering

        // Model : Quit
        menuItem = new JMenuItem("Quit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Model : Step 1 Frame
        menuItem = new JMenuItem("Step 1 Frame");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('f'));
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Edit : Drain
        menuItem = new JMenuItem("Drain");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // The Interface menu
        menu = new JMenu("Interface");
        menuBar.add(menu);
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        // Interface : Toggle Grid
        menuItem = new JMenuItem("Toggle Grid");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('g'));
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // The Edit menu
        menu = new JMenu("Edit");
        menuBar.add(menu);
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        // Edit : Reset
        menuItem = new JMenuItem("Reset");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Edit : Pipe Selector
        menuItem = new JMenuItem("Pipe Selector");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('s'));
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Edit : Rotate Right
        menuItem = new JMenuItem("Rotate Right");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('x'));
        menuItem.addActionListener(this);
        menu.add(menuItem);
        // Edit : Rotate Left
        menuItem = new JMenuItem("Rotate Left");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('z'));
        menuItem.addActionListener(this);
        menu.add(menuItem);
    }

    public static void main(String[] args)
    {
        PipeNetwork program = new PipeNetwork();
        program.dispose();
        return;
    }
}