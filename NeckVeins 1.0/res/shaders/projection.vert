#version 330

layout(location = 0) in vec3 location_modelspace;
layout(location = 1) in vec3 normal_modelspace;

out vec4 location_projectorspace;
out vec3 normal_projectorspace;

out vec3 lightDirection;

uniform mat4 M = mat4(1.f);
uniform mat4 V_camera = mat4(1.f);
uniform mat4 P_camera = mat4(1.f);
uniform mat4 V_projector = mat4(1.f);
uniform mat4 P_projector = mat4(1.f);

void main()
{
	mat4 MVP_camera = P_camera * V_camera * M;
	mat4 MVP_projector = P_projector * V_projector * M;
	
	gl_Position = MVP_camera * (vec4(location_modelspace, 1));
	
	location_projectorspace = MVP_projector * (vec4(location_modelspace, 1));
	
	normal_projectorspace = normalize((V_projector * M * vec4(normal_modelspace, 0)).xyz);

	//lightDirection = normalize((MVP_projector * vec4(0, 0, 1, 0)).xyz);
	lightDirection = vec3(0, 0, 1);
}