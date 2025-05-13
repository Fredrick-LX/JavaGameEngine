package com.hmengine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * 窗口类
 */
public class Window {
    private long window;
    private int width;
    private int height;
    private String title;
    private boolean resizable;
    private boolean[] keyStates = new boolean[GLFW_KEY_LAST + 1];

    /**
     * 构造函数
     * @param width 宽度
     * @param height 高度
     * @param title 标题
     * @param resizable 是否可调整大小
     */
    public Window(int width, int height, String title, boolean resizable) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.resizable = resizable;
    }

    /**
     * 初始化
     */
    public void init() {
        // 设置错误回调
        GLFWErrorCallback.createPrint(System.err).set();

        // 初始化GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("无法初始化GLFW");
        }

        // 配置GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);

        // 创建窗口
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("无法创建GLFW窗口");
        }

        // 设置窗口回调
        setupCallbacks();

        // 居中显示窗口
        centerWindow();

        // 创建OpenGL上下文
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // 初始化OpenGL
        GL.createCapabilities();
    }

    /**
     * 设置回调
     */
    private void setupCallbacks() {
        glfwSetKeyCallback(window, (window, key, _, action, _) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }

            // 更新按键状态
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                if (action == GLFW_PRESS) {
                    keyStates[key] = true;
                } else if (action == GLFW_RELEASE) {
                    keyStates[key] = false;
                }
            }
        });
    }

    /**
     * 检查按键是否被按下
     * @param key 按键
     * @return 是否被按下
     */
    public boolean isKeyPressed(int key) {
        return key >= 0 && key <= GLFW_KEY_LAST && keyStates[key];
    }

    /**
     * 居中显示窗口
     */
    private void centerWindow() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2);
        }
    }

    /**
     * 检查窗口是否应该关闭
     * @return 是否应该关闭
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    /**
     * 更新
     */
    public void update() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    /**
     * 清理
     */
    public void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * 获取窗口句柄
     * @return 窗口句柄
     */
    public long getWindowHandle() {
        return window;
    }

    /**
     * 获取宽度
     * @return 宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * 获取高度
     * @return 高度
     */
    public int getHeight() {
        return height;
    }
}