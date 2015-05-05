#version 330 core

layout(location = 0) in vec3 position_modelspace;

uniform mat4 M = mat4(1.0f);
uniform mat4 V = mat4(1.0f);
uniform mat4 P = mat4(1.0f);

void main()
{
	gl_Position = P * V * M * (vec4(position_modelspace, 1));
}