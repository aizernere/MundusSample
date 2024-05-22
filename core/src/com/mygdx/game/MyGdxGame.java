package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.terrain.Terrain;
import com.mbrlabs.mundus.runtime.Mundus;

public class MyGdxGame extends ApplicationAdapter {
	private Mundus mundus;
	private Scene scene;

	private FirstPersonCameraController controller;
	private Array<Decal> mapDecals = new Array<>();
	private DecalBatch decalBatch;
	private boolean isJumping;
	private float totalTime = 0;

	@Override
	public void create () {
		mundus = new Mundus(Gdx.files.internal("mundus"));
		scene = mundus.loadScene("Main Scene.mundus");

		scene.cam.position.set(230, 150, 190);
		scene.cam.direction.rotate(Vector3.Y, 70);
		scene.cam.direction.rotate(Vector3.Z, -20);

		controller = new FirstPersonCameraController(scene.cam);
		controller.setVelocity(100f);
		Gdx.input.setInputProcessor(controller);
		Map myMap = new Map();
		Terrain terrain = mundus.getAssetManager().getTerrainAssets().get(0).getTerrain();
		mapDecals = myMap.loadMap(terrain);

		decalBatch = new DecalBatch(new CameraGroupStrategy(scene.cam));
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Terrain terrain = mundus.getAssetManager().getTerrainAssets().get(0).getTerrain();

		float height = terrain.getHeightAtWorldCoord(scene.cam.position.x,scene.cam.position.z, new Matrix4());

		controller.update();
		scene.sceneGraph.update();
		scene.render();
		for (Decal decal : mapDecals) {
			decalBatch.add(decal);
			Color color = getColor(decal);
			decal.setColor(color);
		}

		for (Decal decal : mapDecals) {
			DecalHelper.faceCameraPerpendicularToGround(decal, scene.cam);
		}
		float time = Gdx.graphics.getDeltaTime();



		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			isJumping = true;
		}

		if(isJumping){
			totalTime += time;
			if(totalTime >=1){
				isJumping = false;
				totalTime = 0;
			}else{
				scene.cam.position.y = (float) (-(1f/2f)*(9.81f)*Math.pow(totalTime-0.5f,2f)+1f)*10f+height;
//				if(totalTime >=0.9&&scene.cam.position.y<height){
//					isJumping = false;
//					totalTime = 0;
//				}
			}
		}else{
			scene.cam.position.y = height;
		}



		decalBatch.flush();
	}

	private Color getColor(Decal decal) {
		double distance;
		double maxDistance=700;
		Vector3 playerPos = scene.cam.position;
		Vector3 decalPos = decal.getPosition();
		distance = Math.sqrt(
						Math.pow(playerPos.x-decalPos.x,2) +
						Math.pow(playerPos.y-decalPos.y,2) +
						Math.pow(playerPos.z-decalPos.z,2));
		// Calculate blackness factor
		double blackness = distance / maxDistance;
		blackness = Math.max(0, Math.min(blackness, 1)); // Clamp blackness between 0 and 1
		float darkness= (float) (0.5 - blackness);
		if(darkness<0.05){
			darkness = 0.05f;
		}
		float r = darkness;
		float g = darkness;
		float b = darkness;
		float a = 1.0f; // Keep alpha constant
		Color color = new Color(r, g, b, a);
		return color;
	}

	@Override
	public void dispose () {
		mundus.dispose();
	}
}
