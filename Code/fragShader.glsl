#version 430

layout (binding=0) uniform sampler2D tex;
in vec2 uv;
out vec4 color;

void main(void)
{	color = vec4(texture2D(tex, uv).rgb, 1.0);
}