package com.hmengine.renderer;

import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureManager {
    private static Map<String, Integer> textureMap = new HashMap<>();
    private static final int TEXTURE_SIZE = 32; // 纹理大小
    private static int currentTextureUnit = 0;

    public static void init() {
        // 创建纹理时重置状态
        cleanup();
        
        // 创建基本地形纹理（按照优先级顺序创建）
        createTextTexture("mountain", "山", new Color(139, 69, 19));  // 棕色山地
        createTextTexture("forest", "树", new Color(34, 139, 34));    // 深绿色森林
        createTextTexture("water", "水", new Color(65, 105, 225));    // 蓝色水域
        createTextTexture("floor", "地", new Color(169, 169, 169));   // 灰色平地
    }

    private static void createTextTexture(String name, String text, Color color) {
        // 创建图像
        BufferedImage image = new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置渲染质量
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 设置背景为透明
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // 设置文字颜色和字体
        g2d.setColor(color);
        g2d.setFont(new Font("Dialog", Font.BOLD, TEXTURE_SIZE - 4));  // 使用粗体
        
        // 计算文字位置使其居中
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (TEXTURE_SIZE - metrics.stringWidth(text)) / 2;
        int y = ((TEXTURE_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
        
        // 绘制文字
        g2d.drawString(text, x, y);
        g2d.dispose();

        // 将图像转换为ByteBuffer
        ByteBuffer buffer = MemoryUtil.memAlloc(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        for (int i = TEXTURE_SIZE-1; i >= 0; i--) {
            for (int j = 0; j < TEXTURE_SIZE; j++) {
                int pixel = image.getRGB(j, i);
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();

        // 创建OpenGL纹理
        int textureID = glGenTextures();
        
        // 绑定到当前纹理单元
        glActiveTexture(GL_TEXTURE0 + currentTextureUnit);
        glBindTexture(GL_TEXTURE_2D, textureID);
        
        // 设置纹理参数
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        // 上传纹理数据
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, TEXTURE_SIZE, TEXTURE_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        
        // 存储纹理单元索引
        textureMap.put(name, currentTextureUnit);
        
        // 移动到下一个纹理单元
        currentTextureUnit++;
        
        // 释放资源
        MemoryUtil.memFree(buffer);
    }

    public static int getTexture(String name) {
        return textureMap.getOrDefault(name, textureMap.get("floor"));
    }

    public static void cleanup() {
        // 清理所有纹理
        for (int textureUnit : textureMap.values()) {
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        textureMap.clear();
        currentTextureUnit = 0;
    }
} 