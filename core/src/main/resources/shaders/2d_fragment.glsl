#version 330 core

in vec2 v_TexCoord;
in vec4 v_Color;
in float v_TextureIndex;

out vec4 FragColor;

uniform sampler2D u_Textures[32];

void main() {
    int textureIndex = int(v_TextureIndex);
    if (textureIndex >= 0) {
        vec4 texColor = texture(u_Textures[textureIndex], v_TexCoord);
        if (texColor.a < 0.1) {
            discard;
        }
        FragColor = texColor * v_Color;
    } else {
        FragColor = v_Color;
    }
} 