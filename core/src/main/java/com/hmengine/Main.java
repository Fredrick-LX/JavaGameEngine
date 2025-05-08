package com.hmengine;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import com.hmengine.map.TerrainGenerator;
import com.hmengine.renderer.Renderer2D;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private static class Tile {
        Vector2f position;
        float size;
        boolean isWall;

        Tile(Vector2f position, float size, boolean isWall) {
            this.position = position;
            this.size = size;
            this.isWall = isWall;
        }
    }

    private long window;
    private Renderer2D renderer;
    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 900;
    private static final int GRID_SIZE = 50;
    private static final int MAP_WIDTH = WINDOW_WIDTH / GRID_SIZE;
    private static final int MAP_HEIGHT = WINDOW_HEIGHT / GRID_SIZE;
    private static final float TILE_SIZE = 2.0f / MAP_WIDTH;
    private List<Tile> tiles;
    private boolean isDragging = false;
    private Vector2f lastMousePosition;
    private Vector2f worldOffset;
    private static final float DRAG_SPEED = 1.0f;
    private TerrainGenerator terrainGenerator;
    private float viewOffsetX = 0.0f;
    private float viewOffsetY = 0.0f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // 初始化GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("无法初始化GLFW");
        }

        // 配置GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // 创建窗口
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "无限地图生成", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("无法创建GLFW窗口");
        }

        // 设置窗口位置
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // 初始化变量
        lastMousePosition = new Vector2f();
        worldOffset = new Vector2f();

        // 设置鼠标回调
        glfwSetCursorPosCallback(window, (_, xpos, ypos) -> {
            if (isDragging) {
                float deltaX = (float) (xpos - lastMousePosition.x) / GRID_SIZE;
                float deltaY = (float) (ypos - lastMousePosition.y) / GRID_SIZE;
                
                // 更新世界偏移
                worldOffset.x -= deltaX * DRAG_SPEED;
                worldOffset.y += deltaY * DRAG_SPEED;
                
                // 更新视图偏移
                viewOffsetX -= deltaX * DRAG_SPEED * TILE_SIZE;
                viewOffsetY += deltaY * DRAG_SPEED * TILE_SIZE;
                
                // 当视图偏移超过一个瓦片大小时，重新生成地图
                if (Math.abs(viewOffsetX) >= TILE_SIZE || Math.abs(viewOffsetY) >= TILE_SIZE) {
                    worldOffset.x = Math.round(worldOffset.x);
                    worldOffset.y = Math.round(worldOffset.y);
                    viewOffsetX = 0;
                    viewOffsetY = 0;
                    updateTiles();
                }
            }
            lastMousePosition.x = (float) xpos;
            lastMousePosition.y = (float) ypos;
        });

        // 设置鼠标按钮回调
        glfwSetMouseButtonCallback(window, (_, button, action, _) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                isDragging = action == GLFW_PRESS;
                if (!isDragging) {
                    // 拖动结束时，确保地图对齐到网格
                    worldOffset.x = Math.round(worldOffset.x);
                    worldOffset.y = Math.round(worldOffset.y);
                    viewOffsetX = 0;
                    viewOffsetY = 0;
                    updateTiles();
                }
            }
        });

        // 设置OpenGL上下文
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // 初始化OpenGL
        GL.createCapabilities();

        // 初始化渲染器和随机数生成器
        renderer = new Renderer2D(WINDOW_WIDTH, WINDOW_HEIGHT);
        tiles = new ArrayList<>();
        terrainGenerator = new TerrainGenerator(114514);

        // 生成初始地图
        updateTiles();
    }

    private void updateTiles() {
        tiles.clear();
        
        // 计算视口范围
        int startX = (int)Math.floor(worldOffset.x);
        int startY = (int)Math.floor(worldOffset.y);
        
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                // 计算世界坐标
                float worldX = x + startX;
                float worldY = y + startY;

                // 生成地形
                boolean isWall = terrainGenerator.isWall(worldX, worldY);

                // 计算渲染位置（考虑偏移）
                float screenX = (float)x / MAP_WIDTH * 2.0f - 1.0f + TILE_SIZE/2 + viewOffsetX;
                float screenY = (float)y / MAP_HEIGHT * 2.0f - 1.0f + TILE_SIZE/2 + viewOffsetY;

                // 添加瓦片
                tiles.add(new Tile(
                    new Vector2f(screenX, screenY),
                    TILE_SIZE,
                    isWall
                ));
            }
        }
    }

    private void loop() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        while (!glfwWindowShouldClose(window)) {
            GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            renderer.begin();
            for (Tile tile : tiles) {
                renderer.submit(
                    tile.position,
                    new Vector2f(tile.size),
                    tile.isWall ? new Vector4f(0.2f, 0.2f, 0.2f, 1.0f) : new Vector4f(0.8f, 0.8f, 0.8f, 1.0f),
                    -1
                );
            }
            renderer.end();

            glfwSwapBuffers(window);
            glfwPollEvents();

            if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                terrainGenerator.reseed();
                updateTiles();
                while (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                    glfwPollEvents();
                }
            }
        }
    }

    private void cleanup() {
        renderer.cleanup();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        new Main().run();
    }
} 