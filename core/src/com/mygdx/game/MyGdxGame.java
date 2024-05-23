package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.terrain.Terrain;
import com.mbrlabs.mundus.runtime.Mundus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyGdxGame extends ApplicationAdapter {
	private Mundus mundus;
	private Scene scene;

	private FirstPersonCameraController controller;
	private Array<Decal> mapDecals = new Array<>();

	//players
	private List<PlayerPosition> otherPlayerPositions;
	private PlayerPosition myPosition;



	private DecalBatch decalBatch;

	private Decal astronaut;
	private boolean isJumping;
	private float totalTime = 0;

	//server
	private Client client;


	public MyGdxGame(int id, float x, float z) {
		this.myPosition = new PlayerPosition(id,x,z);
	}

	@Override
	public void create () {
		otherPlayerPositions = new ArrayList<>();
		mundus = new Mundus(Gdx.files.internal("mundus"));
		scene = mundus.loadScene("Main Scene.mundus");
		client = new Client();
		client.start();

		Kryo kryo = client.getKryo();
		kryo.register(PlayerPosition.class);

		try {
			client.connect(5000, "localhost", 54555, 54777);
		} catch (IOException e) {
			Gdx.app.log("GameClient", "Unable to connect to server: " + e.getMessage());
			Gdx.app.exit();
		}
		scene.cam.position.set(myPosition.x, 150, myPosition.z);

		TextureRegion region = new TextureRegion(new Texture(Gdx.files.internal("map1/tree.png")));
		astronaut = Decal.newDecal(100, 120, region, true);



		controller = new FirstPersonCameraController(scene.cam);
		controller.setVelocity(100f);
		Gdx.input.setInputProcessor(controller);
		client.addListener(new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				if (object instanceof PlayerPosition) {
					PlayerPosition position = (PlayerPosition) object;
					updatePlayerPosition(position);
				}
			}
		});


//		Map myMap = new Map();
//		Terrain terrain = mundus.getAssetManager().getTerrainAssets().get(0).getTerrain();
//		mapDecals = myMap.loadMap(terrain);
//
		decalBatch = new DecalBatch(new CameraGroupStrategy(scene.cam));
	}

	private void updatePlayerPosition(PlayerPosition position) {
		boolean updated = false;
		for (PlayerPosition pos : otherPlayerPositions) {
			if (pos.id == position.id) {
				pos.x = position.x;
				pos.z = position.z;
				updated = true;
				break;
			}
		}
		if (!updated) {
			otherPlayerPositions.add(position);
		}
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
		handleInput();
//		for (Decal decal : mapDecals) {
//			decalBatch.add(decal);
//			Color color = getColor(decal);
//			decal.setColor(color);
//		}
//
//		for (Decal decal : mapDecals) {
//			DecalHelper.faceCameraPerpendicularToGround(decal, scene.cam);
//		}
		float time = Gdx.graphics.getDeltaTime();


		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			isJumping = true;
		}

		for (PlayerPosition position : otherPlayerPositions) {
			if(position.id!= myPosition.id){
				decalBatch.add(astronaut);
				float otherHeight = terrain.getHeightAtWorldCoord(position.x,position.z, new Matrix4());
				astronaut.setPosition(position.x,otherHeight,position.z);
			}

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

	private void handleInput() {
		// Send updated position to server

		myPosition.x = scene.cam.position.x;
		myPosition.z = scene.cam.position.z;
		client.sendTCP(myPosition);
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
