// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("drawable");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("drawable")
//      }
//    }

#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "ImageProcessing"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static int rgb_clamp(int value) {
    if(value > 255) {
        return 255;
    }
    if(value < 0) {
        return 0;
    }
    return value;
}

static void blur(AndroidBitmapInfo* bitmap, void* pixels){
    int xx, yy, red, green, blue;
    uint32_t* line;

    for(yy = 0; yy < bitmap->height; yy++){
        line = (uint32_t*)pixels;
        for(xx =0; xx < bitmap->width; xx++){

            //extract the RGB values from the pixel
            red = (int) ((line[xx] & 0x00FF0000) >> 16);
            green = (int)((line[xx] & 0x0000FF00) >> 8);
            blue = (int) (line[xx] & 0x00000FF );

//            //manipulate each value
//            red = rgb_clamp((int)(red * brightnessValue));
//            green = rgb_clamp((int)(green * brightnessValue));
//            blue = rgb_clamp((int)(blue * brightnessValue));

            // set the new pixel back in
            line[xx] =
                    ((red << 16) & 0x00FF0000) |
                    ((green << 8) & 0x0000FF00) |
                    (blue & 0x000000FF);
        }

        pixels = (char*)pixels + bitmap->stride;
    }
}

static void brightness(AndroidBitmapInfo* info, void* pixels, float brightnessValue){
    int xx, yy, red, green, blue;
    uint32_t* line;

    for(yy = 0; yy < info->height; yy++){
        line = (uint32_t*)pixels;
        for(xx =0; xx < info->width; xx++){

            red = (int) ((line[xx] >> 16) & 0xFF);
            green = (int) ((line[xx] >> 8) & 0xFF);
            blue = (int) (line[xx] & 0xFF);

            //manipulate each value
            red = rgb_clamp((int)(red * (1 + brightnessValue)));
            green = rgb_clamp((int)(green * (1 + brightnessValue)));
            blue = rgb_clamp((int)(blue * (1 + brightnessValue)));

            // set the new pixel back in
            line[xx] =
                    ((red << 16) & 0x00FF0000) |
                    ((green << 8) & 0x0000FF00) |
                    (blue & 0x000000FF) |
                    (line[xx] & 0xFF000000); // Preserve alpha channel
        }

        pixels = (char*)pixels + info->stride;
    }
}

static void invertColors(AndroidBitmapInfo* info, void* pixels) {
    int xx, yy, red, green, blue;
    uint32_t* line;

    for(yy = 0; yy < info->height; yy++){
        line = (uint32_t*)pixels;
        for(xx =0; xx < info->width; xx++){

            red = (int) ((line[xx] >> 16) & 0xFF);
            green = (int) ((line[xx] >> 8) & 0xFF);
            blue = (int) (line[xx] & 0xFF);

            // Check if the pixel is not fully transparent (alpha channel not 0)
            if ((line[xx] >> 24) != 0) {
                // Invert the colors
                red = 255 - red;
                green = 255 - green;
                blue = 255 - blue;
            }

            // Set the new pixel back in
            line[xx] =
                    ((red << 16) & 0x00FF0000) |
                    ((green << 8) & 0x0000FF00) |
                    (blue & 0x000000FF) |
                    (line[xx] & 0xFF000000); // Preserve alpha channel
        }

        pixels = (char*)pixels + info->stride;
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_drawable_DrawableViewModelKt_brightness(JNIEnv *env, jclass clazz, jobject bmp,
                                                         jfloat brightnessValue) {
    AndroidBitmapInfo  info;
    int ret;
    void* pixels;

    if ((ret = AndroidBitmap_getInfo(env, bmp, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bmp, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    // blur(&info,pixels);
    brightness(&info, pixels, brightnessValue);

    AndroidBitmap_unlockPixels(env, bmp);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_drawable_DrawableViewModelKt_invertColors(JNIEnv *env, jclass clazz, jobject bmp) {

    AndroidBitmapInfo  info;
    int ret;
    void* pixels;

    if ((ret = AndroidBitmap_getInfo(env, bmp, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bmp, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    invertColors(&info, pixels);

    AndroidBitmap_unlockPixels(env, bmp);
}