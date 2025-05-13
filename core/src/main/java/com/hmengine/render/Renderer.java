package com.hmengine.render;

import com.hmengine.render.shader.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * 渲染器类
 */
public abstract class Renderer {
    protected ShaderProgram shaderProgram;
    
    /**
     * 构造函数
     */
    public Renderer() {
        initialize();
    }
    
    /**
     * 初始化
     */
    protected abstract void initialize();
    
    /**
     * 开始渲染
     */
    public void begin() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        if (shaderProgram != null) {
            shaderProgram.use();
        }
    }
    
    /**
     * 结束渲染
     */
    public void end() {
        if (shaderProgram != null) {
            GL20.glUseProgram(0);
        }
    }

    /**
     * 清理
     */
    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.delete();
        }
    }

    /**
     * 设置着色器程序
     * @param shaderProgram 着色器程序
     */
    protected void setShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    /**
     * 获取着色器程序
     * @return 着色器程序
     */
    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }
} 