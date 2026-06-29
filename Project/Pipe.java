/**
 * Class for all pipes. Pipes can:
 * - Store water
 * - Connect to four pipes: up, right, down, left.
 * - Rotate in four directions: up, right, down, left
 * - Have connections in the following ways:
 *      - Two-way Straight
 *      - Two-way Corner
 *      - Three-way Junction
 *      - Four-way Junction
 * - Can be a source, pumps water into the system
 * - Can be a water sink, end location for water
 * 
 * @author Atreya Pandit
 * @version 25/06/2026
 */

// Graphics
import javax.swing.ImageIcon;
import java.awt.Image;

public class Pipe
{
    /** Types of pipe connections */
    public enum Type
    {
        /** Two-way straight pipe */
        TWO,
        /** Two-way corner pipe */
        CORNER,
        /** Three-way junction pipe */
        THREE,
        /** Four-way junction pipe */
        FOUR,
        /** Water sink */
        SINK,
        /** Water source */
        SOURCE,
    }
    /** Valid directions for pipe rotation */
    public enum Direction
    {
        /** Default pipe rotation */
        UP,
        /** Rotation of 90 degrees */
        RIGHT,
        /** Rotation of 180 degrees */
        DOWN,
        /** Rotation of 270 (or -90) degrees */
        LEFT,
    }

    /*
     * Instance variables
     */
    /** Connected pipe in the relevant direction */
    private Pipe  upConnect, rightConnect, downConnect, leftConnect;
    /** Does the pipe contain water */
    private boolean hasWater;
    /** Will the pipe contain water upon next flow */
    private boolean willHaveWater = false;
    /** The type of this pipe */
    private Type type;
    /** The direction of this pipe */
    private Direction dir;

    /**
     * Constructor for new Pipe objects
     * 
     * @param water - Whether the pipe has water
     * @param pipeType - What type the pipe is, valid types:
     *                   TWO, CORNER, THREE, FOUR, SINK, SOURCE
     * @param pipeDirection - What direction the pipe faces, valid directions:
     *                        UP, RIGHT, DOWN, LEFT
     */
    public Pipe(boolean water, String pipeType, String pipeDirection)
    {
        this.hasWater = water;
        this.type = Type.valueOf(pipeType.toUpperCase());
        this.dir = Direction.valueOf(pipeDirection.toUpperCase());
    }

    /*
     * Setters and getters for private fields
     */

    /**
     * Sets whether this pipe has water
     * @param water - Will the pipe have water
     */
    public void setWater(boolean water)
        { this.hasWater = water; }
    /**
     * Gets whether this pipe has water
     * @return Water value
     */
    public boolean getWater()
        { return this.hasWater; }

    /**
     * Sets whether this pipe will have water upon next flow
     * @param water - Will the pipe have water
     */
    public void setWillHaveWater(boolean water)
        { this.willHaveWater = water; }
    /**
     * Gets whether this pipe will have water upon next flow
     * @return Will have water value
     */
    public boolean getWillHaveWater()
        { return this.willHaveWater; }

    /**
     * Sets what type of pipe this pipe is
     * @param pipeType - New type of pipe, valid values: 
     *                   TWO, CORNER, THREE, FOUR, SINK, SOURCE
     */
    public void setType(String pipeType)
        { this.type = Type.valueOf(pipeType.toUpperCase()); }
    /**
     * Gets waht type of pipe this pipe is
     * @return String value of pipe's type
     */
    public String getType()
        { return String.valueOf(this.type); }

    /**
     * Sets this pipe's connection in a given direction
     * @param direction - What direction to set in, valid directions:
     *                    UP, RIGHT, DOWN, LEFT
     * @param pipe - Pipe to connect
     */
    public void setConnect(String direction, Pipe pipe)
    {
        switch (direction.toUpperCase())
        {
            case "UP":
                this.upConnect = pipe;
                break;
            case "RIGHT":
                this.rightConnect = pipe;
                break;
            case "DOWN":
                this.downConnect = pipe;
                break;
            case "LEFT":
                this.leftConnect = pipe;
                break;
            default:
                throw new Error("Invalid pipe direction: " + direction);
        }
    }

    /**
     * Disconnects the pipe from any other pipe in the given direction, this
     * effectively sets the connection in that direction to null
     * 
     * @param direction - What direction to disconnect in, valid directions:
     *                    UP, RIGHT, DOWN, LEFT
     */
    public void disconnect(String direction)
    {
        this.setConnect(direction, null);
    }

    /**
     * Gets this pipe's connection in a given direction
     * 
     * @param direction - What direction to get in, valid values:
     *                    UP, RIGHT, DOWN, LEFT
     * 
     * @return Pipe connected in the given direction
     */
    public Pipe getConnect(String direction)
    {
        switch (direction.toUpperCase())
        {
            case "UP":
                return this.upConnect;
            case "RIGHT":
                return this.rightConnect;
            case "DOWN":
                return this.downConnect;
            case "LEFT":
                return this.leftConnect;
            default:
                throw new Error("Invalid pipe direction: " + direction);
        }
    }
    /**
     * Gets whether the pipe has a connection in the given direction
     * 
     * @param direction - What direction to check in, valid value:
     *                    UP, RIGHT, DOWN, LEFT
     * 
     * @return Is there a pipe connected in the given direction
     */
    public boolean hasConnect(String direction)
    {
        switch (direction.toUpperCase())
        {
            case "UP":
                return (this.upConnect != null);
            case "RIGHT":
                return (this.rightConnect != null);
            case "DOWN":
                return (this.downConnect != null);
            case "LEFT":
                return (this.leftConnect != null);
            default:
                throw new Error("Invalid pipe direction: " + direction);
        }
    }

    /**
     * Checks whether this pipe connects to another given pipe, checks for
     * exact equality, so cloned pipes will return false
     * 
     * @param pipe - The pipe to check
     * 
     * @return Whether the given pipe is connected in any direction
     */
    public boolean connectsTo(Pipe pipe)
    {
        for (Pipe p: this.getAllConnections())
        {
            if (p == pipe) // Check for EXACT object equality
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts how many pipes this pipe connects to
     * 
     * @return Count of non-null connections
     */
    public int countConnections()
    {
        int count = 0;
        if (this.upConnect != null) { ++count; }
        if (this.rightConnect != null) { ++count; }
        if (this.downConnect != null) { ++count; }
        if (this.leftConnect != null) { ++count; }
        return count;
    }

    /**
     * Gets all of the non-null pipes this pipe connects to. This will be
     * ordered by: UP, RIGHT, DOWN, LEFT
     * 
     * @return Array of non-null pipes
     */
    public Pipe[] getAllConnections()
    {
        Pipe[] out = new Pipe[this.countConnections()];
        int i = 0;

        if (this.upConnect != null) out[++i] = this.upConnect;
        if (this.rightConnect != null) out[++i] = this.rightConnect;
        if (this.downConnect != null) out[++i] = this.downConnect;
        if (this.leftConnect != null) out[++i] = this.leftConnect;

        return out;
    }

    /**
     * Attempts to flow water from the given pipe into its connecting pipes,
     * this is run in a static context. It is safe to pass null into this
     * function
     * 
     * @param pipe - The pipe to flow from
     */
    public static void flow(Pipe pipe)
    {
        boolean canFlow = false;
        int connections = 0;
        if (pipe == null)
        {
            canFlow = false;
        }
        else if (pipe.getType().equals("SINK"))
        {
            canFlow = false;
        }
        else
        {
            canFlow = pipe.getWater();
            connections = pipe.countConnections();
        }
        final Pipe[] ALL_CONNECT = pipe.getAllConnections();
        for (int i = 0; i < connections && canFlow; ++i)
        {
            Pipe connect = ALL_CONNECT[i];
            assert (connect != null);
            connect.setWillHaveWater(true);
        }
    }

    /**
     * Gets visual icon that should represent this pipe
     * 
     * @param width - Image width
     * @param height - Image height
     * 
     * @return Pipe's Image
     */
    public Image getImage(int width, int height)
    {
        ImageIcon img;
        int order = 0;
        switch (this.type)
        {
            case TWO:
                order = 3;
                break;
            case CORNER:
                order = 5;
                break;
            case THREE:
                order = 7;
                break;
            case FOUR:
                order = 9;
                break;
            case SINK:
                order = 1;
                break;
            case SOURCE:
                order = -1;
                break;
            default:
                break;
        }
        int fill = this.hasWater ? 1 : 0;

        int number = order + fill;
        if (number < 10)
        {
            img = new ImageIcon("./PipeLight/pipe_0" + number + ".png");
        }
        else
        {
            img = new ImageIcon("./PipeLight/pipe_" + number + ".png");
        }

        img.setImage(
            img.getImage().getScaledInstance(width, height, Image.SCALE_FAST)
        );

        return img.getImage();
    }

    /**
     * Gets the transformation of this pipe, this is represented as a 32-bit
     * integer where the bits represent booleans expressing whether it can or
     * can not connect in that direction
     * 
     * @return 32-bit integer containing translation
     */
    public int getTrans()
    {
        int trans = 0;

        // This is some evil trickery that shouldn't be allowed
        switch (this.type)
        {
            case TWO:
                trans = 0xFF00FF00;
                break;
            case CORNER:
                trans = 0xFF0000FF;
                break;
            case THREE:
                trans = 0xFFFF00FF;
                break;
            case FOUR:
                trans = 0xFFFFFFFF;
                return trans;
            case SOURCE:
            case SINK:
                trans = 0xFF000000;
                break;
            default:
                trans = 0x00000000;
                break;
        }

        switch (this.dir)
        {
            case UP:
                return trans; // 0 degrees is no change, returns
            case RIGHT:
                trans = (trans >>> 8) | (trans << 24); // Turn 90 degrees
                break;
            case DOWN:
                trans = (trans >>> 16)| (trans << 16); // Flip 180 degrees
                break;
            case LEFT:
                trans = (trans >>> 24) | (trans << 8); // Turn -90 degrees
                break;
            default:
                return trans; // Invalid, returns
        }

        return trans;
    }

    /**
     * Returns the angle at which this pipe's icon should be oriented
     * 
     * @return Angle, in radians
     */
    public double getAngle()
    {
        switch (this.dir)
        {
            case UP:
                return 0.0;
            case RIGHT:
                return Math.toRadians(90.0);
            case DOWN:
                return Math.toRadians(180.0);
            case LEFT:
                return Math.toRadians(-90.0);
            default:
                return 0.0;
        }
    }
}