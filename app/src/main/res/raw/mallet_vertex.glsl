#version 300 es
layout(location = 0) uniform mat4 v_matrix;
layout(location = 1) in vec4 v_pos;
layout(location = 2) in vec4 v_color;
layout(location = 3) in float v_size;

out vec4 f_color;

void main() {
    gl_Position = v_matrix * v_pos;
    f_color = v_color;
    gl_PointSize = v_size;
}
