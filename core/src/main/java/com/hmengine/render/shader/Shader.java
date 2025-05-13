package com.hmengine.render.shader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 着色器类
 */
public abstract class Shader {
    private static final Logger logger = LoggerFactory.getLogger(Shader.class);
    
    protected int shaderId;
    protected final ShaderType type;
    protected String source;
    
    /**
     * 着色器类型枚举
     */
    public enum ShaderType {
        VERTEX(GL20.GL_VERTEX_SHADER),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER),
        GEOMETRY(GL43.GL_GEOMETRY_SHADER),
        COMPUTE(GL43.GL_COMPUTE_SHADER);
        
        private final int glType;

        /**
         * 构造函数
         * @param glType 着色器类型
         */
        ShaderType(int glType) {
            this.glType = glType;
        }

        /**
         * 获取着色器类型
         * @return 着色器类型
         */
        public int getGlType() {
            return glType;
        }
    }

    /**
     * 构造函数
     * @param type 着色器类型
     */
    protected Shader(ShaderType type) {
        this.type = type;
    }

    /**
     * 从文件加载着色器
     * @param filePath 文件路径
     * @throws IOException 文件读取异常
     */
    public void loadFromFile(Path filePath) throws IOException {
        source = Files.readString(filePath);
        compile();
    }

    /**
     * 从源码加载着色器
     * @param source 源码
     */
    public void loadFromSource(String source) {
        this.source = source;
        compile();
    }

    /**
     * 编译着色器
     */
    protected void compile() {
        shaderId = GL20.glCreateShader(type.getGlType());
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);
        
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            String error = GL20.glGetShaderInfoLog(shaderId);
            logger.error("Shader compilation failed: {}", error);
            throw new RuntimeException("Shader compilation failed: " + error);
        }
    }

    /**
     * 删除着色器
     */
    public void delete() {
        if (shaderId != 0) {
            GL20.glDeleteShader(shaderId);
            shaderId = 0;
        }
    }

    /**
     * 获取着色器ID
     * @return 着色器ID
     */
    public int getShaderId() {
        return shaderId;
    }

    /**
     * 获取着色器类型
     * @return 着色器类型
     */
    public ShaderType getType() {
        return type;
    }
} 