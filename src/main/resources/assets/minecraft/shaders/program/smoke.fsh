
#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
in vec2 oneTexel;
out vec4 fragColor;

uniform vec2 InSize;
uniform float alpha1;

uniform float quality;
uniform float radius;
uniform float divider;
uniform float maxSample;

uniform vec2 resolution;
uniform float time;

uniform vec4 first;
uniform vec3 second;
uniform vec3 third;

uniform int oct;

float glowShader() {

    vec2 texelSize = vec2(1.0 / resolution.x * (radius * quality), 1.0 / resolution.y * (radius * quality));
    float alpha = 0;

    for (float x = -radius; x < radius; x++) {
        for (float y = -radius; y < radius; y++) {
            vec4 currentColor = texture(DiffuseSampler, texCoord + vec2(texelSize.x * x, texelSize.y * y));

            if (currentColor.a != 0)
            alpha += divider > 0 ? max(0.0, (maxSample - distance(vec2(x, y), vec2(0))) / divider) : 1;
        }
    }

    return alpha;
}

float random (in vec2 _st) {
    return fract(sin(dot(_st.xy, vec2(12.9898,78.233)))*43758.5453123);
}

float noise (in vec2 _st) {
    vec2 i = floor(_st);
    vec2 f = fract(_st);
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a)* u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm( in vec2 _st) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5),
    -sin(0.5), cos(0.50));
    for (int i = 0; i < oct; ++i) {
        v += a * noise(_st);
        _st = rot * _st * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}


vec3 getColor(vec4 centerCol) {
    vec2 st = gl_FragCoord.xy / resolution.xy*3.;
    vec3 color = vec3(0.0);
    vec2 q = vec2(0.);
    q.x = fbm(st);
    q.y = fbm( st + vec2(1.0));
    vec2 r = vec2(0.);
    r.x = fbm( st + 1.0*q + vec2(1.7,9.2)+ 0.15*time );
    r.y = fbm( st + 1.0*q + vec2(8.3,2.8)+ 0.126*time);
    float f = fbm(st+r);
    color = vec3(first[0],first[1],first[2]);
    color = mix(color, vec3(second[0],second[1],second[2]), clamp(length(q),0.0,1.0));
    color = mix(color, vec3(third[0],third[1],third[2]), clamp(length(r.x),0.0,1.0));
    vec4 outputLol = vec4((f*f*f+.6*f*f+.5*f)*color,first[3]);
    return vec3(outputLol[0], outputLol[1], outputLol[2]);
}

void main() {
    vec4 centerCol = texture(DiffuseSampler, texCoord);

    if (centerCol.a != 0) {
        fragColor = vec4(getColor(centerCol), alpha1);
    } else {
        fragColor = vec4(getColor(centerCol), glowShader());
    }
}




