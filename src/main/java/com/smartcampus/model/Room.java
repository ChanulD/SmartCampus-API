package com.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a campus room that contains one or more sensors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Room {

    private String id;
    private String name;
    private String building;
    private int    floor;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Room() {}

    public Room(String id, String name, String building, int floor) {
        this.id       = id;
        this.name     = name;
        this.building = building;
        this.floor    = floor;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId()                  { return id; }
    public void   setId(String id)         { this.id = id; }

    public String getName()                { return name; }
    public void   setName(String name)     { this.name = name; }

    public String getBuilding()                    { return building; }
    public void   setBuilding(String building)     { this.building = building; }

    public int  getFloor()             { return floor; }
    public void setFloor(int floor)    { this.floor = floor; }
}
