/* Author of this file: Simon Žagar, 2012, Ljubljana
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 * or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 */
uniform vec4 bloodColor;
uniform vec3 lightDir;
varying vec3 normal;
varying vec4 position;

void main(){
/*	vec3 n=normalize(normal);
	vec3 l=normalize(vec3(gl_LightSource[0].position));
	float intensity = max(dot(l, n), 0.0);
	
	vec4 diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
	
	vec4 specular=vec4(0.0, 0.0, 0.0, 0.0);
	if(intensity>0.0){
		vec3 eye = normalize( vec3(position.xyz) );
		vec3 R= (2.0*dot(l, n)*n)-l;
		float cosAngle=dot(normalize(R), eye);
		if(cosAngle>0.0){
			specular = (gl_FrontMaterial.specular * gl_LightSource[0].specular) * pow(cosAngle, gl_FrontMaterial.shininess);
		}
	}*/
	
	
	//gl_FragColor = specular;
	
	vec3 n=normalize(normal);
	vec3 l1Vec = normalize(vec3(-1,-2,3));
	vec3 l2Vec = normalize(vec3(-2,1,-3));
	vec3 l3Vec = normalize(vec3(3,-2,-1));
	
	float dp = clamp(dot(n,l1Vec),0,1)+clamp(dot(n,l2Vec),0,1)+clamp(dot(n,l3Vec),0,1);
	dp /= 3.0f;
	gl_FragColor = vec4(0,1,1,1)*dp;
	gl_FragColor = vec4(n*0.5f+0.5f,1.0f);
}



//Phong lighting model