#version 300 es
layout(location = 2) uniform sampler2D f_unit;
layout(location = 3) uniform float f_red;
layout(location = 4) uniform float f_green;
layout(location = 5) uniform float f_blue;
layout(location = 6) uniform float f_alpha;

in vec2 f_coord;
out vec4 o_fragColor;

void main() {
    o_fragColor = texture(f_unit, f_coord);
    o_fragColor.r = o_fragColor.r * f_red;
    o_fragColor.g = o_fragColor.g * f_green;
    o_fragColor.b = o_fragColor.b * f_blue;
    o_fragColor.a = o_fragColor.a * f_alpha;
}
