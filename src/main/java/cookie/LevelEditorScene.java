package cookie;

import components.Sprite;
import components.SpriteRenderer;
import components.Spritesheet;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;

public class LevelEditorScene extends Scene {

    private GameObject obj1;

    public LevelEditorScene() {

    }

    @Override
    public void init() {

        loadResources();

        this.camera = new Camera(new Vector2f());

        Spritesheet sprites = AssetPool.getSpritesheet("assets/spritesheets/tiles1.png");

        obj1 = new GameObject("object 1", new Transform(new Vector2f(100, 100), new Vector2f(256, 256)), 1);
        obj1.addComponent(new SpriteRenderer(new Vector4f(1, 0, 0, 1)));
        this.addGameObjectToScene(obj1);
        this.activeGameObject = obj1;

    }

    private void loadResources() {

        AssetPool.getShader("assets/shaders/default.glsl");

        AssetPool.addSpritesheet("assets/spritesheets/tiles1.png",
                new Spritesheet(AssetPool.getTexture("assets/spritesheets/tiles1.png"),
                        16, 16, 4, 0));

    }

    @Override
    public void update(float dt) {

        for (GameObject go: this.gameObjects) {
            go.update(dt);
        }

        this.renderer.render();

    }

    @Override
    public void imgui() {
        ImGui.begin("Test window");
        ImGui.text("Some random text");
        ImGui.end();
    }
}
