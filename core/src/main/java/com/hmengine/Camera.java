package com.hmengine;

import org.joml.Matrix4f;

public class Camera {
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private float aspectRatio;
    private float zoom = 1.0f;

    public Camera(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        updateProjectionMatrix();
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        updateProjectionMatrix();
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        updateProjectionMatrix();
    }

    private void updateProjectionMatrix() {
        // 创建正交投影矩阵，保持长宽比
        float width = 2.0f / zoom;
        float height = width / aspectRatio;
        projectionMatrix.identity();
        projectionMatrix.ortho(-width/2, width/2, -height/2, height/2, -1.0f, 1.0f);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
} 