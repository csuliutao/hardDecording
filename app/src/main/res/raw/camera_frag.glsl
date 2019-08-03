#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
layout(location = 3) uniform samplerExternalOES f_unit;

in vec2 f_coord;
out vec4 o_fragColor;

void main() {
    o_fragColor = texture(f_unit, f_coord);
}
