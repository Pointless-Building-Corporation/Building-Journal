#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MaskSampler;
uniform vec2 OutSize;

// Most of this shader's code is by hobbes from ShaderToy: https://www.shadertoy.com/view/ttlfzj

in vec2 texCoord;
out vec4 fragColor;

float size = 512.;
float threshold = .006;
mat2 dither_2 = mat2(0., 1., 1., 0.);

void gb_colors(out vec3 colors[4]) {
    colors[0] = vec3(15., 56., 15.)		/255.;
    colors[1] = vec3(48., 98., 48.)		/255.;
    colors[2] = vec3(139., 172., 15.)	/255.;
    colors[3] = vec3(155., 188., 15.)	/255.;
}

void gb_colors_distance(vec3 color, out float distances[4]) {
    vec3 colors[4];
    gb_colors(colors);

    distances[0] = distance(color, colors[0]);
    distances[1] = distance(color, colors[1]);
    distances[2] = distance(color, colors[2]);
    distances[3] = distance(color, colors[3]);
}

vec3 closest_gb(vec3 color) {
    int best_i = 0;
    float best_d = 2.;
    
    vec3 colors[4];
    gb_colors(colors);
    
    for (int i = 0; i < 4; i++) {
        float dis = distance(colors[i], color);;
        if (dis < best_d) {
            best_d = dis;
            best_i = i;
        }
    }
    
    return colors[best_i];
}

vec2 get_tile_sample(vec2 coords, vec2 res) {
    return floor(coords * res / 2.) * 2. / res;
}

void gb_2_closest(vec3 color, out vec3 result[2]) {
    float distances[4];
    gb_colors_distance(color, distances);
    
    int first_i = 0;
    float first_d = 2.;
    
    int second_i = 0;
    float second_d = 2.;
    
    for (int i = 0; i < distances.length(); i++) {
        float d = distances[i];
        if (distances[i] <= first_d) {
            second_i = first_i;
            second_d = first_d;
            first_i = i;
            first_d = d;
        } else if (distances[i] <= second_d) {
            second_i = i;
            second_d = d;
        }
    }
    vec3 colors[4];
    gb_colors(colors);

    if (first_i < second_i) {
        result[0] = colors[first_i];
        result[1] = colors[second_i];
    } else {
        result[0] = colors[second_i];
        result[1] = colors[first_i];
    }
}

bool needs_dither(vec3 color) {
    float distances[4];
    gb_colors_distance(color, distances);
    
    int first_i = 0;
    float first_d = 2.;
    
    int second_i = 0;
    float second_d = 2.;
    
    for (int i = 0; i < distances.length(); i++) {
        float d = distances[i];
        if (d <= first_d) {
            second_i = first_i;
            second_d = first_d;
            first_i = i;
            first_d = d;
        } else if (d <= second_d) {
            second_i = i;
            second_d = d;
        }
    }
    return abs(first_d - second_d) <= threshold;
}

struct dither_tile {
    float height;
};

void main(){
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec4 mask = texture(MaskSampler, texCoord);

    if (mask.a <= 0.0) {
        fragColor = scene;
        return;
    }

    vec2 resolution = vec2(size, OutSize.y / OutSize.x * size);
    vec2 uv = floor(texCoord * resolution) / resolution;

    vec2 tileSample = get_tile_sample(uv, resolution);
    vec3 sampleColor = scene.xyz;

     vec3 colors[2] = vec3[2](vec3(1.,1.,1.), vec3(0.,0.,0.));
    
    if (needs_dither(sampleColor)) {
        ivec2 ti = ivec2(floor((uv - tileSample) * 2. * resolution));
        vec3 closest[2];
        gb_2_closest(sampleColor, closest);
        fragColor = vec4(closest[int(dither_2[ti.x][ti.y])], 1.);
    }
    else {
        fragColor = vec4(closest_gb(texture(DiffuseSampler, uv).xyz),1.0);
    }
}

