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

public class ShaderProgram {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgram.class);
    
    private final int programId;
    private final List<Shader> shaders;
    
    public ShaderProgram() {
        programId = GL20.glCreateProgram();
        shaders = new ArrayList<>();
    }
    
    public void attachShader(Shader shader) {
        GL20.glAttachShader(programId, shader.getShaderId());
        shaders.add(shader);
    }
    
    public void link() {
        GL20.glLinkProgram(programId);
        
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            String error = GL20.glGetProgramInfoLog(programId);
            logger.error("Shader program linking failed: {}", error);
            throw new RuntimeException("Shader program linking failed: " + error);
        }
    }
    
    public void use() {
        GL20.glUseProgram(programId);
    }
    
    public void detachShaders() {
        for (Shader shader : shaders) {
            GL20.glDetachShader(programId, shader.getShaderId());
        }
    }
    
    public void delete() {
        detachShaders();
        for (Shader shader : shaders) {
            shader.delete();
        }
        shaders.clear();
        GL20.glDeleteProgram(programId);
    }
    
    // Uniform setters
    public void setUniform(String name, int value) {
        GL20.glUniform1i(GL20.glGetUniformLocation(programId, name), value);
    }
    
    public void setUniform(String name, float value) {
        GL20.glUniform1f(GL20.glGetUniformLocation(programId, name), value);
    }
    
    public void setUniform(String name, Vector2f value) {
        GL20.glUniform2f(GL20.glGetUniformLocation(programId, name), value.x, value.y);
    }
    
    public void setUniform(String name, Vector3f value) {
        GL20.glUniform3f(GL20.glGetUniformLocation(programId, name), value.x, value.y, value.z);
    }
    
    public void setUniform(String name, Vector4f value) {
        GL20.glUniform4f(GL20.glGetUniformLocation(programId, name), value.x, value.y, value.z, value.w);
    }
    
    public void setUniform(String name, Matrix4f value) {
        float[] matrix = new float[16];
        value.get(matrix);
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(programId, name), false, matrix);
    }
    
    public int getProgramId() {
        return programId;
    }
} 