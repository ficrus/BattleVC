package com.ficrus.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.codeandweb.physicseditor.PhysicsShapeCache;

import java.util.HashMap;
import java.util.Random;

public class MainClass extends ApplicationAdapter {
    private static int WORLD_WIDTH = 16;
    private static int WORLD_HEIGHT = 8;
    private static final float STEP_TIME = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private static final float SCALE = 0.01f;
    private static final int COUNT = 10;

    private TextureAtlas textureAtlas;
    private SpriteBatch batch;
    private final HashMap<String, Sprite> sprites = new HashMap<>();

    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private float accumulator = 0;
    PhysicsShapeCache physicsBodies;
    private Body[] bodies = new Body[1];

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(10, 10, camera);

        textureAtlas = new TextureAtlas("sprites.txt");
        addSprites();

        Box2D.init();
        world = new World(new Vector2(0, 0), true);
        physicsBodies = new PhysicsShapeCache("sprites.xml");

        String[] object_names = new String[]{"crate"};

        Random random = new Random();



        Body body = physicsBodies.createBody(object_names[0], world, SCALE, SCALE);
        body.setTransform(5f, 5f, (float) 0);
        bodies[0] = body;

        debugRenderer = new Box2DDebugRenderer();
    }

    private void addSprites() {
        Array<AtlasRegion> regions = textureAtlas.getRegions();

        for (AtlasRegion region : regions) {
            Sprite sprite = textureAtlas.createSprite(region.name);

            float width = sprite.getWidth() * SCALE;
            float height = sprite.getHeight() * SCALE;

            sprite.setSize(width, height);
            sprite.setOrigin(0, 0);

            sprites.put(region.name, sprite);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

        batch.setProjectionMatrix(camera.combined);

        bodies[0].setTransform(camera.viewportWidth/2, camera.viewportHeight/2,0f);
    }

    @Override
    public void render() {
        handleInput();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stepWorld();

        batch.begin();

        for (Body body : bodies) {
            Vector2 position = body.getPosition();
            float degrees = (float) Math.toDegrees(body.getAngle());
            drawSprite("crate", position.x, position.y, degrees);
        }

        batch.end();


        debugRenderer.render(world, camera.combined);
    }

    private void stepWorld() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += Math.min(delta, 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;
            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }

    private void drawSprite(String name, float x, float y, float degrees) {
        Sprite sprite = sprites.get(name);
        sprite.setPosition(x, y);
        sprite.setRotation(degrees);
        sprite.draw(batch);
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            bodies[0].applyLinearImpulse(new Vector2(0,1),bodies[0].getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            bodies[0].applyLinearImpulse(new Vector2(0,-1),bodies[0].getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bodies[0].applyLinearImpulse(new Vector2(-1,0),bodies[0].getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bodies[0].applyLinearImpulse(new Vector2(1,0),bodies[0].getWorldCenter(), true);
        }

    }

    @Override
    public void dispose() {
        textureAtlas.dispose();
        sprites.clear();
        world.dispose();
        debugRenderer.dispose();
    }
}


