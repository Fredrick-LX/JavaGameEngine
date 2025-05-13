package com.hmengine;

import com.hmengine.geometry.Mesh;
import java.util.ArrayList;
import java.util.List;

/**
 * 场景类
 */
public class Scene {
    private List<Mesh> meshes;

    /**
     * 构造函数
     */
    public Scene() {
        this.meshes = new ArrayList<>();
    }

    /**
     * 添加网格
     * @param mesh 网格
     */
    public void addMesh(Mesh mesh) {
        meshes.add(mesh);
    }

    /**
     * 移除网格
     * @param mesh 网格
     */
    public void removeMesh(Mesh mesh) {
        meshes.remove(mesh);
    }

    /**
     * 获取网格列表
     * @return 网格列表
     */
    public List<Mesh> getMeshes() {
        return meshes;
    }

    /**
     * 清除网格
     */
    public void clear() {
        meshes.clear();
    }
} 