package com.hmengine.geometry;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Mesh {
    private float[] vertices;
    private int vertexCount;
    private int primitiveType;  // 图元类型，如GL_TRIANGLES, GL_LINES等
    private Vector3f position;
    private Vector3f rotation;
    private Vector3f scale;
    private Matrix4f modelMatrix;
    private Vector4f color;  // 新增颜色属性

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

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        updateModelMatrix();
    }

    public void setRotation(float x, float y, float z) {
        rotation.set(x, y, z);
        updateModelMatrix();
    }

    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
        updateModelMatrix();
    }

    private void updateModelMatrix() {
        modelMatrix.identity()
            .translate(position)
            .rotateXYZ(rotation)
            .scale(scale);
    }

    public float[] getVertices() {
        return vertices;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getPrimitiveType() {
        return primitiveType;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    public Vector4f getColor() {
        return color;
    }
} 