package com.hmengine.render.shader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Shader {
    private static final Logger logger = LoggerFactory.getLogger(Shader.class);
    
    protected int shaderId;
    protected final ShaderType type;
    protected String source;
    
    public enum ShaderType {
        VERTEX(GL20.GL_VERTEX_SHADER),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER),
        GEOMETRY(GL43.GL_GEOMETRY_SHADER),
        COMPUTE(GL43.GL_COMPUTE_SHADER);
        
        private final int glType;
        
        ShaderType(int glType) {
            this.glType = glType;
        }
        
        public int getGlType() {
            return glType;
        }
    }
    
    protected Shader(ShaderType type) {
        this.type = type;
    }
    
    public void loadFromFile(Path filePath) throws IOException {
        source = Files.readString(filePath);
        compile();
    }
    
    public void loadFromSource(String source) {
        this.source = source;
        compile();
    }
    
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
    
    public void delete() {
        if (shaderId != 0) {
            GL20.glDeleteShader(shaderId);
            shaderId = 0;
        }
    }
    
    public int getShaderId() {
        return shaderId;
    }
    
    public ShaderType getType() {
        return type;
    }
} 