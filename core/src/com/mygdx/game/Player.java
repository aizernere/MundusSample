package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;

public class Player {
    int id;
    float x;
    float z;
    private Decal decal;

    public Player() {
    }

    public Player(int id, float x, float z) {
        this.id = id;
        TextureRegion region = new TextureRegion(new Texture(Gdx.files.internal("map1/tree.png")));
        decal = Decal.newDecal(100, 120, region, true);
    }

    public Decal getDecal() {
        return decal;
    }
}
