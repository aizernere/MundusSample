package com.mygdx.game;

import java.io.Serializable;

public class PlayerPosition {
    public int id;
    public float x;
    public float z;

    // Default constructor for serialization
    public PlayerPosition() {}

    public PlayerPosition(int id, float x, float z) {
        this.id = id;
        this.x = x;
        this.z = z;
    }
}
