package com.hmengine.geometry;

import static org.lwjgl.opengl.GL11.*;

/**
 * 几何类
 */
public class Geometry {
    /**
     * 创建三角形网格
     * @return 三角形网格
     */
    public static Mesh createTriangle() {
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,  // 左下角顶点
             0.5f, -0.5f, 0.0f,  // 右下角顶点
             0.0f,  0.5f, 0.0f   // 顶部顶点
        };
        return new Mesh(vertices, GL_TRIANGLES);
    }

    /**
     * 创建矩形网格
     * @return 矩形网格
     */
    public static Mesh createRectangle() {
        float[] vertices = {
            // 第一个三角形
            -0.5f,  0.5f, 0.0f,  // 左上
            -0.5f, -0.5f, 0.0f,  // 左下
             0.5f, -0.5f, 0.0f,  // 右下
            // 第二个三角形
            -0.5f,  0.5f, 0.0f,  // 左上
             0.5f, -0.5f, 0.0f,  // 右下
             0.5f,  0.5f, 0.0f   // 右上
        };
        return new Mesh(vertices, GL_TRIANGLES);
    }

    /**
     * 创建六边形网格
     * @return 六边形网格
     */
    public static Mesh createHexagon() {
        float radius = 0.5f;
        float[] vertices = {
            // 中心点
             0.0f,  0.0f, 0.0f,  // 中心
            // 第一个三角形
             0.0f,  radius, 0.0f,  // 上
             radius * 0.866f,  radius * 0.5f, 0.0f,  // 右上
            // 第二个三角形 
             radius * 0.866f,  radius * 0.5f, 0.0f,  // 右上
             radius * 0.866f, -radius * 0.5f, 0.0f,  // 右下
             0.0f,  0.0f, 0.0f,  // 中心
            // 第三个三角形
             radius * 0.866f, -radius * 0.5f, 0.0f,  // 右下
             0.0f, -radius, 0.0f,  // 下
             0.0f,  0.0f, 0.0f,  // 中心
            // 第四个三角形
             0.0f, -radius, 0.0f,  // 下
            -radius * 0.866f, -radius * 0.5f, 0.0f,  // 左下
             0.0f,  0.0f, 0.0f,  // 中心
            // 第五个三角形
            -radius * 0.866f, -radius * 0.5f, 0.0f,  // 左下
            -radius * 0.866f,  radius * 0.5f, 0.0f,  // 左上
             0.0f,  0.0f, 0.0f,  // 中心
            // 第六个三角形
            -radius * 0.866f,  radius * 0.5f, 0.0f,  // 左上
             0.0f,  radius, 0.0f,  // 上
             0.0f,  0.0f, 0.0f   // 中心
        };
        return new Mesh(vertices, GL_TRIANGLES);
    }
} 