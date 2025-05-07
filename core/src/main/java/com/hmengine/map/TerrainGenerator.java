package com.hmengine.map;

import com.hmengine.math.perlin;
import java.util.Random;

public class TerrainGenerator {
    private perlin baseNoise;      // 基础噪声
    private perlin detailNoise;    // 细节噪声
    private perlin ridgeNoise;     // 山脊噪声
    private final Random random;

    // 噪声参数
    private static final float BASE_SCALE = 0.05f;    // 基础噪声缩放
    private static final float DETAIL_SCALE = 0.2f;   // 细节噪声缩放
    private static final float RIDGE_SCALE = 0.02f;   // 山脊噪声缩放
    private static final float DETAIL_WEIGHT = 0.1f;  // 细节噪声权重
    private static final float RIDGE_WEIGHT = 0.6f;   // 山脊噪声权重

    public TerrainGenerator(long seed) {
        this.random = new Random(seed);
        initializeNoise(seed);
    }

    private void initializeNoise(long seed) {
        this.baseNoise = new perlin(seed);
        this.detailNoise = new perlin(seed + 1);
        this.ridgeNoise = new perlin(seed + 2);
    }

    /**
     * 生成地形高度值
     * @param x 世界坐标X
     * @param y 世界坐标Y
     * @return 地形高度值 (0-1)
     */
    public float generateHeight(float x, float y) {
        // 基础地形
        float base = baseNoise.noise(x * BASE_SCALE, y * BASE_SCALE);
        
        // 添加细节
        float detail = detailNoise.noise(x * DETAIL_SCALE, y * DETAIL_SCALE) * DETAIL_WEIGHT;
        
        // 添加山脊
        float ridge = ridgeNoise.noise(x * RIDGE_SCALE, y * RIDGE_SCALE) * RIDGE_WEIGHT;
        
        // 组合所有噪声
        float combined = base + detail + ridge;
        
        // 归一化到0-1范围
        return (combined + 1.0f) * 0.5f;
    }

    /**
     * 检查指定位置是否为墙体
     * @param x 世界坐标X
     * @param y 世界坐标Y
     * @return 是否为墙体
     */
    public boolean isWall(float x, float y) {
        float height = generateHeight(x, y);
        
        // 检查周围点的高度，确保地形连续性
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                float neighborHeight = generateHeight(x + dx, y + dy);
                // 如果相邻点高度差异太大，则不是墙体
                if (Math.abs(neighborHeight - height) > 0.3f) {
                    return false;
                }
            }
        }
        
        // 使用阈值判断是否为墙体
        return height > 0.95f;
    }

    /**
     * 重新生成随机种子
     */
    public void reseed() {
        initializeNoise(random.nextLong());
    }
} 