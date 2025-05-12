package com.hmengine.text;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTBakedChar.Buffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.stb.STBTruetype.*;

public class Font {
    private final int textureId;
    private final Map<Character, CharInfo> charMap;
    private final int textureWidth;
    private final int textureHeight;
    private final FloatBuffer vertexBuffer;

    public Font(String fontFile, int fontSize) {
        this.charMap = new HashMap<>();
        this.vertexBuffer = MemoryUtil.memAllocFloat(6 * 4); // 6个顶点，每个顶点4个float（位置和纹理坐标）
        
        // 加载字体文件
        ByteBuffer ttfBuffer = loadFontFile(fontFile);
        
        // 创建纹理
        textureWidth = 2048;  // 增加纹理大小
        textureHeight = 2048;
        ByteBuffer bitmap = MemoryUtil.memAlloc(textureWidth * textureHeight);
        
        // 创建STBTTBakedChar缓冲区
        Buffer cdata = STBTTBakedChar.malloc(0x9FFF); // 扩展Unicode范围
        
        // 烘焙字体
        stbtt_BakeFontBitmap(ttfBuffer, fontSize, bitmap, textureWidth, textureHeight, 0, cdata);
        
        // 创建OpenGL纹理
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, textureWidth, textureHeight, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        // 释放资源
        MemoryUtil.memFree(bitmap);
        MemoryUtil.memFree(ttfBuffer);
        
        // 初始化字符信息
        initCharInfo(cdata);
        
        // 释放cdata
        cdata.free();
    }
    
    private ByteBuffer loadFontFile(String fontFile) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fontFile)) {
            if (is == null) {
                throw new RuntimeException("无法加载字体文件: " + fontFile);
            }
            
            byte[] bytes = is.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("加载字体文件时发生错误", e);
        }
    }
    
    private void initCharInfo(Buffer cdata) {
        // 初始化ASCII字符
        for (int i = 0; i < 96; i++) {
            STBTTBakedChar charData = cdata.get(i);
            charMap.put((char)(i + 32), new CharInfo(
                charData.x0() / (float)textureWidth,
                charData.y0() / (float)textureHeight,
                charData.x1() / (float)textureWidth,
                charData.y1() / (float)textureHeight,
                charData.xoff(),
                charData.yoff(),
                charData.xadvance()
            ));
        }
        
        // 初始化常用汉字
        for (int i = 0; i < 0x4E00; i++) {
            STBTTBakedChar charData = cdata.get(i);
            if (charData.x0() != 0 || charData.y0() != 0) {  // 只添加实际存在的字符
                charMap.put((char)(i), new CharInfo(
                    charData.x0() / (float)textureWidth,
                    charData.y0() / (float)textureHeight,
                    charData.x1() / (float)textureWidth,
                    charData.y1() / (float)textureHeight,
                    charData.xoff(),
                    charData.yoff(),
                    charData.xadvance()
                ));
            }
        }
    }
    
    public void renderText(String text, float x, float y, float scale) {
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            CharInfo charInfo = charMap.get(c);
            if (charInfo == null) continue;
            
            // 计算顶点坐标
            float x0 = x + charInfo.xoff() * scale;
            float y0 = y + charInfo.yoff() * scale;
            float x1 = x0 + (charInfo.x1() - charInfo.x0()) * textureWidth * scale;
            float y1 = y0 + (charInfo.y1() - charInfo.y0()) * textureHeight * scale;
            
            // 更新顶点缓冲区
            vertexBuffer.clear();
            
            // 第一个三角形
            // 左下
            vertexBuffer.put(x0).put(y1).put(charInfo.x0()).put(charInfo.y1());
            // 右下
            vertexBuffer.put(x1).put(y1).put(charInfo.x1()).put(charInfo.y1());
            // 右上
            vertexBuffer.put(x1).put(y0).put(charInfo.x1()).put(charInfo.y0());
            
            // 第二个三角形
            // 左下
            vertexBuffer.put(x0).put(y1).put(charInfo.x0()).put(charInfo.y1());
            // 右上
            vertexBuffer.put(x1).put(y0).put(charInfo.x1()).put(charInfo.y0());
            // 左上
            vertexBuffer.put(x0).put(y0).put(charInfo.x0()).put(charInfo.y0());
            
            vertexBuffer.flip();
            
            // 更新VBO数据
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
            
            // 绘制
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            x += charInfo.xadvance() * scale;
        }
    }
    
    public void cleanup() {
        glDeleteTextures(textureId);
        MemoryUtil.memFree(vertexBuffer);
    }
    
    private static class CharInfo {
        private final float x0, y0, x1, y1;
        private final float xoff, yoff, xadvance;
        
        public CharInfo(float x0, float y0, float x1, float y1, float xoff, float yoff, float xadvance) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.xoff = xoff;
            this.yoff = yoff;
            this.xadvance = xadvance;
        }
        
        public float x0() { return x0; }
        public float y0() { return y0; }
        public float x1() { return x1; }
        public float y1() { return y1; }
        public float xoff() { return xoff; }
        public float yoff() { return yoff; }
        public float xadvance() { return xadvance; }
    }
} 