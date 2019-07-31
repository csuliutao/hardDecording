#version 300 es
precision mediump float;

in vec4 f_color;

out vec4 o_fragColor;

void main() {
    o_fragColor = f_color;
}
