package com.readboy.bean;


import java.util.Arrays;

public class Line  {

    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Line{" +
                "location=" + location +
                '}';
    }
}
