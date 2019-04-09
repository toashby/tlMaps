package com.toashby.tlmap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.json.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MapClass extends ApplicationAdapter implements GestureDetector.GestureListener {
	private OrthographicCamera camera;
	private Viewport viewport;
	SpriteBatch batch;
	Texture img;
	Texture m1train;
	private ShapeRenderer renderer;
	private int dragX, dragY;
	float currentZoom = 1;
	BitmapFont font;

	ArrayList<Integer> trackupdateno = new ArrayList<Integer>();
	ArrayList<Integer> southtrackupdateno = new ArrayList<Integer>();

	//m1 upd
	ArrayList<Integer> m1trackupdateno = new ArrayList<Integer>();
	ArrayList<Integer> m1westtrackupdateno = new ArrayList<Integer>();


	ArrayList<String> m2tracks = new ArrayList<String>();
	Stations[] m2Stations = new Stations[14];
	Stations[] m2SouthStations = new Stations[14];

	ArrayList<String> m1tracks = new ArrayList<String>();
	Stations[] m1Stations = new Stations[15];
	Stations[] m1WestStations = new Stations[15];

	boolean checkingSouth = false;

	boolean checkingWest = false;



	int stationNo = 0;
	int southstationNo = 0;

	//m1
	int eastStationNo = 0;
	int westStationNo = 0;

	private float elapsedTime;
	private float refreshTimer = 30;
	private float refreshSouthTimer = 90;
	private float refreshM1Timer = 0;
	private float refreshM1WestTimer = 60;

	static int trackPiece;

	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		//beautiful text!!
		font = new BitmapFont();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans-Bold.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 55;
		parameter.characters = "0123456789£qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM:^/-+~";
		font = generator.generateFont(parameter);
		font.setColor(Color.DARK_GRAY);
		generator.dispose();

		camera = new OrthographicCamera(100, 100);
//		JSONObject obj = new JSONObject(" ... ");
		batch = new SpriteBatch();
		m1train = new Texture("m1train.png");

		//m2
		for(int i = 0; i < 14; i++){
			m2Stations[i] = new Stations();
			m2Stations[i].setTimeToDepart(-1);
			m2Stations[i].setTimeToArrive(0);
			m2Stations[i].setNorth(true);
			m2SouthStations[i] = new Stations();
			m2SouthStations[i].setTimeToDepart(-1);
			m2SouthStations[i].setTimeToArrive(0);
			m2SouthStations[i].setNorth(false);
		}

		//m1
		for(int i = 0; i < 15; i++){
			m1Stations[i] = new Stations();
			m1Stations[i].setTimeToDepart(-1);
			m1Stations[i].setTimeToArrive(0);
			m1Stations[i].setNorth(true);
			m1WestStations[i] = new Stations();
			m1WestStations[i].setTimeToDepart(-1);
			m1WestStations[i].setTimeToArrive(0);
			m1WestStations[i].setNorth(false);
		}

		//m2
		for(int i = 0; i < 14; i++){
			trackupdateno.add(i);
			southtrackupdateno.add(i);
		}

		//m1
		for(int i = 0; i < 15; i++){
			m1trackupdateno.add(i);
			m1westtrackupdateno.add(i);
		}

		m2tracks.add("Ouchy-Olympique");
		m2tracks.add("lausanne,jordils");
		m2tracks.add("lausanne,delices");
		m2tracks.add("lausanne,grancy");
		m2tracks.add("lausanne,gare");
		m2tracks.add("Lausanne-Flon");
		m2tracks.add("riponne-m-bejart");
		m2tracks.add("lausanne,bessieres");
		m2tracks.add("lausanne,ours");
		m2tracks.add("lausanne,CHUV");
		m2tracks.add("lausanne,sallaz");
		m2tracks.add("lausanne,fourmi");
		m2tracks.add("lausanne,vennes");
		m2tracks.add("lausanne,epalinges,croisettes");

		//m1 stations east to west
		m1tracks.add("Lausanne-Flon");
		m1tracks.add("lausanne,vigie");
		m1tracks.add("lausanne,montelly");
		m1tracks.add("lausanne,provence");
		m1tracks.add("lausanne,malley");
		m1tracks.add("Lausanne,bourdonnette");
		m1tracks.add("UNIL-Dorigny");
		m1tracks.add("UNIL-Mouline");
		m1tracks.add("UNIL-Sorge");
		m1tracks.add("EPFL");
		m1tracks.add("Ecublens,VD,Bassenges");
		m1tracks.add("Ecublens,VD,Cerisaie");
		m1tracks.add("Chavannes-pr\\u00e8s-Renens,Crochy");
		m1tracks.add("Ecublens,VD,Epenex");
		m1tracks.add("Renens,VD,gare");


		for(trackPiece = 0; trackPiece < 13; trackPiece++) {
			m2Stations[trackPiece].sendRequest("GET", m2tracks.get(trackPiece), m2tracks.get(trackPiece + 1));
		}
		for(trackPiece = 13; trackPiece > 0; trackPiece--) {
			m2SouthStations[trackPiece].sendRequest("GET", m2tracks.get(trackPiece), m2tracks.get(trackPiece - 1));
		}

		for(trackPiece = 0; trackPiece < 14; trackPiece++) {
			m1Stations[trackPiece].sendRequest("GET", m1tracks.get(trackPiece), m1tracks.get(trackPiece + 1));
		}
		for(trackPiece = 14; trackPiece > 0; trackPiece--) {
			m1WestStations[trackPiece].sendRequest("GET", m1tracks.get(trackPiece), m1tracks.get(trackPiece - 1));
		}


		renderer = new ShapeRenderer();

		camera.setToOrtho(false,w,h);
		camera.translate(662600, 4650700);
		camera.update();

		//Gdx.input.setInputProcessor(this);
		Gdx.input.setInputProcessor(new GestureDetector(this));
		viewport = new ScreenViewport(camera);

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();

		//a new day every 1 seconds
		elapsedTime += Gdx.graphics.getDeltaTime();
		if (elapsedTime > 1) {
			elapsedTime = elapsedTime - 1;

			//m2
			for (int i = 0; i < m2Stations.length; i++) {
				if (m2Stations[i].getTimeToDepart() != -1) {
					if (m2Stations[i].getTimeToDepart() > 0) {
						m2Stations[i].setTimeToDepart(m2Stations[i].getTimeToDepart() - 1);

					} else {
						if (m2Stations[i].getTimeToArrive() > 0) {
							m2Stations[i].setTimeToArrive(m2Stations[i].getTimeToArrive() - 1);
							m2Stations[i].setXpos(m2Stations[i].getXpos() + m2Stations[i].getXdifference() / m2Stations[i].getTimeStep());
							m2Stations[i].setYpos(m2Stations[i].getYpos() + m2Stations[i].getYdifference() / m2Stations[i].getTimeStep());
						}
					}
				}
				if (m2SouthStations[i].getTimeToDepart() != -1) {
					if (m2SouthStations[i].getTimeToDepart() > 0) {
						m2SouthStations[i].setTimeToDepart(m2SouthStations[i].getTimeToDepart() - 1);

					} else {
						if (m2SouthStations[i].getTimeToArrive() > 0) {
							m2SouthStations[i].setTimeToArrive(m2SouthStations[i].getTimeToArrive() - 1);
							m2SouthStations[i].setXpos(m2SouthStations[i].getXpos() + m2SouthStations[i].getXdifference() / m2SouthStations[i].getTimeStep());
							m2SouthStations[i].setYpos(m2SouthStations[i].getYpos() + m2SouthStations[i].getYdifference() / m2SouthStations[i].getTimeStep());
						}
					}
				}
			}

			//m1
			for (int i = 0; i < m1Stations.length; i++) {
				if (m1Stations[i].getTimeToDepart() != -1) {
					if (m1Stations[i].getTimeToDepart() > 0) {
						m1Stations[i].setTimeToDepart(m1Stations[i].getTimeToDepart() - 1);

					} else {
						if (m1Stations[i].getTimeToArrive() > 0) {
							m1Stations[i].setTimeToArrive(m1Stations[i].getTimeToArrive() - 1);
							m1Stations[i].setXpos(m1Stations[i].getXpos() + m1Stations[i].getXdifference() / m1Stations[i].getTimeStep());
							m1Stations[i].setYpos(m1Stations[i].getYpos() + m1Stations[i].getYdifference() / m1Stations[i].getTimeStep());
						}
					}
				}
				if (m1WestStations[i].getTimeToDepart() != -1) {
					if (m1WestStations[i].getTimeToDepart() > 0) {
						m1WestStations[i].setTimeToDepart(m1WestStations[i].getTimeToDepart() - 1);

					} else {
						if (m1WestStations[i].getTimeToArrive() > 0) {
							m1WestStations[i].setTimeToArrive(m1WestStations[i].getTimeToArrive() - 1);
							m1WestStations[i].setXpos(m1WestStations[i].getXpos() + m1WestStations[i].getXdifference() / m1WestStations[i].getTimeStep());
							m1WestStations[i].setYpos(m1WestStations[i].getYpos() + m1WestStations[i].getYdifference() / m1WestStations[i].getTimeStep());
						}
					}
				}
			}

		}

		//requestTimer
		refreshTimer += Gdx.graphics.getDeltaTime();
		refreshSouthTimer += Gdx.graphics.getDeltaTime();
		if (refreshTimer > 120) {
			System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRR");
			refreshTimer = refreshTimer - 120;

			stationNo = 0;
			trackupdateno.clear();
			checkingSouth = false;

			for (trackPiece = 0; trackPiece < 13; trackPiece++) {
					//System.out.println("onetrue");
					if (m2Stations[trackPiece].getTimeToDepart() < 1 && m2Stations[trackPiece].getTimeToArrive() < 1 || m2Stations[trackPiece].getTimeToDepart() < 0) {
						trackupdateno.add(trackPiece);
						m2Stations[trackPiece].sendRequest("GET", m2tracks.get(trackPiece), m2tracks.get(trackPiece + 1));
					}

			}
		}

		if (refreshSouthTimer > 120) {
			System.out.println("SouthRRRRRRRRRRRRRRRRRRRRRRRRR");
			refreshSouthTimer = refreshSouthTimer - 120;

			southstationNo = 0;
			trackupdateno.clear();
			checkingSouth = true;

			for (trackPiece = 1; trackPiece < 13; trackPiece++) {
				//if (m2SouthStations[trackPiece].getIsTrain() == true) {
					if (m2SouthStations[trackPiece].getTimeToDepart() < 1 && m2SouthStations[trackPiece].getTimeToArrive() < 1 || m2SouthStations[trackPiece].getTimeToDepart() < 0) {
						southtrackupdateno.add(trackPiece);
						m2SouthStations[trackPiece].sendRequest("GET", m2tracks.get(trackPiece), m2tracks.get(trackPiece - 1));
					}
				//}

			}
		}

		//refreshM1Timer
		refreshM1Timer += Gdx.graphics.getDeltaTime();
		if (refreshM1Timer > 120) {
			System.out.println("M11111111111111111");
			refreshM1Timer = refreshM1Timer - 120;

			stationNo = 0;
			m1trackupdateno.clear();
			checkingSouth = false;

			for (trackPiece = 0; trackPiece < 14; trackPiece++) {
				//System.out.println("onetrue");
				if (m1Stations[trackPiece].getTimeToDepart() < 1 && m1Stations[trackPiece].getTimeToArrive() < 1 || m1Stations[trackPiece].getTimeToDepart() < 0) {
					m1trackupdateno.add(trackPiece);
					m1Stations[trackPiece].sendRequest("GET", m1tracks.get(trackPiece), m1tracks.get(trackPiece + 1));
				}
			}
		}

		//refreshM1WestTimer
		refreshM1WestTimer += Gdx.graphics.getDeltaTime();
		if (refreshM1WestTimer > 120) {
			System.out.println("M11111111111111111WEST");
			refreshM1WestTimer = refreshM1WestTimer - 120;

			stationNo = 0;
			m1westtrackupdateno.clear();
			checkingSouth = false;

			for (trackPiece = 1; trackPiece < 14; trackPiece++) {
				if (m1WestStations[trackPiece].getTimeToDepart() < 1 && m1WestStations[trackPiece].getTimeToArrive() < 1 || m1WestStations[trackPiece].getTimeToDepart() < 0) {
					m1westtrackupdateno.add(trackPiece);
					m1WestStations[trackPiece].sendRequest("GET", m1tracks.get(trackPiece), m1tracks.get(trackPiece - 1));
				}
			}
		}

			batch.setProjectionMatrix(camera.combined);

			renderer.setProjectionMatrix(camera.combined);
			renderer.begin(ShapeType.Filled);
			renderer.setColor(1f, 0, 0.4f, 1);
			//renderer.line(100, 100, 500, 500);

			//thick line from ouchy to jordils
			renderer.rectLine(6.626641f * 100000, 46.507473f * 100000, (6.627432f * 100000), (46.509927f * 100000), 25);
			renderer.rectLine((6.627432f * 100000), (46.509927f * 100000), (6.628043f * 100000), (46.512022f * 100000), 25);
			renderer.rectLine((6.628043f * 100000), (46.512022f * 100000), (6.62887f * 100000), (46.514925f * 100000), 25);
			renderer.rectLine((6.62887f * 100000), (46.514925f * 100000), (6.62967f * 100000), (46.517595f * 100000), 25);
			renderer.rectLine((6.62967f * 100000), (46.517595f * 100000), (6.630345f * 100000), (46.520795f * 100000), 25);
			renderer.rectLine((6.630345f * 100000), (46.520795f * 100000), (6.632664f * 100000), (46.522872f * 100000), 25);
			renderer.rectLine((6.632664f * 100000), (46.522872f * 100000), (6.637078f * 100000), (46.520714f * 100000), 25);
			renderer.rectLine((6.637078f * 100000), (46.520714f * 100000), (6.640673f * 100000), (46.521245f * 100000), 25);
			renderer.rectLine((6.640673f * 100000), (46.521245f * 100000), (6.641491f * 100000), (46.526404f * 100000), 25);
			renderer.rectLine((6.641491f * 100000), (46.526404f * 100000), (6.646193f * 100000), (46.532814f * 100000), 25);
			renderer.rectLine((6.646193f * 100000), (46.532814f * 100000), (6.651056f * 100000), (46.538809f * 100000), 25);
			renderer.rectLine((6.651056f * 100000), (46.538809f * 100000), (6.657618f * 100000), (46.541299f * 100000), 25);
			renderer.rectLine((6.657618f * 100000), (46.541299f * 100000), (6.661465f * 100000), (46.542891f * 100000), 25);
			//renderer.rect(500, 500, 100, 100);
			//renderer.circle(500, 500, 500);

			//FLON TO RENENS
			renderer.rectLine((6.630345f * 100000), (46.520795f * 100000), (6.623926f * 100000), (46.521541f * 100000), 25);
			renderer.rectLine((6.623926f * 100000), (46.521541f * 100000), (6.613562f * 100000), (46.52182f * 100000), 25);
			renderer.rectLine((6.613562f * 100000), (46.52182f * 100000), (6.608105f * 100000), (46.523384f * 100000), 25);
			renderer.rectLine((6.608105f * 100000), (46.523384f * 100000), (6.603305f * 100000), (46.524211f * 100000), 25);
			renderer.rectLine((6.603305f * 100000), (46.524211f * 100000), (6.589803f * 100000), (46.523267f * 100000), 25);
			renderer.rectLine((6.589803f * 100000), (46.523267f * 100000), (6.584706f * 100000), (46.52422f * 100000), 25);
			renderer.rectLine((6.584706f * 100000), (46.52422f * 100000), (6.578899f * 100000), (46.524993f * 100000), 25);
			renderer.rectLine((6.578899f * 100000), (46.524993f * 100000), (6.573578f * 100000), (46.522449f * 100000), 25);
			renderer.rectLine((6.573578f * 100000), (46.522449f * 100000), (6.566144f * 100000), (46.522197f * 100000), 25);
			renderer.rectLine((6.566144f * 100000), (46.522197f * 100000), (6.564588f * 100000), (46.524588f * 100000), 25);
			renderer.rectLine((6.564588f * 100000), (46.524588f * 100000), (6.566548f * 100000), (46.527753f * 100000), 25);
			renderer.rectLine((6.566548f * 100000), (46.527753f * 100000), (6.569973f * 100000), (46.532715f * 100000), 25);
			renderer.rectLine((6.569973f * 100000), (46.532715f * 100000), (6.573578f * 100000), (46.537713f * 100000), 25);
			renderer.rectLine((6.573578f * 100000), (46.537713f * 100000), (6.578072f * 100000), (46.537479f * 100000), 25);

			renderer.end();

			batch.begin();
			//batch.draw(img, 0, 0);
			font.draw(batch, "  Ouchy-Olympique ~ " + (int) (m2Stations[0].getTimeToDepart() / 60f) + " : " + (int) (int) (m2SouthStations[0].getTimeToDepart() /60f)  , (6.626641f * 100000), (46.507473f * 100000));
			font.draw(batch, "  Jordils ~ " + (int) (m2Stations[1].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[1].getTimeToDepart() / 60f) , (6.627432f * 100000), (46.509927f * 100000));
			font.draw(batch, "  Delices ~ " + (int) (m2Stations[2].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[2].getTimeToDepart() / 60f) , (6.628043f * 100000), (46.512022f * 100000));
			font.draw(batch, "  Grancy ~ " + (int) (m2Stations[3].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[3].getTimeToDepart() / 60f) , (6.62887f * 100000), (46.514925f * 100000));
			font.draw(batch, "  Lausanne-Gare ~ " + (int) (m2Stations[4].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[4].getTimeToDepart() / 60f) , (6.62967f * 100000), (46.517595f * 100000));
			font.draw(batch, "  Lausanne-Flon ~ " + (int) (m2Stations[5].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[5].getTimeToDepart() / 60f) , (6.630345f * 100000), (46.520795f * 100000));
			font.draw(batch, "    Riponne M Bejart ~ " + (int) (m2Stations[6].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[6].getTimeToDepart() / 60f) , (6.632664f * 100000), (46.522872f * 100000));
			font.draw(batch, "  Bessieres ~ " + (int) (m2Stations[7].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[7].getTimeToDepart() / 60f) , (6.637078f * 100000), (46.520714f * 100000));
			font.draw(batch, "  Ours ~ " + (int) (m2Stations[8].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[8].getTimeToDepart() / 60f) , (6.640673f * 100000), (46.521245f * 100000));
			font.draw(batch, "  CHUV ~ " + (int) (m2Stations[9].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[9].getTimeToDepart() / 60f) , (6.641491f * 100000), (46.526404f * 100000));
			font.draw(batch, "  Sallaz ~ " + (int) (m2Stations[10].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[10].getTimeToDepart() / 60f) , (6.646193f * 100000), (46.532814f * 100000));
			font.draw(batch, "  Fourmi ~ " + (int) (m2Stations[11].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[11].getTimeToDepart() / 60f) , (6.651056f * 100000), (46.538809f * 100000));
			font.draw(batch, "  Vennes ~ " + (int) (m2Stations[12].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[12].getTimeToDepart() / 60f) , (6.657618f * 100000), (46.541299f * 100000));
			font.draw(batch, "  Croisettes ~ " + (int) (m2Stations[13].getTimeToDepart() / 60f)  + " : " + (int) (int) (m2SouthStations[13].getTimeToDepart() / 60f) , (6.661465f * 100000), (46.542891f * 100000));

			//M!1 WALOOOOO!!!!
			font.draw(batch, "  Vigie ~ " + (int) (m1Stations[1].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[1].getTimeToDepart() /60f)  , (6.623926f * 100000), (46.522541f * 100000));
			font.draw(batch, "  Montelly ~ " + (int) (m1Stations[2].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[2].getTimeToDepart() /60f)  , (6.613562f * 100000), (46.52282f * 100000));
			font.draw(batch, "  Provence ~ " + (int) (m1Stations[3].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[3].getTimeToDepart() /60f)  , (6.608105f * 100000), (46.524384f * 100000));
			font.draw(batch, "  Malley ~ " + (int) (m1Stations[4].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[4].getTimeToDepart() /60f)  , (6.603305f * 100000), (46.525211f * 100000));
			font.draw(batch, "  Bourdonnette ~ " + (int) (m1Stations[5].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[5].getTimeToDepart() /60f)  , (6.589803f * 100000), (46.524267f * 100000));
			font.draw(batch, "  UNIL-Dorigny ~ " + (int) (m1Stations[6].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[6].getTimeToDepart() /60f)  , (6.584706f * 100000), (46.52522f * 100000));
			font.draw(batch, "  Mouline ~ " + (int) (m1Stations[7].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[7].getTimeToDepart() /60f)  , (6.578899f * 100000), (46.525993f * 100000));
			font.draw(batch, "  UNIL-Sorge ~ " + (int) (m1Stations[8].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[8].getTimeToDepart() /60f)  , (6.573578f * 100000), (46.522049f * 100000));
			font.draw(batch, "  EPFL ~ " + (int) (m1Stations[9].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[9].getTimeToDepart() /60f)  , (6.566144f * 100000), (46.521800f * 100000));
			font.draw(batch, "  Bassenges ~ " + (int) (m1Stations[10].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[10].getTimeToDepart() /60f)  , (6.564788f * 100000), (46.524588f * 100000));
			font.draw(batch, "  Cerisaie ~ " + (int) (m1Stations[11].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[11].getTimeToDepart() /60f)  , (6.566548f * 100000), (46.527753f * 100000));
			font.draw(batch, "  Crochy ~ " + (int) (m1Stations[12].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[12].getTimeToDepart() /60f)  , (6.569973f * 100000), (46.532715f * 100000));
			font.draw(batch, "  Epenex ~ " + (int) (m1Stations[13].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[13].getTimeToDepart() /60f)  , (6.573578f * 100000), (46.538713f * 100000));
			font.draw(batch, "  Renens-CFF ~ " + (int) (m1Stations[14].getTimeToDepart() / 60f) + " : " + (int) (int) (m1WestStations[14].getTimeToDepart() /60f)  , (6.578072f * 100000), (46.537479f * 100000));

			try {
				for (int i = 0; i < 13; i++) {
				//	if (m2Stations[i].getTimeToDepart() < 0) {

				//	} else {
						if (m2Stations[i + 1].getTimeToDepart() > 0 && m2Stations[i].getTimeToDepart() < 1) {
							batch.draw(m1train, m2Stations[i].getXpos() * 100000, m2Stations[i].getYpos() * 100000);
						}
				//	}
				}

			} catch (Exception exception) {

			}

			////SOUTHSOUTHSOUTH
			try {
				for (int i = 0; i < 13; i++) {
					//if (m2SouthStations[i].getTimeToDepart() < 0) {

//					} else {
						if (m2SouthStations[i].getTimeToDepart() < 1 && m2SouthStations[i].getTimeToArrive() > 0) {
							batch.draw(m1train, m2SouthStations[i].getXpos() * 100000, m2SouthStations[i].getYpos() * 100000);
						}
//					}
				}

			} catch (Exception exception) {

			}

			//M1 EAST
			try {
			for (int i = 0; i < 14; i++) {
				//	if (m2Stations[i].getTimeToDepart() < 0) {

				//	} else {
				if (m1Stations[i + 1].getTimeToDepart() > 0 && m1Stations[i].getTimeToDepart() < 1) {
					batch.draw(m1train, m1Stations[i].getXpos() * 100000, m1Stations[i].getYpos() * 100000);
				}
				//	}
			}

			} catch (Exception exception) {

			}

			////M1 WEST
			try {
			for (int i = 0; i < 14; i++) {
				//if (m2SouthStations[i].getTimeToDepart() < 0) {

//					} else {
				if (m1WestStations[i].getTimeToDepart() < 1 && m1WestStations[i].getTimeToArrive() > 0) {
					batch.draw(m1train, m1WestStations[i].getXpos() * 100000, m1WestStations[i].getYpos() * 100000);
				}
//					}
			}

			} catch (Exception exception) {

			}

			batch.end();


	}


	@Override
	public void dispose () {
		batch.dispose();
		//img.dispose();  //dispose error was here
	}


	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		//camera.position.add(-deltaX, deltaY, 0f);
		camera.translate(-deltaX * currentZoom,deltaY * currentZoom);
		camera.update();
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		currentZoom = camera.zoom;
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		camera.zoom = (initialDistance / distance) * currentZoom;
		camera.update();
		// Don't go below the map

		return true;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return false;
	}

	@Override
	public void pinchStop() {

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);

	}
}


