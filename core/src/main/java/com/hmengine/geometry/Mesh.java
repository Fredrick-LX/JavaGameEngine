package com.hmengine.geometry;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * 网格类
 */
public class Mesh {
    private float[] vertices;
    private int vertexCount;
    private int primitiveType;  // 图元类型，如GL_TRIANGLES, GL_LINES等
    private Vector3f position;
    private Vector3f rotation;
    private Vector3f scale;
    private Matrix4f modelMatrix;
    private Vector4f color;  // 新增颜色属性

    /**
     * 构造网格
     * @param vertices 顶点数组
     * @param primitiveType 图元类型
     */
    public Mesh(float[] vertices, int primitiveType) {
        this.vertices = vertices;
        this.vertexCount = vertices.length / 3;  // 每个顶点3个分量
        this.primitiveType = primitiveType;
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.rotation = new Vector3f(0.0f, 0.0f, 0.0f);
        this.scale = new Vector3f(1.0f, 1.0f, 1.0f);
        this.modelMatrix = new Matrix4f();
        this.color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);  // 默认白色
        updateModelMatrix();
    }

    /**
     * 设置位置
     * @param x x坐标
     * @param y y坐标
     * @param z z坐标
     */
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        updateModelMatrix();
    }

    /**
     * 设置旋转
     * @param x x坐标
     * @param y y坐标
     * @param z z坐标
     */
    public void setRotation(float x, float y, float z) {
        rotation.set(x, y, z);
        updateModelMatrix();
    }

    /**
     * 设置缩放
     * @param x x坐标
     * @param y y坐标
     * @param z z坐标
     */
    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
        updateModelMatrix();
    }

    /**
     * 更新模型矩阵
     */
    private void updateModelMatrix() {
        modelMatrix.identity()
            .translate(position)
            .rotateXYZ(rotation)
            .scale(scale);
    }

    /**
     * 获取顶点数组
     * @return 顶点数组
     */
    public float[] getVertices() {
        return vertices;
    }

    /**
     * 获取顶点数
     * @return 顶点数
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * 获取图元类型
     * @return 图元类型
     */
    public int getPrimitiveType() {
        return primitiveType;
    }

    /**
     * 获取模型矩阵
     * @return Matrix4f 模型矩阵
     */
    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    /**
     * 获取位置
     * @return Vector3f 位置
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * 获取旋转
     * @return Vector3f 旋转
     */
    public Vector3f getRotation() {
        return rotation;
    }

    /**
     * 设置颜色
     * @param r 红色
     * @param g 绿色
     * @param b 蓝色
     * @param a 透明度
     */
    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    /**
     * 获取颜色
     * @return Vector4f 颜色
     */
    public Vector4f getColor() {
        return color;
    }
} 