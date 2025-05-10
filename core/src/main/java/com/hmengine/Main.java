package com.hmengine;

import com.hmengine.geometry.Geometry;
import com.hmengine.geometry.Mesh;
import static org.lwjgl.opengl.GL11.*;

public class Main {
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private Window window;
    private Shader shader;
    private Renderer renderer;
    private Scene scene;
    private Camera camera;
    private float rotation = 0.0f;

    // FPS相关变量
    private long lastFrameTime;
    private int frameCount;
    private float fps;
    private static final float FPS_UPDATE_INTERVAL = 0.5f; // 每秒更新一次FPS
    private float timeSinceLastFPSUpdate = 0.0f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // 创建并初始化窗口
        window = new Window(WIDTH, HEIGHT, "渲染测试", true);
        window.init();

        // 创建着色器程序
        shader = new Shader("shaders/basic.vert", "shaders/basic.frag");

        float aspectRatio = (float) WIDTH / (float) HEIGHT;
        // 创建相机
        camera = new Camera(aspectRatio);
        camera.setZoom(2f);

        // 创建渲染器
        renderer = new Renderer(shader, camera);

        // 创建场景
        scene = new Scene();

        int n = 32768;
        for (int i = 0; i < n; i++) {
            Mesh mesh = Geometry.createTriangle();
            float x = (float) Math.cos(i * 2 * Math.PI / (float) n) * 0.3f;
            float y = (float) Math.sin(i * 2 * Math.PI / (float) n) * 0.3f;
            mesh.setPosition(x, y, 0.0f);
            mesh.setScale(0.1f, 0.1f, 1.0f);
            scene.addMesh(mesh);
        }

        // 设置场景
        renderer.setScene(scene);

        // 初始化FPS计时器
        lastFrameTime = System.nanoTime();
        frameCount = 0;
        fps = 0.0f;
    }

    private void loop() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        while (!window.shouldClose()) {
            // 计算帧时间
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
            lastFrameTime = currentTime;

            // 更新FPS计数
            frameCount++;
            timeSinceLastFPSUpdate += deltaTime;

            if (timeSinceLastFPSUpdate >= FPS_UPDATE_INTERVAL) {
                fps = frameCount / timeSinceLastFPSUpdate;
                frameCount = 0;
                timeSinceLastFPSUpdate = 0.0f;
                System.out.printf("FPS: %.1f%n", fps);
            }

            glClear(GL_COLOR_BUFFER_BIT);

            // 更新旋转
            rotation += 0.001f;
            for (int i = 0; i < scene.getMeshes().size(); i++) {
                Mesh mesh = scene.getMeshes().get(i);
                mesh.setRotation(0.0f, 0.0f, -rotation + i * 0.2f);
                mesh.setPosition((float) Math.sin(rotation*10+i) * 0.5f, (float) Math.cos(rotation*10+i) * 0.5f, 0f);
            }

            renderer.render();
            window.update();
        }
    }

    private void cleanup() {
        shader.cleanup();
        window.cleanup();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
