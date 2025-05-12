package com.hmengine.text;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TextRenderer {
    private Font font;
    private int shaderProgram;
    private Matrix4f projectionMatrix;
    private int projectionMatrixLocation;
    private int vao;
    private int vbo;

    public TextRenderer(String fontFile, int fontSize, int width, int height, String vertexShaderPath,
            String fragmentShaderPath) {
        this.font = new Font(fontFile, fontSize);
        initShaders(vertexShaderPath, fragmentShaderPath);
        initProjectionMatrix(width, height);
        initBuffers();
    }

    private String loadShaderSource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("无法找到着色器文件: " + path);
            }
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取着色器文件失败: " + path, e);
        }
    }

    private void initShaders(String vertexPath, String fragmentPath) {
        // 从文件加载着色器
        String vertexShaderSource = loadShaderSource(vertexPath);
        String fragmentShaderSource = loadShaderSource(fragmentPath);

        // 编译着色器
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        // 检查着色器编译错误
        checkShaderCompileError(vertexShader, "VERTEX");
        checkShaderCompileError(fragmentShader, "FRAGMENT");

        // 创建着色器程序
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        // 检查链接错误
        checkProgramLinkError(shaderProgram);

        // 删除着色器
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private void initBuffers() {
        // 创建VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // 创建VBO
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // 分配6个顶点（2个三角形）的空间
        glBufferData(GL_ARRAY_BUFFER, 6 * 4 * Float.BYTES, GL_DYNAMIC_DRAW);

        // 设置顶点属性
        // 位置属性
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // 纹理坐标属性
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // 解绑
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void initProjectionMatrix(int width, int height) {
        projectionMatrix = new Matrix4f().ortho(0.0f, width, height, 0.0f, -1.0f, 1.0f);
    }

    public void renderText(String text, float x, float y, float scale, float[] color) {
        GL20.glUseProgram(shaderProgram);

        // 设置投影矩阵
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(16);
            projectionMatrix.get(matrixBuffer);
            GL20.glUniformMatrix4fv(projectionMatrixLocation, false, matrixBuffer);
        }

        // 设置文本颜色
        int textColorLocation = GL20.glGetUniformLocation(shaderProgram, "textColor");
        GL20.glUniform3f(textColorLocation, color[0], color[1], color[2]);

        // 启用混合
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // 绑定VAO和VBO
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // 渲染文本
        font.renderText(text, x, y, scale);

        // 解绑
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // 禁用混合
        glDisable(GL_BLEND);

        GL20.glUseProgram(0);
    }

    public void cleanup() {
        font.cleanup();
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        GL20.glDeleteProgram(shaderProgram);
    }

    public int getShaderProgram() {
        return shaderProgram;
    }

    private void checkShaderCompileError(int shader, String type) {
        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(shader);
            throw new RuntimeException("着色器编译错误 (" + type + "): " + infoLog);
        }
    }

    private void checkProgramLinkError(int program) {
        int success = glGetProgrami(program, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(program);
            throw new RuntimeException("着色器程序链接错误: " + infoLog);
        }
    }
}