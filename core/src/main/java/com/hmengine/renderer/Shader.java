package com.hmengine.renderer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;

/**
 * 着色器程序类，用于管理OpenGL着色器程序
 */
public class Shader {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    /**
     * 创建一个新的着色器程序
     * @param vertexPath 顶点着色器文件的路径
     * @param fragmentPath 片段着色器文件的路径
     * @throws RuntimeException 如果着色器编译或链接失败
     */
    public Shader(String vertexPath, String fragmentPath) {
        programId = glCreateProgram();
        vertexShaderId = createShader(vertexPath, GL_VERTEX_SHADER);
        fragmentShaderId = createShader(fragmentPath, GL_FRAGMENT_SHADER);

        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("着色器程序链接失败: " + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
    }

    /**
     * 创建并编译着色器
     * @param filePath 着色器文件的路径
     * @param type 着色器类型（GL_VERTEX_SHADER 或 GL_FRAGMENT_SHADER）
     * @return 编译后的着色器ID
     * @throws RuntimeException 如果着色器文件无法加载或编译失败
     */
    private int createShader(String filePath, int type) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            if (is == null) {
                throw new RuntimeException("无法找到着色器文件: " + filePath);
            }
            
            String source = reader.lines().collect(Collectors.joining("\n"));
            
            int shaderId = glCreateShader(type);
            glShaderSource(shaderId, source);
            glCompileShader(shaderId);

            if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
                throw new RuntimeException("着色器编译失败: " + glGetShaderInfoLog(shaderId));
            }

            return shaderId;
        } catch (Exception e) {
            throw new RuntimeException("无法加载着色器文件: " + filePath, e);
        }
    }

    /**
     * 绑定着色器程序，使其成为当前活动的着色器程序
     */
    public void bind() {
        glUseProgram(programId);
    }

    /**
     * 解绑着色器程序
     */
    public void unbind() {
        glUseProgram(0);
    }

    /**
     * 清理着色器程序资源
     */
    public void cleanup() {
        unbind();
        glDeleteProgram(programId);
    }

    /**
     * 设置整数类型的uniform变量
     * @param name uniform变量的名称
     * @param value 要设置的值
     */
    public void setInt(String name, int value) {
        glUniform1i(glGetUniformLocation(programId, name), value);
    }

    /**
     * 设置浮点数类型的uniform变量
     * @param name uniform变量的名称
     * @param value 要设置的值
     */
    public void setUniform(String name, float value) {
        glUniform1f(glGetUniformLocation(programId, name), value);
    }

    /**
     * 设置二维向量类型的uniform变量
     * @param name uniform变量的名称
     * @param value 要设置的二维向量值
     */
    public void setUniform(String name, Vector2f value) {
        glUniform2f(glGetUniformLocation(programId, name), value.x, value.y);
    }

    /**
     * 设置三维向量类型的uniform变量
     * @param name uniform变量的名称
     * @param value 要设置的三维向量值
     */
    public void setUniform(String name, Vector3f value) {
        glUniform3f(glGetUniformLocation(programId, name), value.x, value.y, value.z);
    }

    /**
     * 设置四维向量类型的uniform变量
     * @param name uniform变量的名称
     * @param value 要设置的四维向量值
     */
    public void setUniform(String name, Vector4f value) {
        glUniform4f(glGetUniformLocation(programId, name), value.x, value.y, value.z, value.w);
    }

    /**
     * 设置4x4矩阵类型的uniform变量
     * @param name uniform变量的名称
     * @param value 要设置的4x4矩阵值
     */
    public void setUniform(String name, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            glUniformMatrix4fv(glGetUniformLocation(programId, name), false, buffer);
        }
    }
}