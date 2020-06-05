#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <vector>
#include <math.h>
#include <algorithm>

#include "syn-ef.h"

using namespace std;
using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_try4_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_try4_MainActivity_frameFromJNI(
        JNIEnv *env,
        jobject /* this */,
        jlong frame) {
    // get Mat from raw address
    int i = 0;
    Mat &mframe = *(Mat *) frame;
    cvtColor(mframe, mframe, COLOR_RGBA2GRAY);
}

/*
void gamma(Mat *img, float g){
    g = 1.0/g;
    Mat lookUpTable(1, 256, CV_8U);
    uchar* p = lookUpTable.ptr();
    for( int i = 0; i < 256; ++i)
        p[i] = saturate_cast<uchar>(pow(i / 255.0, g) * 255.0);
    LUT(*img, lookUpTable, *img);
}


void extract(Mat* lum, int regions, Mat* labels){
    vector<uchar> lums = lum->reshape(0,1);
    //lums.assign(*lum->data, *lum->data + lum->total());
    sort(lums.begin(), lums.end());
    int diff = lums[lums.size()-1] - lums[0];
    int tmp_endpoints = (diff/regions);
    vector<float> temp;
    for(int i = 0; i < regions; i++){
        temp.push_back((tmp_endpoints*i)+lums[0]);
    }
    vector<uchar> table(256);
    for(int i = 0; i < 256; i++){
        for(int j = 0; j < regions; j++){
            int k = regions - j - 1;
            if(j == 0){
                if(i >= temp[k])
                    table[i] = j;
            }
            else{
                if(i >= temp[k] && i <temp[k+1])
                    table[i] = j;
            }

        }
    }

    LUT(*lum, table, *labels);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_try4_MainActivity_synEF_L_FromJNI(JNIEnv *env, jobject thiz, jlong frame, jlong res) {
    Mat &mprev = *(Mat *) frame;

    fastNlMeansDenoisingColored(mprev, mprev);
    gamma(&mprev, 2.2);
    Mat &mres = *(Mat *) res;
    Mat small = mprev;
    
    //resize(mprev, small, Size(), 0.1, 0.1);


    //mprev.convertTo(mprev, CV_32FC3, 1.f/255);
    Mat yuv;
    cvtColor(small, yuv, COLOR_RGB2YUV);

    Mat chan[3];
    split(yuv, chan);

    yuv.release();

    Mat lum;
    lum = chan[0];

    chan[1].release();
    chan[2].release();

    int regions = 7;
    Mat labels(lum.rows, lum.cols,CV_8UC1);


    //Generate Segments
    extract(&lum, regions, &labels);

    vector<double> a;
    vector<double> sums(regions, 0);
    vector<double> count(regions, 0);


    for(int i = 0; i < lum.rows; i++){
        for(int j=0; j < lum.cols; j++){
            double idx = (double)lum.at<uchar>(i,j);
            double eps = 0.003;
            double m = max(idx, eps);
            double v = log(m);
            sums[labels.at<uchar>(i,j)] += v;
            count[labels.at<uchar>(i,j)] += 1;
        }
    }

    labels.release();

    //Calculate enhancement factors
    for(int i = 0; i < regions; i++){
        double v = sums[i] / (count[i] + 0.003);
        a.push_back(0.18/exp(v));
    }


    cvtColor(mprev, yuv, COLOR_RGB2YUV);
    split(yuv, chan);
    yuv.release();
    lum.release();
    lum = chan[0];
    chan[1].release();
    chan[2].release();

    //apply enhancement factor
    vector<Mat> exp;
    for(int i =0; i < regions; i++){
        Mat temp;
        lum.convertTo(temp, CV_32F);
        temp = temp * a[i];
        exp.push_back(temp.clone());
    }

    //tone mapping
    for(int i = 0; i < regions; i++){
        double max_v;
        int temp;
        minMaxIdx(exp[i], nullptr,&max_v, nullptr, &temp);
        Mat t1 = exp[i] / max_v;
        t1 = t1 + 1;
        Mat t2 = exp[i] + 1;
        t2 = exp[i] / t2;
        Mat t3;
        t3 = t2.mul(t1);
        Mat t4;
        lum.convertTo(t4, CV_32F);
        Mat t5 = t3 / (t4 + 0.003);
        exp[i] = t5.clone();

    }


    Mat p;
    mprev.convertTo(p, CV_32F);
    vector<Mat> p_chan;
    split(p, p_chan);
    vector<Mat> pme;
    for(int i = 0; i < regions; i++){
        vector<Mat> temp(3);
        for(int j = 0; j < 3; j++){
            temp[j] = p_chan[j].mul(exp[i]);
        }
        exp[i].release();
        Mat temp2;
        merge(temp, temp2);
        Mat temp3;
        temp3 = temp2 * 255;
        temp3.convertTo(temp3, CV_8UC3);
        float f = temp2.at<float>(100,10);
        int ii = temp3.at<uchar>(100,10);
        double max_v;
        minMaxIdx(temp3, nullptr,&max_v, nullptr, nullptr);
        gamma(&temp3, 1.0/2.2);
        pme.push_back(temp3.clone());
    }

    gamma(&mprev, 1.0/2.2);
    p.release();
    for(int i = 0; i < 3; i++){
        p_chan[i].release();
    }


    //OpenCV Exposure Fusion
    Ptr<MergeMertens> merge = createMergeMertens();
    merge->process(pme, mres);
    mres.convertTo(mres, CV_32F);
    float f = mres.at<float>(100,10);
    mres = mres *255;
    mres.convertTo(mres, CV_8UC3);
    int f2 = mres.at<uchar>(100,10);
    cvtColor(mres, mres, COLOR_RGB2BGR);

}
*/

extern "C"
JNIEXPORT void JNICALL
Java_com_fyp_aipoweredcameraapp_ActivityImageSelection_synEFFromJNI(JNIEnv *env, jobject thiz, jlong frame, jlong res) {
    Mat &mprev = *(Mat *) frame;
    Mat &mres = *(Mat *) res;

    cvtColor(mprev, mprev, COLOR_BGRA2RGB);

    runner(mprev, mres);
    cvtColor(mres, mres, COLOR_RGB2BGR);
}