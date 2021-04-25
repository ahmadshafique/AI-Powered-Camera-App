#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

#include "syn-ef.h"

using namespace std;
using namespace cv;


extern "C"
JNIEXPORT void JNICALL
Java_com_fyp_aipoweredcameraapp_ActivityImage_synEFFromJNI(JNIEnv *env, jobject thiz,
                                                           jlong frame, jlong res) {
    Mat mprev = (Mat *) frame;
    Mat mres = (Mat *) res;

    cvtColor(mprev, mprev, COLOR_BGRA2RGB);

    synEF(mprev, mres);
    cvtColor(mres, mres, COLOR_RGB2BGR);
}
