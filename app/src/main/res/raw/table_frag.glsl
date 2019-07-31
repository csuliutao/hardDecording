#version 300 es
precision mediump float;

in vec2 f_texCoord;
layout(location = 3) uniform sampler2D f_texUnit;

out vec4 o_fragColor;

void main() {
    o_fragColor = texture(f_texUnit, f_texCoord);
}
