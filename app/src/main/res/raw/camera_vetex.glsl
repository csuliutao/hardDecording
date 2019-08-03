#version 300 es
layout(location = 0) in vec4 v_pos;
layout(location = 1) in vec4 v_coord;
layout(location = 2) uniform mat4 v_matrix;

out vec2 f_coord;

void main() {
    gl_Position= v_pos;
    f_coord = (v_matrix * v_coord).xy;
}
