package com.hmengine.render;

import com.hmengine.render.shader.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class Renderer {
    protected ShaderProgram shaderProgram;
    
    public Renderer() {
        initialize();
    }
    
    protected abstract void initialize();
    
    public void begin() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        if (shaderProgram != null) {
            shaderProgram.use();
        }
    }
    
    public void end() {
        if (shaderProgram != null) {
            GL20.glUseProgram(0);
        }
    }
    
    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.delete();
        }
    }
    
    protected void setShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }
    
    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }
} 