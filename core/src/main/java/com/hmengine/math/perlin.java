package com.hmengine.math;

import java.util.Random;

/**
 * Perlin噪声类
 */
public class perlin {
    private final int[] p; // 置换表

    /**
     * 构造Perlin噪声
     * @param seed 种子
     */
    public perlin(long seed) {
        p = new int[512];
        int[] permutation = new int[256];
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }
        Random rand = new Random(seed);
        // 洗牌算法
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = tmp;
        }
        for (int i = 0; i < 512; i++) {
            p[i] = permutation[i % 256];
        }
    }

    /**
     * 计算Perlin噪声
     * @param x x坐标
     * @param y y坐标
     * @return 噪声值
     */
    public float noise(float x, float y) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        float u = fade(x);
        float v = fade(y);
        int A = p[X] + Y;
        int B = p[X + 1] + Y;
        float res = lerp(v,
            lerp(u, grad(p[A], x, y), grad(p[B], x - 1, y)),
            lerp(u, grad(p[A + 1], x, y - 1), grad(p[B + 1], x - 1, y - 1))
        );
        return (res + 1.0f) / 2.0f; // 归一化到[0,1]
    }

    /**
     * 平滑插值
     * @param t 插值参数
     * @return 插值结果
     */
    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * 线性插值
     * @param t 插值参数
     * @param a 起始值
     * @param b 结束值
     * @return 插值结果
     */
    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    /**
     * 梯度计算
     * @param hash 哈希值
     * @param x x坐标
     * @param y y坐标
     * @return 梯度值
     */
    private float grad(int hash, float x, float y) {
        int h = hash & 7; // 只取低3位
        float u = h < 4 ? x : y;
        float v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
