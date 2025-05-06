package com.hmengine.renderer;

import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;

/**
 * 2D渲染器类，用于渲染2D图形（主要是四边形）
 */
public class Renderer2D {
    private static final int MAX_QUADS = 500000; // 最大支持50万个四边形
    private static final int VERTEX_SIZE = (2 + 2) * 4; // 位置(2) + 纹理坐标(2)
    private static final int INSTANCE_SIZE = (2 + 4 + 1 + 1) * 4; // 位置(2) + 颜色(4) + 大小(1) + 纹理索引(1)
    private static final int QUAD_SIZE = VERTEX_SIZE * 4;
    private static final int BUFFER_SIZE = MAX_QUADS * QUAD_SIZE;
    private static final int INSTANCE_BUFFER_SIZE = MAX_QUADS * INSTANCE_SIZE;

    private int vao;
    private int vbo;
    private int instanceVbo;
    private int ebo;
    private Shader shader;
    private List<Quad> quads;
    private int quadCount;
    private List<Texture> textures;
    private static final int MAX_TEXTURES = 32; // 最大支持32个纹理

    /**
     * 创建2D渲染器实例
     * 初始化所有必要的OpenGL资源，包括VAO、VBO、EBO和着色器程序
     */
    public Renderer2D(int screenWidth, int screenHeight) {
        quads = new ArrayList<>();
        textures = new ArrayList<>();
        init(screenWidth, screenHeight);
    }

    /**
     * 添加纹理到渲染器
     * @param texture 要添加的纹理
     * @return 纹理的索引，如果超过最大纹理数量则返回-1
     */
    public int addTexture(Texture texture) {
        if (textures.size() >= MAX_TEXTURES) {
            return -1;
        }
        textures.add(texture);
        return textures.size() - 1;
    }

    /**
     * 初始化渲染器所需的OpenGL资源
     * 包括：
     * - 创建并配置VAO（顶点数组对象）
     * - 创建并配置VBO（顶点缓冲区对象）
     * - 创建并配置EBO（元素缓冲区对象）
     * - 设置顶点属性
     * - 创建着色器程序
     */
    private void init(int screenWidth, int screenHeight) {
        // 创建VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // 创建顶点VBO
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, BUFFER_SIZE, GL_STATIC_DRAW);

        // 设置顶点属性
        // 位置
        glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
        glEnableVertexAttribArray(0);

        // 纹理坐标
        glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_SIZE, 2 * 4);
        glEnableVertexAttribArray(1);

        // 创建实例化VBO
        instanceVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glBufferData(GL_ARRAY_BUFFER, INSTANCE_BUFFER_SIZE, GL_DYNAMIC_DRAW);

        // 设置实例化属性
        // 位置偏移
        glVertexAttribPointer(2, 2, GL_FLOAT, false, INSTANCE_SIZE, 0);
        glEnableVertexAttribArray(2);
        glVertexAttribDivisor(2, 1);

        // 颜色
        glVertexAttribPointer(3, 4, GL_FLOAT, false, INSTANCE_SIZE, 2 * 4);
        glEnableVertexAttribArray(3);
        glVertexAttribDivisor(3, 1);

        // 大小
        glVertexAttribPointer(4, 1, GL_FLOAT, false, INSTANCE_SIZE, 6 * 4);
        glEnableVertexAttribArray(4);
        glVertexAttribDivisor(4, 1);

        // 纹理索引
        glVertexAttribPointer(5, 1, GL_FLOAT, false, INSTANCE_SIZE, 7 * 4);
        glEnableVertexAttribArray(5);
        glVertexAttribDivisor(5, 1);

        // 创建EBO
        int[] indices = new int[MAX_QUADS * 6];
        for (int i = 0; i < MAX_QUADS; i++) {
            int offset = i * 4;
            indices[i * 6] = offset;
            indices[i * 6 + 1] = offset + 1;
            indices[i * 6 + 2] = offset + 2;
            indices[i * 6 + 3] = offset + 2;
            indices[i * 6 + 4] = offset + 3;
            indices[i * 6 + 5] = offset;
        }

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // 创建着色器
        shader = new Shader("shaders/2d_vertex.glsl", "shaders/2d_fragment.glsl");

        // 初始化基础顶点数据（正方形）
        // 根据屏幕宽高计算正方形的顶点位置
        float aspectRatio = (float)screenHeight / screenWidth;
        float baseSize = 1f; // 基础大小

        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(4 * VERTEX_SIZE);
        // 左下角
        vertexBuffer.put(-baseSize * aspectRatio).put(-baseSize).put(0.0f).put(0.0f);
        // 右下角
        vertexBuffer.put(baseSize * aspectRatio).put(-baseSize).put(1.0f).put(0.0f);
        // 右上角
        vertexBuffer.put(baseSize * aspectRatio).put(baseSize).put(1.0f).put(1.0f);
        // 左上角
        vertexBuffer.put(-baseSize * aspectRatio).put(baseSize).put(0.0f).put(1.0f);
        vertexBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

        // 解绑
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * 开始新的渲染批次
     * 清除之前的四边形数据，准备新的渲染
     */
    public void begin() {
        quads.clear();
        quadCount = 0;
    }

    /**
     * 提交一个四边形到渲染队列
     * @param position 四边形左下角的位置坐标
     * @param size 四边形的尺寸（宽度和高度）
     * @param color 四边形的颜色（RGBA）
     * @param textureIndex 纹理索引，-1表示不使用纹理
     */
    public void submit(Vector2f position, Vector2f size, Vector4f color, int textureIndex) {
        if (quadCount >= MAX_QUADS) {
            return;
        }

        Quad quad = new Quad(position, size, color, textureIndex);
        quads.add(quad);
        quadCount++;
    }

    /**
     * 结束当前渲染批次并执行渲染
     * 将收集的四边形数据上传到GPU并绘制
     */
    public void end() {
        if (quadCount == 0) return;

        // 更新实例化数据
        FloatBuffer instanceBuffer = org.lwjgl.BufferUtils.createFloatBuffer(quadCount * INSTANCE_SIZE);
        
        for (Quad quad : quads) {
            // 位置偏移
            instanceBuffer.put(quad.position.x).put(quad.position.y);
            // 颜色
            instanceBuffer.put(quad.color.x).put(quad.color.y).put(quad.color.z).put(quad.color.w);
            // 大小
            instanceBuffer.put(Math.max(quad.size.x, quad.size.y));
            // 纹理索引
            instanceBuffer.put(quad.textureIndex);
        }
        
        instanceBuffer.flip();
        
        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, instanceBuffer);

        // 绑定所有纹理
        for (int i = 0; i < textures.size(); i++) {
            textures.get(i).bind(i);
        }

        // 渲染
        shader.bind();
        shader.setInt("u_Textures", MAX_TEXTURES);
        glBindVertexArray(vao);
        glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0, quadCount);
        glBindVertexArray(0);
        shader.unbind();

        // 解绑所有纹理
        for (int i = 0; i < textures.size(); i++) {
            textures.get(i).unbind();
        }
    }

    /**
     * 清理渲染器资源
     * 释放所有OpenGL资源，包括VAO、VBO、EBO和着色器程序
     */
    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(instanceVbo);
        glDeleteBuffers(ebo);
        shader.cleanup();
        for (Texture texture : textures) {
            texture.cleanup();
        }
    }

    /**
     * 内部类，表示一个四边形的基本属性
     */
    private static class Quad {
        Vector2f position;
        Vector2f size;
        Vector4f color;
        int textureIndex;

        Quad(Vector2f position, Vector2f size, Vector4f color, int textureIndex) {
            this.position = position;
            this.size = size;
            this.color = color;
            this.textureIndex = textureIndex;
        }
    }
} 