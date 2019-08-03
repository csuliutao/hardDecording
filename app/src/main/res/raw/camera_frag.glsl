#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
layout(location = 3) uniform samplerExternalOES f_unit;

in vec2 f_coord;
out vec4 o_fragColor;

void main() {
    o_fragColor = texture(f_unit, f_coord);
    o_fragColor.r = o_fragColor.r * 0.8;
    o_fragColor.g = o_fragColor.g * 0.4;
    o_fragColor.b = o_fragColor.b * 0.7;
}