package com.lunazstudios.terraward.area;

import java.util.HashMap;
import java.util.Map;

public class ProtectedArea {
    private String name;
    private int x1, y1, z1;
    private int x2, y2, z2;
    private int priority;
    private Map<String, Boolean> flags;

    /**
     * Create a new ProtectedArea instance.
     * @param name The name of the area.
     *             This name is used to identify the area in the config file.
     * @param x1 The x-coordinate of the first corner of the area.
     *           This corner is used to define the bounding box of the area.
     * @param y1 The y-coordinate of the first corner of the area.
     *           This corner is used to define the bounding box of the area.
     * @param z1 The z-coordinate of the first corner of the area.
     *           This corner is used to define the bounding box of the area.
     * @param x2 The x-coordinate of the second corner of the area.
     *           This corner is used to define the bounding box of the area.
     * @param y2 The y-coordinate of the second corner of the area.
     *           This corner is used to define the bounding box of the area.
     * @param z2 The z-coordinate of the second corner of the area.
     *           This corner is used to define the bounding box of the area.
     * @param priority The priority of the area.
     *                 Areas with higher priority values take precedence over areas with lower priority values.
     *                 The default priority value is 0.
     */
    public ProtectedArea (String name, int x1, int y1, int z1, int x2, int y2, int z2, int priority) {
        this.name = name;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.priority = priority;
        this.flags = new HashMap<>();
        initializeDefaultFlags();
    }

    /**
     * Initialize the default flags for the area.
     * By default, all flags are set to false.
     */
    private void initializeDefaultFlags() {
        flags.put("no-break", false);
        flags.put("no-place", false);
        flags.put("no-pvp", false);
        flags.put("no-damage", false);
        flags.put("no-falldamage", false);
        flags.put("no-hunger", false);
        flags.put("no-breakentities", false);
        flags.put("no-openchest", false);
        flags.put("no-rightclick", false);
    }

    /**
     * Set the state of a flag for the area.
     * @param flag The name of the flag to set.
     *             The flag name is case-sensitive.
     * @param value The value to set the flag to.
     *              If true, the flag is set to true.
     *              If false, the flag is set to false.
     */
    public void setFlag(String flag, boolean value) {
        flags.put(flag, value);
    }

    /**
     * Get the state of a flag for the area.
     * @param flag The name of the flag to get.
     *             The flag name is case-sensitive.
     */
    public boolean getFlagState(String flag) {
        return flags.getOrDefault(flag, false);
    }

    /**
     * Get the name of the area.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the x-coordinate of the first corner of the area.
     */
    public int getX1() {
        return x1;
    }

    /**
     * Get the y-coordinate of the first corner of the area.
     */
    public int getY1() {
        return y1;
    }

    /**
     * Get the z-coordinate of the first corner of the area.
     */
    public int getZ1() {
        return z1;
    }

    /**
     * Get the x-coordinate of the second corner of the area.
     */
    public int getX2() {
        return x2;
    }

    /**
     * Get the y-coordinate of the second corner of the area.
     */
    public int getY2() {
        return y2;
    }

    /**
     * Get the z-coordinate of the second corner of the area.
     */
    public int getZ2() {
        return z2;
    }

    /**
     * Check if the area contains the given location.
     * @param x The x-coordinate of the location.
     * @param y The y-coordinate of the location.
     * @param z The z-coordinate of the location.
     */
    public boolean contains(int x, int y, int z) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2) &&
                y >= Math.min(y1, y2) && y <= Math.max(y1, y2) &&
                z >= Math.min(z1, z2) && z <= Math.max(z1, z2);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
