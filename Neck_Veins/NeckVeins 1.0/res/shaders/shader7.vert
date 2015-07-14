/* Author of this file: Simon Žagar, 2012, Ljubljana
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 * or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 */
varying vec3 normal;
varying vec4 position;
varying vec3 worldPos;

void main(){
	vec4 ambient = gl_FrontMaterial.ambient * gl_LightSource[0].ambient;
	vec4 globalAmbient = gl_FrontMaterial.ambient * gl_LightModel.ambient;
	gl_FrontColor=ambient+globalAmbient;

	normal = gl_Normal;
	position = ftransform();
	gl_Position = position;
	worldPos = gl_Vertex.xyz;
}



//intensity per fragment