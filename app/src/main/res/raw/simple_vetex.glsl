#version 300 es

layout(location = 0) in vec4 v_position;
layout(location = 1) in vec4 v_color;
layout(location = 2) in float v_size;
layout(location = 3) uniform mat4 v_Matrix;

out vec4 f_color;

void main() {
    f_color = v_color;
    gl_Position = v_Matrix * v_position;
    gl_PointSize = v_size;
}
