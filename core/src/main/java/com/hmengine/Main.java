package com.hmengine;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import com.hmengine.math.perlin;
import com.hmengine.renderer.Renderer2D;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private enum ResourceType {
        NONE(new Vector4f(0.8f, 0.8f, 0.8f, 1.0f)),  // 无资源（普通地面）
        WOOD(new Vector4f(0.6f, 0.4f, 0.2f, 1.0f)),  // 木材（棕色）
        STONE(new Vector4f(0.5f, 0.5f, 0.5f, 1.0f)), // 石头（灰色）
        GOLD(new Vector4f(1.0f, 0.84f, 0.0f, 1.0f)); // 黄金（金色）

        private final Vector4f color;

        ResourceType(Vector4f color) {
            this.color = color;
        }

        public Vector4f getColor() {
            return color;
        }
    }

    private enum BiomeType {
        PLAINS(new Vector4f(0.8f, 0.8f, 0.6f, 1.0f)),    // 平原（浅黄色）
        HILLS(new Vector4f(0.6f, 0.7f, 0.5f, 1.0f)),     // 丘陵（浅绿色）
        PLATEAU(new Vector4f(0.7f, 0.6f, 0.5f, 1.0f)),   // 高原（浅棕色）
        BASIN(new Vector4f(0.5f, 0.6f, 0.7f, 1.0f)),     // 盆地（浅蓝色）
        MOUNTAINS(new Vector4f(0.4f, 0.4f, 0.4f, 1.0f)); // 山脉（深灰色）

        private final Vector4f color;

        BiomeType(Vector4f color) {
            this.color = color;
        }

        public Vector4f getColor() {
            return color;
        }
    }

    private static class Tile {
        Vector2f position;
        float size;
        boolean isWall;
        ResourceType resource;
        BiomeType biome;

        Tile(Vector2f position, float size, boolean isWall, ResourceType resource, BiomeType biome) {
            this.position = position;
            this.size = size;
            this.isWall = isWall;
            this.resource = resource;
            this.biome = biome;
        }
    }

    private long window;
    private Renderer2D renderer;
    private Random random;
    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 900;
    private static final int GRID_SIZE = 10;
    private static final int MAP_WIDTH = WINDOW_WIDTH / GRID_SIZE;
    private static final int MAP_HEIGHT = WINDOW_HEIGHT / GRID_SIZE;
    private static final float TILE_SIZE = 2.0f / MAP_WIDTH;
    private List<Tile> tiles;
    private boolean isDragging = false;
    private Vector2f lastMousePosition;
    private Vector2f worldOffset;
    private static final float DRAG_SPEED = 1.0f;
    private perlin terrainNoise;    // 地形噪声
    private perlin resourceNoise;   // 资源噪声
    private perlin biomeNoise;      // 群系噪声
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
        random = new Random();
        tiles = new ArrayList<>();
        terrainNoise = new perlin(114514);
        resourceNoise = new perlin(1919810);
        biomeNoise = new perlin(233333); // 群系噪声使用不同的种子

        // 生成初始地图
        updateTiles();
    }

    private BiomeType getBiome(float worldX, float worldY) {
        // 使用群系噪声确定群系类型
        float biomeValue = biomeNoise.noise(worldX * 0.05f, worldY * 0.05f);
        
        // 使用另一个噪声值来确定是否生成山脉
        float mountainValue = biomeNoise.noise(worldX * 0.1f + 1000, worldY * 0.1f + 1000);
        if (mountainValue > 0.7f) return BiomeType.MOUNTAINS;
        
        if (biomeValue > 0.5f) return BiomeType.PLATEAU;
        if (biomeValue > 0.0f) return BiomeType.HILLS;
        if (biomeValue > -0.5f) return BiomeType.PLAINS;
        return BiomeType.BASIN;
    }

    private boolean isWall(float worldX, float worldY) {
        BiomeType biome = getBiome(worldX, worldY);
        
        // 如果是山脉，使用特殊的生成逻辑
        if (biome == BiomeType.MOUNTAINS) {
            // 使用多层噪声生成山脉
            float baseNoise = terrainNoise.noise(worldX * 0.05f, worldY * 0.05f);
            float detailNoise = terrainNoise.noise(worldX * 0.1f, worldY * 0.1f) * 0.3f;
            float ridgeNoise = terrainNoise.noise(worldX * 0.02f, worldY * 0.02f) * 0.5f;
            float combinedNoise = baseNoise + detailNoise + ridgeNoise;
            
            // 检查周围更大范围以确保山脉的连续性
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    float neighborNoise = terrainNoise.noise((worldX + dx) * 0.05f, (worldY + dy) * 0.05f);
                    if (Math.abs(neighborNoise - baseNoise) > 0.3f) {
                        return false;
                    }
                }
            }
            
            return combinedNoise > 0.3f;
        }
        
        // 其他群系使用原有的生成逻辑
        float baseNoise = terrainNoise.noise(worldX * 0.1f, worldY * 0.1f);
        float detailNoise = terrainNoise.noise(worldX * 0.2f, worldY * 0.2f) * 0.6f;
        float combinedNoise = baseNoise + detailNoise;
        
        float threshold;
        float smoothness;
        
        switch (biome) {
            case PLATEAU:
                threshold = 0.85f;
                smoothness = 0.4f;
                break;
            case HILLS:
                threshold = 0.75f;
                smoothness = 0.35f;
                break;
            case PLAINS:
                threshold = 0.65f;
                smoothness = 0.3f;
                break;
            case BASIN:
                threshold = 0.55f;
                smoothness = 0.25f;
                break;
            default:
                threshold = 0.75f;
                smoothness = 0.35f;
        }
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                float neighborNoise = terrainNoise.noise((worldX + dx) * 0.1f, (worldY + dy) * 0.1f);
                if (Math.abs(neighborNoise - combinedNoise) > smoothness) {
                    return false;
                }
            }
        }
        
        return combinedNoise < threshold;
    }

    private ResourceType generateResource(float worldX, float worldY, boolean isWall) {
        if (isWall) return ResourceType.NONE;

        // 使用独立的资源噪声
        float resourceValue = resourceNoise.noise(worldX * 0.2f, worldY * 0.2f);
        
        // 检查是否在墙体边缘
        boolean isNearWall = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (isWall(worldX + dx, worldY + dy)) {
                    isNearWall = true;
                    break;
                }
            }
        }

        // 根据位置和噪声值决定资源类型
        if (isNearWall) {
            // 墙体边缘有更高概率生成资源
            if (resourceValue > 0.85f) return ResourceType.GOLD;
            if (resourceValue > 0.75f) return ResourceType.STONE;
            if (resourceValue > 0.65f) return ResourceType.WOOD;
        } else {
            // 空地有较低概率生成资源
            if (resourceValue > 0.95f) return ResourceType.GOLD;
            if (resourceValue > 0.90f) return ResourceType.STONE;
            if (resourceValue > 0.85f) return ResourceType.WOOD;
        }

        return ResourceType.NONE;
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

                // 确定群系
                BiomeType biome = getBiome(worldX, worldY);

                // 生成地形
                boolean isWall = isWall(worldX, worldY);

                // 生成资源
                ResourceType resource = generateResource(worldX, worldY, isWall);

                // 计算渲染位置（考虑偏移）
                float screenX = (float)x / MAP_WIDTH * 2.0f - 1.0f + TILE_SIZE/2 + viewOffsetX;
                float screenY = (float)y / MAP_HEIGHT * 2.0f - 1.0f + TILE_SIZE/2 + viewOffsetY;

                // 添加瓦片
                tiles.add(new Tile(
                    new Vector2f(screenX, screenY),
                    TILE_SIZE,
                    isWall,
                    resource,
                    biome
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
                    tile.isWall ? new Vector4f(0.2f, 0.2f, 0.2f, 1.0f) : (tile.resource == ResourceType.NONE ? tile.biome.getColor() : tile.resource.getColor()),
                    -1
                );
            }
            renderer.end();

            glfwSwapBuffers(window);
            glfwPollEvents();

            if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                terrainNoise = new perlin(random.nextLong());
                resourceNoise = new perlin(random.nextLong());
                biomeNoise = new perlin(random.nextLong());
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