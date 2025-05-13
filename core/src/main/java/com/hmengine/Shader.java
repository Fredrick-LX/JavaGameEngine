package com.hmengine;

import org.joml.Matrix4f;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

/**
 * 着色器类
 */
public class Shader {
    private int shaderProgram;
    private int projectionMatrixLocation;
    private int viewMatrixLocation;
    private int modelMatrixLocation;
    private int colorLocation;

    /**
     * 构造函数
     * @param vertexPath 顶点着色器路径
     * @param fragmentPath 片段着色器路径
     */
    public Shader(String vertexPath, String fragmentPath) {
        createShaders(vertexPath, fragmentPath);
        getUniformLocations();
    }

    /**
     * 加载着色器源码
     * @param path 着色器路径
     * @return 着色器源码
     */
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

    /**
     * 创建着色器
     * @param vertexPath 顶点着色器路径
     * @param fragmentPath 片段着色器路径
     */
    private void createShaders(String vertexPath, String fragmentPath) {
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

    /**
     * 获取着色器变量位置
     */
    private void getUniformLocations() {
        projectionMatrixLocation = glGetUniformLocation(shaderProgram, "projectionMatrix");
        viewMatrixLocation = glGetUniformLocation(shaderProgram, "viewMatrix");
        modelMatrixLocation = glGetUniformLocation(shaderProgram, "modelMatrix");
        colorLocation = glGetUniformLocation(shaderProgram, "color");
    }

    /**
     * 检查着色器编译错误
     * @param shader 着色器
     * @param type 着色器类型
     */
    private void checkShaderCompileError(int shader, String type) {
        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(shader, GL_INFO_LOG_LENGTH);
            String infoLog = glGetShaderInfoLog(shader, len);
            System.err.println("着色器编译错误 (" + type + "): " + infoLog);
        }
    }

    /**
     * 检查着色器程序链接错误
     * @param program 着色器程序
     */
    private void checkProgramLinkError(int program) {
        int success = glGetProgrami(program, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(program, GL_INFO_LOG_LENGTH);
            String infoLog = glGetProgramInfoLog(program, len);
            System.err.println("着色器程序链接错误: " + infoLog);
        }
    }

    /**
     * 使用着色器
     */
    public void use() {
        glUseProgram(shaderProgram);
    }

    /**
     * 设置投影矩阵
     * @param matrix 投影矩阵
     */
    public void setProjectionMatrix(Matrix4f matrix) {
        float[] matrixArray = new float[16];
        matrix.get(matrixArray);
        glUniformMatrix4fv(projectionMatrixLocation, false, matrixArray);
    }

    /**
     * 设置视图矩阵
     * @param matrix 视图矩阵
     */
    public void setViewMatrix(Matrix4f matrix) {
        float[] matrixArray = new float[16];
        matrix.get(matrixArray);
        glUniformMatrix4fv(viewMatrixLocation, false, matrixArray);
    }

    /**
     * 设置模型矩阵
     * @param matrix 模型矩阵
     */
    public void setModelMatrix(Matrix4f matrix) {
        float[] matrixArray = new float[16];
        matrix.get(matrixArray);
        glUniformMatrix4fv(modelMatrixLocation, false, matrixArray);
    }

    /**
     * 设置颜色
     * @param r 红色
     * @param g 绿色
     * @param b 蓝色
     * @param a 透明度
     */
    public void setColor(float r, float g, float b, float a) {
        glUniform4f(colorLocation, r, g, b, a);
    }

    /**
     * 清理
     */
    public void cleanup() {
        glDeleteProgram(shaderProgram);
    }

    /**
     * 获取着色器程序ID
     * @return 着色器程序ID
     */
    public int getProgramId() {
        return shaderProgram;
    }
} 