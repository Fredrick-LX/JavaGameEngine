package com.hmengine.render.shader;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 着色器程序类
 */
public class ShaderProgram {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgram.class);
    
    private final int programId;
    private final List<Shader> shaders;

    /**
     * 构造函数
     */
    public ShaderProgram() {
        programId = GL20.glCreateProgram();
        shaders = new ArrayList<>();
    }

    /**
     * 附加着色器
     * @param shader 着色器
     */
    public void attachShader(Shader shader) {
        GL20.glAttachShader(programId, shader.getShaderId());
        shaders.add(shader);
    }

    /**
     * 链接程序
     */
    public void link() {
        GL20.glLinkProgram(programId);
        
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            String error = GL20.glGetProgramInfoLog(programId);
            logger.error("Shader program linking failed: {}", error);
            throw new RuntimeException("Shader program linking failed: " + error);
        }
    }

    /**
     * 使用程序
     */
    public void use() {
        GL20.glUseProgram(programId);
    }

    /**
     * 分离着色器
     */
    public void detachShaders() {
        for (Shader shader : shaders) {
            GL20.glDetachShader(programId, shader.getShaderId());
        }
    }

    /**
     * 删除程序
     */
    public void delete() {
        detachShaders();
        for (Shader shader : shaders) {
            shader.delete();
        }
        shaders.clear();
        GL20.glDeleteProgram(programId);
    }

    /**
     * 设置Uniform
     * @param name 名称
     * @param value 值
     */
    public void setUniform(String name, int value) {
        GL20.glUniform1i(GL20.glGetUniformLocation(programId, name), value);
    }

    /**
     * 设置Uniform
     * @param name 名称
     * @param value 值
     */
    public void setUniform(String name, float value) {
        GL20.glUniform1f(GL20.glGetUniformLocation(programId, name), value);
    }

    /**
     * 设置Uniform
     * @param name 名称
     * @param value 值
     */
    public void setUniform(String name, Vector2f value) {
        GL20.glUniform2f(GL20.glGetUniformLocation(programId, name), value.x, value.y);
    }

    /**
     * 设置Uniform
     * @param name 名称
     * @param value 值
     */
    public void setUniform(String name, Vector3f value) {
        GL20.glUniform3f(GL20.glGetUniformLocation(programId, name), value.x, value.y, value.z);
    }

    /**
     * 设置Uniform
     * @param name 名称
     * @param value 值
     */
    public void setUniform(String name, Vector4f value) {
        GL20.glUniform4f(GL20.glGetUniformLocation(programId, name), value.x, value.y, value.z, value.w);
    }

    /**
     * 设置Uniform
     * @param name 名称
     * @param value 值
     */
    public void setUniform(String name, Matrix4f value) {
        float[] matrix = new float[16];
        value.get(matrix);
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(programId, name), false, matrix);
    }

    /**
     * 获取程序ID
     * @return 程序ID
     */
    public int getProgramId() {
        return programId;
    }
} 