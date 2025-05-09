package com.hmengine.map;

import com.hmengine.math.perlin;
import java.util.Random;

public class TerrainGenerator {
    private perlin noise;          // 柏林噪声
    private final Random random;

    // 噪声参数
    private static final float NOISE_SCALE = 0.1f;    // 噪声缩放

    // 地形阈值
    private static final float WALL_THRESHOLD = 0.85f;    // 提高墙体阈值，减少墙体数量
    private static final float WATER_THRESHOLD = 0.2f;    // 降低水域阈值，减少水域数量
    private static final float MOUNTAIN_THRESHOLD = 0.7f; // 提高山地阈值，增加山地数量
    private static final float FOREST_THRESHOLD = 0.4f;   // 提高森林阈值，增加森林数量

    public TerrainGenerator(long seed) {
        this.random = new Random(seed);
        initializeNoise(seed);
    }

    private void initializeNoise(long seed) {
        this.noise = new perlin(seed);
    }

    /**
     * 生成地形值
     * @param x 世界坐标X
     * @param y 世界坐标Y
     * @return 地形值 (0-1)
     */
    private float generateValue(float x, float y) {
        float value = noise.noise(x * NOISE_SCALE, y * NOISE_SCALE);
        return (value + 1.0f) * 0.5f; // 归一化到0-1范围
    }

    /**
     * 检查指定位置是否为水域
     * @param x 世界坐标X
     * @param y 世界坐标Y
     * @return 是否为水域
     */
    public boolean isWater(float x, float y) {
        return generateValue(x, y) < WATER_THRESHOLD;
    }

    /**
     * 检查指定位置是否为山地
     * @param x 世界坐标X
     * @param y 世界坐标Y
     * @return 是否为山地
     */
    public boolean isMountain(float x, float y) {
        float value = generateValue(x, y);
        return value > MOUNTAIN_THRESHOLD && value <= WALL_THRESHOLD;
    }

    /**
     * 检查指定位置是否为森林
     * @param x 世界坐标X
     * @param y 世界坐标Y
     * @return 是否为森林
     */
    public boolean isForest(float x, float y) {
        float value = generateValue(x, y);
        return value > FOREST_THRESHOLD && value <= MOUNTAIN_THRESHOLD;
    }

    /**
     * 重新生成随机种子
     */
    public void reseed() {
        initializeNoise(random.nextLong());
    }
} 