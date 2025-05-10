package com.hmengine;

import com.hmengine.geometry.Mesh;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Mesh> meshes;

    public Scene() {
        this.meshes = new ArrayList<>();
    }

    public void addMesh(Mesh mesh) {
        meshes.add(mesh);
    }

    public void removeMesh(Mesh mesh) {
        meshes.remove(mesh);
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public void clear() {
        meshes.clear();
    }
} 