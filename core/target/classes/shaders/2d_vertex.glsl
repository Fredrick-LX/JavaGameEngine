#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec2 aOffset;  // 实例化位置偏移
layout (location = 3) in vec4 aColor;
layout (location = 4) in float aSize;  // 正方形大小
layout (location = 5) in float aTextureIndex;

out vec2 v_TexCoord;
out vec4 v_Color;
out float v_TextureIndex;

void main() {
    vec2 finalPos = (aPos * aSize) + aOffset;  // 应用大小缩放
    gl_Position = vec4(finalPos, 0.0, 1.0);
    v_TexCoord = aTexCoord;
    v_Color = aColor;
    v_TextureIndex = aTextureIndex;
} 