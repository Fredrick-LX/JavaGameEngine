import com.hmengine.Camera;
import com.hmengine.Renderer;
import com.hmengine.Scene;
import com.hmengine.Shader;
import com.hmengine.Window;
import com.hmengine.geometry.Geometry;
import com.hmengine.geometry.Mesh;
import com.hmengine.text.TextRenderer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;

public class Main {

    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private Window window;
    private Shader shader;
    private Renderer renderer;
    private Scene scene;
    private Camera camera;
    private TextRenderer textRenderer;

    private float rotation = 0.0f;
    private long lastToggleTime = 0;

    // 背景颜色设置
    private float[] backgroundColor = { 0.2f, 0.3f, 0.3f, 1.0f };

    // 相机控制参数
    private float cameraMoveSpeed = 0.01f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // 创建并初始化窗口
        window = new Window(WIDTH, HEIGHT, "渲染测试 WSAD控制相机移动 空格键控制网格线显示", true);
        window.init();

        // 创建着色器程序
        shader = new Shader("resources/shaders/basic.vert", "resources/shaders/basic.frag");

        // 创建文本渲染器
        textRenderer = new TextRenderer("resources/fonts/simhei.ttf", 48, WIDTH, HEIGHT, "resources/shaders/text.vert", "resources/shaders/text.frag");

        float aspectRatio = (float) WIDTH / (float) HEIGHT;
        // 创建相机
        camera = new Camera(aspectRatio);
        camera.setZoom(1f);
        camera.setPosition(0f, 0f, 0f);
        camera.setRotation(1.3f, 0.0f, 0.0f);

        // 创建渲染器
        renderer = new Renderer(shader, camera);

        // 创建场景
        scene = new Scene();

        // 极限测试104万个三角形
        // int n = 1048576;
        int n = 60;
        for (int i = 0; i < n; i++) {
            Mesh mesh = Geometry.createHexagon();
            float x = (float) Math.cos(i * 2 * Math.PI / (float) n) * 0.3f;
            float y = (float) Math.sin(i * 2 * Math.PI / (float) n) * 0.3f;
            mesh.setPosition(x, y, 0.0f);
            mesh.setScale(0.1f, 0.1f, 1.0f);

            scene.addMesh(mesh);
        }
                
        // 设置场景
        renderer.setScene(scene);
    }

    private void loop() {
        while (!window.shouldClose()) {
            // 设置背景颜色
            glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
            glClear(GL_COLOR_BUFFER_BIT);

            // 更新旋转
            rotation += 0.0005f;
            for (int i = 0; i < scene.getMeshes().size(); i++) {
                Mesh mesh = scene.getMeshes().get(i);
                mesh.setRotation(0.0f, 0.0f, 0.0f);
                mesh.setPosition((float) Math.sin(rotation * 10 + i) * 0.5f, (float) Math.cos(rotation * 10 + i) * 0.5f,
                        0.0f);

                float hue = (float) (i + rotation) % scene.getMeshes().size();
                mesh.setColor(
                        (float) Math.sin(hue * Math.PI / 2f * (float) (scene.getMeshes().size() + 0.52f)) * 0.5f + 0.5f,
                        (float) Math.sin(hue * Math.PI / 2f * (float) (scene.getMeshes().size() + 1.04f)) * 0.5f + 0.5f,
                        (float) Math.sin(hue * Math.PI / 2f * (float) (scene.getMeshes().size() + 1.57f)) * 0.5f + 0.5f,
                        1.0f);
            }
            textRenderer.renderText("Hello,World!", 100, 100, 1.0f, new float[] { 1.0f, 0.0f, 0.0f });
            // 相机控制
            handleCameraControl();

            renderer.render();
            window.update();
        }
    }

    /**
     * 使用方向键控制相机，空格键控制网格线显示
     */
    private void handleCameraControl() {
        if (window.isKeyPressed(GLFW_KEY_W) || window.isKeyPressed(GLFW_KEY_UP)) {
            camera.move(0, cameraMoveSpeed, 0);
        }
        if (window.isKeyPressed(GLFW_KEY_S) || window.isKeyPressed(GLFW_KEY_DOWN)) {
            camera.move(0, -cameraMoveSpeed, 0);
        }
        if (window.isKeyPressed(GLFW_KEY_A) || window.isKeyPressed(GLFW_KEY_LEFT)) {
            camera.move(-cameraMoveSpeed, 0, 0);
        }
        if (window.isKeyPressed(GLFW_KEY_D) || window.isKeyPressed(GLFW_KEY_RIGHT)) {
            camera.move(cameraMoveSpeed, 0, 0);
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            //防抖
            if (System.currentTimeMillis() - lastToggleTime > 200) {
                lastToggleTime = System.currentTimeMillis();
                renderer.setShowGridLines(!renderer.isShowGridLines());
            }
        }
    }

    // 设置背景颜色
    public void setBackgroundColor(float r, float g, float b, float a) {
        backgroundColor[0] = r;
        backgroundColor[1] = g;
        backgroundColor[2] = b;
        backgroundColor[3] = a;
    }

    private void cleanup() {
        shader.cleanup();
        window.cleanup();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
