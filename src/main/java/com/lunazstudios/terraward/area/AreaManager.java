package com.lunazstudios.terraward.area;

import com.lunazstudios.terraward.config.ModConfig;

import java.util.Collection;

public class AreaManager {

    public void addArea(ProtectedArea area) {
        ModConfig config = ModConfig.getInstance();
        config.addArea(area);
    }

    public void setPriority(String areaName, int priority) {
        ModConfig config = ModConfig.getInstance();
        ProtectedArea area = config.getArea(areaName);
        if (area != null) {
            area.setPriority(priority);
            config.save();
        }
    }

    public boolean removeArea(String name) {
        ModConfig config = ModConfig.getInstance();
        if (config.getArea(name) != null) {
            config.removeArea(name);
            return true;
        }
        return false;
    }

    public ProtectedArea getArea(String name) {
        ModConfig config = ModConfig.getInstance();
        return config.getArea(name);
    }

    public Collection<ProtectedArea> getAllAreas() {
        ModConfig config = ModConfig.getInstance();
        return config.getAllAreas();
    }
}
