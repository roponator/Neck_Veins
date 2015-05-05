#version 330

layout(location = 0) in vec3 location_modelspace;

out vec4 location_projectorspace;

uniform mat4 M = mat4(1.f);
uniform mat4 V_camera = mat4(1.f);
uniform mat4 P_camera = mat4(1.f);
uniform mat4 V_projector = mat4(1.f);
uniform mat4 P_projector = mat4(1.f);

void main()
{
	mat4 mvp_camera = P_camera * V_camera * M;
	mat4 mvp_projector = P_projector * V_projector * M;
	
	gl_Position = mvp_camera * (vec4(location_modelspace, 1));
	location_projectorspace = mvp_projector * (vec4(location_modelspace, 1));
}