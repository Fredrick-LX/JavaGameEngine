package com.hmengine;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.Vector3f;

/**
 * 摄像机类
 */
public class Camera {
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private float aspectRatio;
    private float zoom = 1.0f;
    
    // 相机位置和旋转
    private Vector3f position;
    private Vector3f rotation;
    
    // 视锥体平面
    private Vector4f[] frustumPlanes = new Vector4f[6];
    
    /**
     * 构造函数
     * @param aspectRatio 视图比例
     */
    public Camera(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.rotation = new Vector3f(0.0f, 0.0f, 0.0f);
        for (int i = 0; i < 6; i++) {
            frustumPlanes[i] = new Vector4f();
        }
        updateProjectionMatrix();
        updateViewMatrix();
    }

    /**
     * 设置位置
     * @param x x坐标
     * @param y y坐标
     * @param z z坐标
     */
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        updateViewMatrix();
    }

    /**
     * 设置旋转
     * @param x x坐标
     * @param y y坐标
     * @param z z坐标
     */
    public void setRotation(float x, float y, float z) {
        rotation.set(x, y, z);
        updateViewMatrix();
    }

    /**
     * 移动
     * @param dx x坐标
     * @param dy y坐标
     * @param dz z坐标
     */
    public void move(float dx, float dy, float dz) {
        position.add(dx, dy, dz);
        updateViewMatrix();
    }

    /**
     * 旋转
     * @param dx x坐标
     * @param dy y坐标
     * @param dz z坐标
     */
    public void rotate(float dx, float dy, float dz) {
        rotation.add(dx, dy, dz);
        updateViewMatrix();
    }

    /**
     * 设置视图比例
     * @param aspectRatio 视图比例
     */
    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        updateProjectionMatrix();
    }

    /**
     * 设置缩放
     * @param zoom 缩放
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        updateProjectionMatrix();
    }

    /**
     * 更新投影矩阵
     */
    private void updateProjectionMatrix() {
        // 创建正交投影矩阵，保持长宽比
        float width = 2.0f / zoom;
        float height = width / aspectRatio;
        projectionMatrix.identity();
        projectionMatrix.ortho(-width/2, width/2, -height/2, height/2, -1.0f, 1.0f);
        updateFrustumPlanes();
    }

    /**
     * 更新视图矩阵
     */
    private void updateViewMatrix() {
        viewMatrix.identity()
            .rotateXYZ(rotation)
            .translate(-position.x, -position.y, -position.z);
        updateFrustumPlanes();
    }

    /**
     * 更新视锥体平面
     */
    private void updateFrustumPlanes() {
        Matrix4f vp = new Matrix4f(projectionMatrix).mul(viewMatrix);
        
        // 提取视锥体平面
        // 左平面
        frustumPlanes[0].set(vp.m03() + vp.m00(), vp.m13() + vp.m10(), vp.m23() + vp.m20(), vp.m33() + vp.m30()).normalize3();
        // 右平面
        frustumPlanes[1].set(vp.m03() - vp.m00(), vp.m13() - vp.m10(), vp.m23() - vp.m20(), vp.m33() - vp.m30()).normalize3();
        // 下平面
        frustumPlanes[2].set(vp.m03() + vp.m01(), vp.m13() + vp.m11(), vp.m23() + vp.m21(), vp.m33() + vp.m31()).normalize3();
        // 上平面
        frustumPlanes[3].set(vp.m03() - vp.m01(), vp.m13() - vp.m11(), vp.m23() - vp.m21(), vp.m33() - vp.m31()).normalize3();
        // 近平面
        frustumPlanes[4].set(vp.m03() + vp.m02(), vp.m13() + vp.m12(), vp.m23() + vp.m22(), vp.m33() + vp.m32()).normalize3();
        // 远平面
        frustumPlanes[5].set(vp.m03() - vp.m02(), vp.m13() - vp.m12(), vp.m23() - vp.m22(), vp.m33() - vp.m32()).normalize3();
    }

    /**
     * 判断点是否在视锥体内
     * @param x x坐标
     * @param y y坐标
     * @param z z坐标
     * @param radius 半径
     * @return 是否在视锥体内
     */
    public boolean isInFrustum(float x, float y, float z, float radius) {
        Vector4f pos = new Vector4f(x, y, z, 1.0f);
        for (Vector4f plane : frustumPlanes) {
            if (plane.dot(pos) + radius < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取投影矩阵
     * @return 投影矩阵
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * 获取视图矩阵
     * @return 视图矩阵
     */
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
} 