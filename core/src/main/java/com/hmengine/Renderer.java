package com.hmengine;

import com.hmengine.geometry.Mesh;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    private int VAO;
    private int VBO;
    private Shader shader;
    private Camera camera;
    private Scene scene;
    
    // 控制网格线和轮廓线显示的布尔变量
    private boolean showGridLines = true;

    public Renderer(Shader shader, Camera camera) {
        this.shader = shader;
        this.camera = camera;
        this.scene = new Scene();
        init();
    }

    private void init() {
        // 创建VAO和VBO
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0L);
        glEnableVertexAttribArray(0);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void addMesh(Mesh mesh) {
        scene.addMesh(mesh);
    }

    public void removeMesh(Mesh mesh) {
        scene.removeMesh(mesh);
    }

    private void updateBuffer(Mesh mesh) {
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, mesh.getVertices(), GL_STATIC_DRAW);
    }

    public void render() {
        // 渲染场景中的所有物体
        for (Mesh mesh : scene.getMeshes()) {
            // 检查物体是否在视锥体内
            Vector3f position = mesh.getPosition();
            float radius = 0.1f; // 假设物体的包围球半径为0.1
            if (!camera.isInFrustum(position.x, position.y, position.z, radius)) {
                continue; // 如果不在视锥体内，跳过渲染
            }
            
            // 更新顶点数据
            updateBuffer(mesh);
            
            // 使用主着色器绘制填充部分
            shader.use();
            shader.setProjectionMatrix(camera.getProjectionMatrix());
            shader.setViewMatrix(camera.getViewMatrix());
            shader.setModelMatrix(mesh.getModelMatrix());
            shader.setColor(
                mesh.getColor().x,
                mesh.getColor().y,
                mesh.getColor().z,
                mesh.getColor().w
            );
            
            // 绘制填充部分
            glBindVertexArray(VAO);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glDrawArrays(mesh.getPrimitiveType(), 0, mesh.getVertexCount());

            // 绘制网格线
            if (showGridLines) {
                shader.setColor(0.0f, 1.0f, 0.0f, 1.0f); // 绿色网格线
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                glDrawArrays(mesh.getPrimitiveType(), 0, mesh.getVertexCount());
            }
        }
    }

    public void cleanup() {
        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        shader.cleanup();
    }

    // Getter和Setter方法
    public boolean isShowGridLines() {
        return showGridLines;
    }

    public void setShowGridLines(boolean showGridLines) {
        this.showGridLines = showGridLines;
    }
}