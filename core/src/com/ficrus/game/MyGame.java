package com.ficrus.game;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.codeandweb.physicseditor.PhysicsShapeCache;

import java.util.HashMap;
import java.util.Random;

public class MyGame extends ApplicationAdapter implements InputProcessor {
    private static final float STEP_TIME = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    static final int WORLD_WIDTH = 100;
    static final int WORLD_HEIGHT = 100;
    private SpriteBatch batch;
    private OrthographicCamera cam;
    Box2DDebugRenderer debugRenderer;
    private World world;
    private float accumulator = 0;
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


        Box2D.init();
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        create_bounds();
        create_player();
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public boolean keyDown(int keycode){
        if (keycode == Input.Keys.A){
            player.setLinearVelocity(-5,0);
        }
        if (keycode == Input.Keys.D){
            player.setLinearVelocity(5,0);
        }
        if (keycode == Input.Keys.W){
            player.setLinearVelocity(0,5);
        }
        if (keycode == Input.Keys.S){
            player.setLinearVelocity(0,-5);
        }
        return true;
    }
    @Override
    public boolean	keyTyped(char character){
        return true;
    }
    @Override
    public boolean	keyUp(int keycode){
        return true;
    }
    @Override
    public boolean	mouseMoved(int screenX, int screenY){
        return true;
    }
    public boolean	scrolled(int amount){
        return true;
    }
    public boolean	touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }
    public boolean	touchDragged(int screenX, int screenY, int pointer){
        return true;
    }
    public boolean	touchUp(int screenX, int screenY, int pointer, int button){
        return true;
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = 30f;
        cam.viewportHeight = 30f * height/width;
        cam.update();


    }

    private void create_player(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(cam.viewportWidth/2, cam.viewportHeight/2);

        player = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        Fixture fixture = player.createFixture(fixtureDef);

        circle.dispose();
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


    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stepWorld();

        look_at_player();
        cam.update();
        /*batch.setProjectionMatrix(cam.combined);
        batch.begin();

        batch.end();*/

        debugRenderer.render(world, cam.combined);
    }

    private void look_at_player(){
        float oldX = cam.position.x;
        float oldY = cam.position.y;
        cam.position.x = MathUtils.clamp(oldX, player.getPosition().x - BOUND, player.getPosition().x + BOUND);
        cam.position.y = MathUtils.clamp(oldY, player.getPosition().y - BOUND, player.getPosition().y + BOUND);
    }

    private void stepWorld() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += Math.min(delta, 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;
            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }


    @Override
    public void dispose() {
        world.dispose();
    }
}