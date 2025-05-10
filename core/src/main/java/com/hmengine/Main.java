package com.hmengine;

import com.hmengine.geometry.Geometry;
import com.hmengine.geometry.Mesh;
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

    private float rotation = 0.0f;

    // 背景颜色设置
    private float[] backgroundColor = { 0.2f, 0.3f, 0.3f, 1.0f };

    // FPS相关变量
    private long lastFrameTime;
    private int frameCount;
    private float fps;
    private static final float FPS_UPDATE_INTERVAL = 0.5f;
    private float timeSinceLastFPSUpdate = 0.0f;

    // 帧率限制相关变量
    private static final int TARGET_FPS = 120;
    private static final long TARGET_FRAME_TIME = 1_000_000_000 / TARGET_FPS; // 纳秒
    private long lastFrameEndTime;

    // 相机控制参数
    private float cameraMoveSpeed = 0.01f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // 创建并初始化窗口
        window = new Window(WIDTH, HEIGHT, "渲染测试 WSAD控制相机移动", true);
        window.init();

        // 创建着色器程序
        shader = new Shader("shaders/basic.vert", "shaders/basic.frag");

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

            // 为每个六边形设置固定的颜色
            /*
             * float hue = (float) i;
             * mesh.setColor(
             * (float) Math.sin(hue * Math.PI / 2f * (float) (n + 0.52f)) * 0.5f + 0.5f,
             * (float) Math.sin(hue * Math.PI / 2f * (float) (n + 1.04f)) * 0.5f + 0.5f,
             * (float) Math.sin(hue * Math.PI / 2f * (float) (n + 1.57f)) * 0.5f + 0.5f,
             * 1.0f);
             */

            scene.addMesh(mesh);
        }

        // 设置场景
        renderer.setScene(scene);

        // 初始化FPS计时器
        lastFrameTime = System.nanoTime();
        lastFrameEndTime = lastFrameTime;
        frameCount = 0;
        fps = 0.0f;
    }

    private void loop() {
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

            // 相机控制
            handleCameraControl();

            renderer.render();
            window.update();

            // 帧率限制
            long frameEndTime = System.nanoTime();
            long frameTime = frameEndTime - lastFrameEndTime;
            if (frameTime < TARGET_FRAME_TIME) {
                try {
                    Thread.sleep((TARGET_FRAME_TIME - frameTime) / 1_000_000); // 转换为毫秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastFrameEndTime = System.nanoTime();
        }
    }

    /**
     * 使用方向键控制相机
     */
    private void handleCameraControl() {
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.move(0, cameraMoveSpeed, 0);
        }
        if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.move(0, -cameraMoveSpeed, 0);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.move(-cameraMoveSpeed, 0, 0);
        }
        if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.move(cameraMoveSpeed, 0, 0);
        }
        /*
         * if (window.isKeyPressed(GLFW_KEY_LEFT)) {
         * camera.rotate(0, 0, cameraRotateSpeed);
         * }
         * if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
         * camera.rotate(0, 0, -cameraRotateSpeed);
         * }
         */
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
