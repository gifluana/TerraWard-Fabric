package com.lunazstudios.terraward.area;

import java.util.HashMap;
import java.util.Map;

public class ProtectedArea {
    private String name;
    private int x1, y1, z1;
    private int x2, y2, z2;
    private int priority;
    private Map<String, Boolean> flags;

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

    public void setFlag(String flag, boolean value) {
        flags.put(flag, value);
    }

    public boolean getFlagState(String flag) {
        return flags.getOrDefault(flag, false);
    }

    public String getName() {
        return name;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getZ1() {
        return z1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public int getZ2() {
        return z2;
    }

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
