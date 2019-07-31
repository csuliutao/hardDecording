#version 300 es
layout(location = 0) in vec4 v_position;
layout(location = 1) uniform mat4 v_martix;
layout(location = 2) in vec2 v_texCoord;

out vec2 f_texCoord;

void main() {
    gl_Position =  v_martix * v_position;
    f_texCoord = v_texCoord;
}
