#version 300 es
layout(location = 0) in vec4 v_pos;
layout(location = 1) in vec2 v_coord;

out vec2 f_coord;

void main() {
    gl_Position = v_pos;
    f_coord = v_coord;
}
