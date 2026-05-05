#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MaskSampler;
uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;

void main(){
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec4 mask = texture(MaskSampler, texCoord);

    if (mask.a <= 0.0) {
        fragColor = scene;
        return;
    }

    vec2 texel = 1.0 / OutSize;

    //Standard luminence vec3 used here
    float tl = dot(texture(DiffuseSampler, texCoord + vec2(-texel.x, -texel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float t  = dot(texture(DiffuseSampler, texCoord + vec2(0.0,     -texel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float tr = dot(texture(DiffuseSampler, texCoord + vec2( texel.x, -texel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float l  = dot(texture(DiffuseSampler, texCoord + vec2(-texel.x,  0.0    )).rgb, vec3(0.299, 0.587, 0.114));
    float r  = dot(texture(DiffuseSampler, texCoord + vec2( texel.x,  0.0    )).rgb, vec3(0.299, 0.587, 0.114));
    float bl = dot(texture(DiffuseSampler, texCoord + vec2(-texel.x,  texel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float b  = dot(texture(DiffuseSampler, texCoord + vec2(0.0,       texel.y)).rgb, vec3(0.299, 0.587, 0.114));
    float br = dot(texture(DiffuseSampler, texCoord + vec2( texel.x,  texel.y)).rgb, vec3(0.299, 0.587, 0.114));

    float sobelX = -tl - 2.0*l - bl + tr + 2.0*r + br;
    float sobelY = -tl - 2.0*t - tr + bl + 2.0*b + br;
    float edge = clamp(sqrt(sobelX*sobelX + sobelY*sobelY) * 2.0, 0.0, 1.0);

    // Blueprint blue
    vec3 blueprintBlue = vec3(0.05, 0.15, 0.5);
    vec3 color = blueprintBlue;

    color = mix(color, vec3(1.0), edge);

    fragColor = vec4(color, scene.a);
}
