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
import com.codeandweb.physicseditor.PhysicsShapeCache;

import java.util.HashMap;

public class MainClass extends ApplicationAdapter {
    private static final float STEP_TIME = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private static final float SCALE = 0.01f;

    private TextureAtlas textureAtlas;
    private SpriteBatch batch;
    private final HashMap<String, Sprite> sprites = new HashMap<>();

    private OrthographicCamera cam;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private float accumulator = 0;
    PhysicsShapeCache physicsBodies;
    private Body ground;
    private Body up;
    private Body left;
    private Body right;
    private Body player;
    private float BOUND = 5f;


    @Override
    public void create() {
        batch = new SpriteBatch();
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        cam = new OrthographicCamera(30, 30 * (h / w));

        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();


        textureAtlas = new TextureAtlas("sprites.txt");
        addSprites();

        Box2D.init();
        world = new World(new Vector2(0, 0), true);
        physicsBodies = new PhysicsShapeCache("sprites.xml");

        Body body = physicsBodies.createBody("orange", world, SCALE, SCALE);
        body.setTransform(5f, 5f, (float) 0);

        player = body;

        debugRenderer = new Box2DDebugRenderer();

        create_bounds();
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
        cam.viewportWidth = 30f;
        cam.viewportHeight = 30f * height/width;
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        player.setTransform(cam.viewportWidth/2, cam.viewportHeight/2,0f);
    }

    @Override
    public void render() {
        handleInput();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stepWorld();

        look_at_player();
        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();

        Vector2 position = player.getPosition();
        float degrees = (float) Math.toDegrees(player.getAngle());
        drawSprite("orange", position.x, position.y, degrees);

        batch.end();

        debugRenderer.render(world, cam.combined);
    }

    private void create_bounds() {
        if (ground != null) world.destroyBody(ground);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 1;

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(cam.viewportWidth, cam.viewportHeight/20);
        fixtureDef.shape = shape1;

        ground = world.createBody(bodyDef);
        ground.createFixture(fixtureDef);
        ground.setTransform(cam.viewportWidth/2, -cam.viewportHeight/2, 0);

        up = world.createBody(bodyDef);
        up.createFixture(fixtureDef);
        up.setTransform(cam.viewportWidth/2, cam.viewportHeight*3/2, (float)Math.PI);

        PolygonShape shape2 = new PolygonShape();
        shape2.setAsBox(cam.viewportWidth/20, cam.viewportHeight);
        fixtureDef.shape = shape2;

        left = world.createBody(bodyDef);
        left.createFixture(fixtureDef);
        left.setTransform(-cam.viewportWidth/2, cam.viewportHeight/2, 0);

        right = world.createBody(bodyDef);
        right.createFixture(fixtureDef);
        right.setTransform(cam.viewportWidth*3/2, cam.viewportHeight/2, (float)Math.PI);

        PolygonShape shape3 = new PolygonShape();
        shape3.setAsBox(cam.viewportWidth/20, cam.viewportWidth/20);
        fixtureDef.shape = shape3;
        for (int i = 0; i < 4; i++) {
            Body s1 = world.createBody(bodyDef);
            s1.createFixture(fixtureDef);
            s1.setTransform(cam.viewportWidth*((i < 2)? 1:3) / 4, cam.viewportHeight* ((i % 2 == 0)? 1:3)/ 4, (float) Math.PI / 4);
        }

        shape1.dispose();
        shape2.dispose();
        shape3.dispose();
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
            player.applyLinearImpulse(new Vector2(0,1),player.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            player.applyLinearImpulse(new Vector2(0,-1),player.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.applyLinearImpulse(new Vector2(-1,0),player.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.applyLinearImpulse(new Vector2(1,0),player.getWorldCenter(), true);
        }

    }
    private void look_at_player(){
        float oldX = cam.position.x;
        float oldY = cam.position.y;
        cam.position.x = MathUtils.clamp(oldX, player.getWorldCenter().x - BOUND, player.getWorldCenter().x + BOUND);
        cam.position.y = MathUtils.clamp(oldY, player.getWorldCenter().y - BOUND, player.getWorldCenter().y + BOUND);
    }


    @Override
    public void dispose() {
        textureAtlas.dispose();
        sprites.clear();
        world.dispose();
        debugRenderer.dispose();
    }
}


