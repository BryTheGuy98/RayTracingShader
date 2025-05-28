#version 430

layout (local_size_x=1) in;
layout (binding=0, rgba8) uniform image2D output_tex;
float camera_pos_z = 5.0;

struct Ray
{	vec3 start;		// origin of the ray
	vec3 dir;		// normalized direction of the ray
};

struct Collision
{	float t;			// distance along ray at which the collision occurs
	vec3 p;				// world position of the collision
	vec3 n;				// surface normal at the collision
	bool inside;		// whether the ray started inside the object and collided without exiting
	int object_index;	// index of the object this collision hit
};

float sphere_radius = 2.5;
vec3 sphere_position = vec3(1.0, 0.0, -3.0);
vec3 sphere_color = vec3(0.0, 0.0, 1.0);	// blue

vec3 box_mins = vec3(-2.0, -2.0, 0.0);
vec3 box_maxs = vec3(-0.5, 1.0, 2.0);
vec3 box_color = vec3(1.0, 0.0, 0.0); 	// read

vec4 global_amb = vec4(0.3, 0.3, 0.3, 1.0);
vec3 pointLight_pos = vec3(-3.0, 2.0, 4.0);
vec4 pointLight_amb = vec4(0.2, 0.2, 0.2, 1.0);
vec4 pointLight_diff = vec4(0.7, 0.7, 0.7, 1.0);
vec4 pointLight_spec = vec4(1.0, 1.0, 1.0, 1.0);

vec4 objMat_amb = vec4(0.2, 0.2, 0.2, 1.0);
vec4 objMat_diff = vec4(0.7, 0.7, 0.7, 1.0);
vec4 objMat_spec = vec4(1.0, 1.0, 1.0, 1.0);
float objMat_shin = 50.0f;

// check if the ray intersects with the box
Collision intersect_box_object(Ray r)
{ 	// calculate box's world mins and maxs
	vec3 t_min = (box_mins - r.start) / r.dir;
	vec3 t_max = (box_maxs - r.start) / r.dir;
	vec3 t_minDist = min(t_min, t_max);
	vec3 t_maxDist = max(t_min, t_max);
	float t_near = max(max(t_minDist.x, t_minDist.y), t_minDist.z);
	float t_far = min(min(t_maxDist.x, t_maxDist.y), t_maxDist.z);
	
	Collision c;
	c.t = t_near;
	c.inside = false;
	
	// if the ray doesn't intersect with the box, return a negative t value
	if (t_near >= t_far || t_far <= 0.0)
	{	c.t = -1.0;
		return c;
	}
	
	float intersect_distance = t_near;
	vec3 plane_intersect_distances = t_minDist;
	
	// if t_near < 0, then the ray started inside the box and left the box
	if (t_near < 0.0)
	{	c.t = t_far;
		intersect_distance = t_far;
		plane_intersect_distances = t_maxDist;
		c.inside = true;
	}
	
	// check which boundary the intersection lies on
	int face_index = 0;
	if (intersect_distance == plane_intersect_distances.y) face_index = 1;
	else if (intersect_distance == plane_intersect_distances.z) face_index = 2;
	
	// create the collision normal
	c.n = vec3(0.0);
	c.n[face_index] = 1.0;
	
	// if we hit the box from the negative axis, invert the normal
	if (r.dir[face_index] > 0.0) c.n *= -1.0;
	
	// calculate the world-position of the intersection
	c.p = r.start + c.t * r.dir;
	return c;
}

// check if the ray intersects the sphere
Collision intersect_sphere_object(Ray r)
{	float qa = dot(r.dir, r.dir);
	float qb = dot(2*r.dir, r.start - sphere_position);
	float qc = dot(r.start - sphere_position, r.start - sphere_position) - sphere_radius * sphere_radius;
	
	// solve for qa * t^2 + qb * t + qc = 0
	float qd = qb * qb - 4 * qa * qc;
	
	Collision c;
	c.inside = false;
	
	if (qd < 0.0)	// no solution
	{	c.t = -1.0;
		return c;
	}
	
	float t1 = (-qb + sqrt(qd)) / (2.0 * qa);
	float t2 = (-qb - sqrt(qd)) / (2.0 * qa);
	float t_near = min(t1, t2);
	float t_far = max(t1, t2);
	c.t = t_near;
	
	if (t_far < 0.0)	// sphere is behind the ray, no intersection
	{	c.t = -1.0;
		return c;
	}
	
	if (t_near < 0.0)	// ray is inside the sphere
	{	c.t = t_far;
		c.inside = true;
	}
	
	c.p = r.start + c.t * r.dir;			// world position of the collision
	c.n = normalize(c.p - sphere_position);	// use the world position ot compute the surface normal
	if (c.inside) c.n *= -1.0;				// flip normal if inside the sphere
	return c;
}

// object_index == -1: no collision
// object_index == 1: collided with sphere
// object_index == 2: collided with box
Collision get_closest_collision(Ray r)
{	Collision closest_collision, cSph, cBox;
	closest_collision.object_index = -1;
	
	cSph = intersect_sphere_object(r);
	cBox = intersect_box_object(r);
	
	if ((cSph.t > 0) && ((cSph.t < cBox.t) || (cBox.t < 0)))
	{	closest_collision = cSph;
		closest_collision.object_index = 1;
	}
	if ((cBox.t > 0) && ((cBox.t < cSph.t) || (cSph.t < 0)))
	{	closest_collision = cBox;
		closest_collision.object_index = 2;
	}
	return closest_collision;
}

vec3 ads_phong_lighting(Ray r, Collision c)
{	// compute the ambient contribution from ambient and positional lights
	vec4 amb = global_amb + pointLight_amb * objMat_amb;
	
	vec4 diff = vec4(0.0);
	vec4 spec = vec4(0.0);
	
	Ray light_ray;
	light_ray.start = c.p + c.n * 0.01;
	light_ray.dir = normalize(pointLight_pos - c.p);
	bool in_shadow = false;
	
	Collision c_shadow = get_closest_collision(light_ray);
	if (c_shadow.object_index != -1 && c_shadow.t < length(pointLight_pos - c.p)) in_shadow = true;
	
	// compute light's reflection on the surface
	if (in_shadow == false)
	{	vec3 light_dir = normalize(pointLight_pos - c.p);
		vec3 light_ref = normalize(reflect(-light_dir, c.n));
		float cos_theta = dot(light_dir, c.n);
		float cos_phi = dot(normalize(-r.dir), light_ref);
	
		// compute diffuse and specular contributions
		diff = pointLight_diff * objMat_diff * max(cos_theta, 0.0);
		spec = pointLight_spec * objMat_spec * pow(max(cos_phi, 0.0), objMat_shin);
	}
	
	vec4 phong_color = amb + diff + spec;
	return phong_color.rgb;
}

vec3 raytrace(Ray r)
{	Collision c = get_closest_collision(r);
	if (c.object_index == -1) return vec3(0.0);
	if (c.object_index == 1) return ads_phong_lighting(r, c) * sphere_color;
	if (c.object_index == 2) return ads_phong_lighting(r, c) * box_color;
}

void main(void)
{	int width = int(gl_NumWorkGroups.x);
	int height = int(gl_NumWorkGroups.y);
	ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
	
	// convert pixel's screen space location to world space
	float x_pixel = 2.0 * pixel.x / width - 1.0;
	float y_pixel = 2.0 * pixel.y / height - 1.0;
	
	// get the pixel's world-space ray
	Ray world_ray;
	world_ray.start = vec3(0.0, 0.0, camera_pos_z);
	vec4 world_ray_end = vec4(x_pixel, y_pixel, camera_pos_z - 1.0, 1.0);
	world_ray.dir = normalize(world_ray_end.xyz - world_ray.start);
	
	// cast the ray into the world and intersect the ray with objects
	vec3 color = raytrace(world_ray);
	imageStore(output_tex, pixel, vec4(color, 1.0));
}