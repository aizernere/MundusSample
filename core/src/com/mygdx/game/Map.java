package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.mbrlabs.mundus.commons.terrain.Terrain;

import java.util.Random;

public class Map {
    private final Array<Decal> mapDecals = new Array<>();
    public Array<Decal> loadMap(Terrain terrain) {
        Decal decal;
        float height;
        Random random = new Random();
        int randomX;
        int randomZ;
        TextureRegion ground = new TextureRegion(new Texture(Gdx.files.internal("map1/tree.png")));
        for(int i=0;i<50;i++){
            randomX = random.nextInt(500) + 1;
            randomZ = random.nextInt(500) + 1;
            height = terrain.getHeightAtWorldCoord(randomX, randomZ,new Matrix4())+32f;
            decal = Decal.newDecal(100, 120, ground,true);
            decal.setPosition(randomX,height,randomZ);
            mapDecals.add(decal);
            System.out.println("Tree added.");
        }
//        height = terrain.getHeightAtWorldCoord(45, 50)+12f;
//        decal = Decal.newDecal(30, 20, ground,true);
//        decal.setPosition(45,height,50);

        return mapDecals;
    }
}