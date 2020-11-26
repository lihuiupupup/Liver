precision highp float;
uniform sampler2D u_samplerTexture;
uniform vec2 singleStepOffset;
const highp vec4 params = vec4(0.6, 0.8, 0.25, 0.25);
varying highp vec2 v_texturecoord;
const highp vec3 W = vec3(0.299,0.587,0.114);
const mat3 saturateMatrix = mat3(
         1.1102,-0.0598,-0.061,
         -0.0774,1.0826,-0.1186,
         -0.0228,-0.0228,1.1772);
float hardlight(float color)
{
    if(color <= 0.5)
    {
        color = color * color * 2.0;
    }
    else
    {
        color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);
    }
    return color;
}
void main(){
    vec2 blurCoordinates[24];
    blurCoordinates[0] = v_texturecoord.xy + singleStepOffset * vec2(0.0, -10.0);
    blurCoordinates[1] = v_texturecoord.xy + singleStepOffset * vec2(0.0, 10.0);
    blurCoordinates[2] = v_texturecoord.xy + singleStepOffset * vec2(-10.0, 0.0);
    blurCoordinates[3] = v_texturecoord.xy + singleStepOffset * vec2(10.0, 0.0);
    blurCoordinates[4] = v_texturecoord.xy + singleStepOffset * vec2(5.0, -8.0);
    blurCoordinates[5] = v_texturecoord.xy + singleStepOffset * vec2(5.0, 8.0);
    blurCoordinates[6] = v_texturecoord.xy + singleStepOffset * vec2(-5.0, 8.0);
    blurCoordinates[7] = v_texturecoord.xy + singleStepOffset * vec2(-5.0, -8.0);
    blurCoordinates[8] = v_texturecoord.xy + singleStepOffset * vec2(8.0, -5.0);
    blurCoordinates[9] = v_texturecoord.xy + singleStepOffset * vec2(8.0, 5.0);
    blurCoordinates[10] = v_texturecoord.xy + singleStepOffset * vec2(-8.0, 5.0);
    blurCoordinates[11] = v_texturecoord.xy + singleStepOffset * vec2(-8.0, -5.0);
    blurCoordinates[12] = v_texturecoord.xy + singleStepOffset * vec2(0.0, -6.0);
    blurCoordinates[13] = v_texturecoord.xy + singleStepOffset * vec2(0.0, 6.0);
    blurCoordinates[14] = v_texturecoord.xy + singleStepOffset * vec2(6.0, 0.0);
    blurCoordinates[15] = v_texturecoord.xy + singleStepOffset * vec2(-6.0, 0.0);
    blurCoordinates[16] = v_texturecoord.xy + singleStepOffset * vec2(-4.0, -4.0);
    blurCoordinates[17] = v_texturecoord.xy + singleStepOffset * vec2(-4.0, 4.0);
    blurCoordinates[18] = v_texturecoord.xy + singleStepOffset * vec2(4.0, -4.0);
    blurCoordinates[19] = v_texturecoord.xy + singleStepOffset * vec2(4.0, 4.0);
    blurCoordinates[20] = v_texturecoord.xy + singleStepOffset * vec2(-2.0, -2.0);
    blurCoordinates[21] = v_texturecoord.xy + singleStepOffset * vec2(-2.0, 2.0);
    blurCoordinates[22] = v_texturecoord.xy + singleStepOffset * vec2(2.0, -2.0);
    blurCoordinates[23] = v_texturecoord.xy + singleStepOffset * vec2(2.0, 2.0);
    
    float sampleColor = texture2D(u_samplerTexture, v_texturecoord).g * 22.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[0]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[1]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[2]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[3]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[4]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[5]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[6]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[7]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[8]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[9]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[10]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[11]).g;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[12]).g * 2.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[13]).g * 2.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[14]).g * 2.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[15]).g * 2.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[16]).g * 2.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[17]).g * 2.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[18]).g * 2.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[19]).g * 2.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[20]).g * 3.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[21]).g * 3.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[22]).g * 3.0;
    sampleColor += texture2D(u_samplerTexture, blurCoordinates[23]).g * 3.0;
    sampleColor = sampleColor / 62.0;
    
    vec3 centralColor = texture2D(u_samplerTexture, v_texturecoord).rgb;
    float highpass = centralColor.g - sampleColor + 0.5;
    for(int i = 0; i < 5;i++)
    {
        highpass = hardlight(highpass);
    }
    float lumance = dot(centralColor, W);
    float alpha = pow(lumance, params.r);
    vec3 smoothColor = centralColor + (centralColor-vec3(highpass))*alpha*0.1;
    smoothColor.r = clamp(pow(smoothColor.r, params.g),0.0,1.0);
    smoothColor.g = clamp(pow(smoothColor.g, params.g),0.0,1.0);
    smoothColor.b = clamp(pow(smoothColor.b, params.g),0.0,1.0);
    vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);
    vec3 bianliang = max(smoothColor, centralColor);
    vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;
    gl_FragColor = vec4(mix(centralColor, lvse, alpha), 1.0);
    gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, alpha);
    gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, params.b);
    vec3 satcolor = gl_FragColor.rgb * saturateMatrix;
    gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, params.a);
}