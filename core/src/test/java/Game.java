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
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;

public class Game {
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private Window window;
    private Shader shader;
    private Renderer renderer;
    private Scene scene;
    private Camera camera;
    private TextRenderer textRenderer;

    private int score = 0;
    private int highScore = 0;
    private Mesh player;
    private float playerSpeed = 0.01f;
    private boolean gameOver = false;
    private float gameTime = 0f;
    private int level = 1;
    private Random random;

    // 游戏状态
    private static final float COLLISION_DISTANCE = 0.1f;
    private static final int INITIAL_TARGET_COUNT = 5;
    private static final float TARGET_SPAWN_RADIUS = 0.7f; // 目标生成范围
    private static final float LEVEL_TIME = 10f; // 每关时间（秒）

    // 目标列表
    private List<Mesh> targets;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // 初始化随机数生成器
        random = new Random();

        // 初始化窗口
        window = new Window(WIDTH, HEIGHT, "Simple Game", true);
        window.init();

        // 初始化着色器
        shader = new Shader("resources/shaders/basic.vert", "resources/shaders/basic.frag");

        // 初始化文本渲染器
        textRenderer = new TextRenderer("resources/fonts/simhei.ttf", 48, WIDTH, HEIGHT,
                "resources/shaders/text.vert", "resources/shaders/text.frag");

        // 初始化相机
        float aspectRatio = (float) WIDTH / (float) HEIGHT;
        camera = new Camera(aspectRatio);
        camera.setZoom(1f);
        camera.setPosition(0f, 0f, 0f);

        // 初始化渲染器
        renderer = new Renderer(shader, camera);
        renderer.setShowGridLines(false);

        // 初始化场景
        scene = new Scene();
        targets = new ArrayList<>();

        // 创建游戏对象
        createGameObjects();
    }

    private void createGameObjects() {
        // 清除现有对象
        scene.getMeshes().clear();
        targets.clear();

        // 创建玩家（一个六边形）
        player = Geometry.createTriangle();
        player.setPosition(0f, 0f, 0f);
        player.setScale(0.1f, 0.1f, 1.0f);
        player.setColor(0.0f, 1.0f, 1.0f, 1.0f); // 青色
        scene.addMesh(player);

        // 创建目标（小六边形）
        int targetCount = INITIAL_TARGET_COUNT + (level - 1) * 2; // 每关增加2个目标
        for (int i = 0; i < targetCount; i++) {
            Mesh target = Geometry.createHexagon();
            // 在圆形区域内随机生成目标位置
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float radius = random.nextFloat() * TARGET_SPAWN_RADIUS;
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;

            target.setPosition(x, y, 0.0f);
            target.setScale(0.05f, 0.05f, 1.0f);
            target.setColor(0.0f, 1.0f, 0.0f, 1.0f); // 绿色
            scene.addMesh(target);
            targets.add(target);
        }

        renderer.setScene(scene);
    }

    private void loop() {
        while (!window.shouldClose()) {
            // 清除背景
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            // 更新游戏状态
            update();

            // 渲染场景
            renderer.render();

            // 渲染UI文本
            renderUI();

            // 更新窗口
            window.update();
        }
    }

    private void renderUI() {
        // 渲染分数
        textRenderer.renderText("Score: " + score, 10, 40, 0.7f, new float[] { 1.0f, 1.0f, 1.0f });
        textRenderer.renderText("High Score: " + highScore, 10, 70, 0.7f, new float[] { 1.0f, 1.0f, 1.0f });
        textRenderer.renderText("Level: " + level, 10, 100, 0.7f, new float[] { 1.0f, 1.0f, 1.0f });
        textRenderer.renderText("Time: " + String.format("%.1f", LEVEL_TIME - gameTime), 10, 130, 0.7f,
                new float[] { 1.0f, 1.0f, 1.0f });

        if (gameOver) {
            textRenderer.renderText("Game Over! Press R to restart", WIDTH / 2 - 350, HEIGHT / 2, 1f,
                    new float[] { 1.0f, 0.0f, 0.0f });
        }
    }

    private void update() {
        if (gameOver) {
            // 检查是否按下R键重新开始
            if (window.isKeyPressed(GLFW_KEY_R)) {
                resetGame();
            }
            return;
        }

        // 更新游戏时间
        gameTime += 0.016f; // 假设60FPS

        // 检查关卡时间
        if (gameTime >= LEVEL_TIME) {
            gameOver = true; // 设置游戏结束
        }

        // 处理输入
        handleInput();

        // 检测碰撞
        checkCollisions();
    }

    private void handleInput() {
        // 获取玩家当前位置
        float playerX = player.getPosition().x;
        float playerY = player.getPosition().y;

        // 处理键盘输入
        boolean pressedW = window.isKeyPressed(GLFW_KEY_W);
        boolean pressedS = window.isKeyPressed(GLFW_KEY_S);
        boolean pressedA = window.isKeyPressed(GLFW_KEY_A);
        boolean pressedD = window.isKeyPressed(GLFW_KEY_D);

        // 设置旋转角度
        float rotation = player.getRotation().z;
        if (pressedA) {
            rotation += 0.05f; // 向左旋转
        }
        if (pressedD) {
            rotation -= 0.05f; // 向右旋转
        }
        player.setRotation(0, 0, rotation); // 更新玩家旋转

        // 更新位置
        if (pressedW) {
            playerX += Math.cos(rotation + (float) Math.PI / 2f) * playerSpeed; // 前进
            playerY += Math.sin(rotation + (float) Math.PI / 2f) * playerSpeed; // 前进
        }
        if (pressedS) {
            playerX -= Math.cos(rotation + (float) Math.PI / 2f) * playerSpeed; // 后退
            playerY -= Math.sin(rotation + (float) Math.PI / 2f) * playerSpeed; // 后退
        }

        // 限制玩家移动范围
        playerX = Math.max(-1.0f, Math.min(1.0f, playerX));
        playerY = Math.max(-1.0f, Math.min(1.0f, playerY));

        // 更新玩家位置
        player.setPosition(playerX, playerY, 0.0f);
    }

    private void checkCollisions() {
        float playerX = player.getPosition().x;
        float playerY = player.getPosition().y;

        // 使用迭代器来安全地移除元素
        Iterator<Mesh> iterator = targets.iterator();
        while (iterator.hasNext()) {
            Mesh target = iterator.next();
            float targetX = target.getPosition().x;
            float targetY = target.getPosition().y;

            // 计算距离
            float dx = playerX - targetX;
            float dy = playerY - targetY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 检测碰撞
            if (distance < COLLISION_DISTANCE) {
                // 移除目标
                scene.removeMesh(target);
                iterator.remove();
                score += 10 * level; // 分数随关卡增加

                // 检查是否所有目标都被收集
                if (targets.isEmpty()) {
                    nextLevel();
                }
                break;
            }
        }
    }

    private void nextLevel() {
        level++;
        gameTime = 0f;

        // 清除当前目标
        for (Mesh target : targets) {
            scene.removeMesh(target);
        }
        targets.clear();

        // 重置玩家位置
        player.setPosition(0f, 0f, 0f);

        // 创建新的目标
        createGameObjects();
    }

    private void resetGame() {
        // 更新最高分
        if (score > highScore) {
            highScore = score;
        }

        // 重置游戏状态
        score = 0;
        level = 1;
        gameTime = 0f;
        gameOver = false;

        // 清除场景
        scene.getMeshes().clear();
        targets.clear();

        // 重新创建游戏对象
        createGameObjects();
    }

    private void cleanup() {
        renderer.cleanup();
        shader.cleanup();
        textRenderer.cleanup();
        window.cleanup();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.run();
    }
}
