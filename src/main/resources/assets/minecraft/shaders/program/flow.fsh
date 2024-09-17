#version 150

#ifdef GL_ES
precision mediump float;
#endif
#define NUM_OCTAVES 16

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
uniform float mixFactor;
uniform float time;
uniform vec2 resolution;
uniform float quality;
uniform float radius;
uniform float divider;
uniform float maxSample;
out vec4 fragColor;

mat3 rotX(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat3(
        1, 0, 0,
        0, c, -s,
        0, s, c
    );
}
mat3 rotY(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat3(
        c, 0, -s,
        0, 1, 0,
        s, 0, c
    );
}

float random(vec2 pos) {
    return fract(sin(dot(pos.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

float noise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    float a = random(i + vec2(0.0, 0.0));
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 pos) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i=0; i<NUM_OCTAVES; i++) {
        v += a * noise(pos);
        pos = rot * pos * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

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

void main(void) {
    vec4 centerCol = texture(DiffuseSampler, texCoord);
    vec2 p = (gl_FragCoord.xy * 2.0 - resolution.xy) / min(resolution.x, resolution.y);

    float t = 0.0, d;
    float alpha = 0;

    float time2 = 3.0 * time / 2.0;

    vec2 q = vec2(0.0);
    q.x = fbm(p + 0.00 * time2);
    q.y = fbm(p + vec2(1.0));
    vec2 r = vec2(0.0);
    r.x = fbm(p + 1.0 * q + vec2(1.7, 9.2) + 0.15 * time2);
    r.y = fbm(p + 1.0 * q + vec2(8.3, 2.8) + 0.126 * time2);
    float f = fbm(p + r);
    vec3 color = mix(
        vec3(0.101961, 0.866667, 0.319608),
        vec3(.666667, 0.598039, 0.366667),
        clamp((f * f) * 4.0, 0.0, 1.0)
    );

    color = mix(
        color,
        vec3(0.34509803921, 0.06666666666, 0.83137254902),
        clamp(length(q), 0.0, 1.0)
    );


    color = mix(
        color,
        vec3(0.1, -0.5, 0.1),
        clamp(length(r.x), 0.0, 1.0)
    );

    color = (f *f * f + 0.6 * f * f + 0.5 * f) * color;

    if (centerCol.a != 0) {
        alpha = mixFactor;
    } else {
        alpha = glowShader();
        color *= 2;
    }

    fragColor = vec4(color, alpha);
}