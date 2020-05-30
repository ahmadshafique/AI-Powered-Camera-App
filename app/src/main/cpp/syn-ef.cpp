#include <math.h>
#include <algorithm>
#include <opencv2/opencv.hpp>

#include "contrast_enhancement.h"
#include "guidedfilter.h"

#include"syn-ef.h"

using namespace std;
using namespace cv;


void temp() {
    Mat t = imread("imgs/before.jpg", 1);
    Mat mres = t.clone();
    vector<Mat> pme;
    for (int i = 0; i < 8; i++)
    {
        pme.push_back(t.clone());
    }
    cout << pme.size() << "-" << pme[0].size() << "-" << pme[0].channels() << endl;

    auto t1 = getTickCount() / getTickFrequency();

    Ptr<MergeMertens> merge = createMergeMertens();  //8imgs = 7.4s
    merge->process(pme, mres);

    //expo_fuse(pme, mres);  //8imgs = 7.2s

    auto t2 = getTickCount() / getTickFrequency();
    std::cout << "It took " << t2-t1 << "s" << std::endl;
}

void expo_fuse(vector<Mat> pme, Mat &res) {
    float r = 12, eps = 0.25, sig_l = 0.5, sig_g = 0.2, sig_d = 0.12, alpha = 1.1;
    vector<Mat> W_B, W_D, B, D;

    for (int i = 0; i < pme.size(); i++) {
        Mat img = pme[i].clone();
        img.convertTo(img, CV_32FC3);
        img = img / 255;
        Mat lum;
        cvtColor(img, lum, COLOR_RGB2GRAY, 1);
        double max_v;
        minMaxIdx(lum, nullptr, &max_v, nullptr, nullptr);

        Mat wl;
        float wg;
        Mat base = guidedFilter(lum, lum, r, eps);
        wl = (base - 0.5).mul(base - 0.5) / (-2 * sig_l*sig_l);
        float m = mean(lum)[0];
        wg = ((m - 0.5)*(m - 0.5)) / (-2 * sig_l*sig_l);
        exp(wl, wl);
        wg = exp(wg);
        W_B.push_back(wl * wg);
        B.push_back(base);

        wl.release();
        cvtColor(base, base, COLOR_GRAY2RGB);

        Mat detail = img - base;
        base.release();
        Mat kernel = Mat::ones(7, 7, CV_32FC1)*(1 / 49);
        Mat conved, wd;
        filter2D(lum, conved, -1, kernel);
        wd = (conved - 0.5).mul(conved - 0.5) / (-2 * sig_d*sig_d);
        exp(wd, wd);
        W_D.push_back(wd);
        D.push_back(detail);
    }

    Mat wb_s = W_B[0].clone(), wd_s = W_D[0].clone();
    for (int i = 1; i < pme.size(); i++) {
        add(wb_s, W_B[i], wb_s);
        add(wd_s, W_D[i], wd_s);
    }

    Mat dst = Mat::zeros(pme[0].size(), CV_32FC3);
    for (int i = 0; i < pme.size(); i++) {
        Mat wb, wd;
        divide(W_B[i], wb_s, wb);
        divide(W_D[i], wd_s, wd);

        cvtColor(wd, wd, COLOR_GRAY2RGB);
        multiply(wd, D[i], wd);
        wd *= alpha;

        multiply(wb, B[i], wb);
        cvtColor(wb, wb, COLOR_GRAY2RGB);

        dst += wd + wb;

        W_B[i].release(); W_D[i].release(); D[i].release(); B[i].release();
    }
    res = dst.clone();
}


void correction(Mat &img, const Mat &prev) {
    Mat mask;
    Mat hsv, hsv2;
    cvtColor(img, hsv, COLOR_RGB2HSV);
    cvtColor(prev, hsv2, COLOR_RGB2HSV);
    vector<Mat> chan, chan2;
    split(hsv, chan);
    split(hsv2, chan2);
    threshold(chan[2], mask, 10, 0, 1);
    threshold(mask, mask, 1, 1, 0);
    chan[2] = mask.mul(chan[2]) + chan2[2].mul(1 - mask);
    merge(chan, hsv);
    cvtColor(hsv, img, COLOR_HSV2RGB);
}


void gamma(Mat *img, float g) {
    Mat lookUpTable(1, 256, CV_8U);
    uchar* p = lookUpTable.ptr();
    for (int i = 0; i < 256; ++i)
        p[i] = saturate_cast<uchar>(pow(i / 255.0, g) * 255.0);
    LUT(*img, lookUpTable, *img);
}


void extract(Mat* lum, int regions, Mat* labels) {
    vector<uchar> lums = lum->reshape(0, 1);
    //lums.assign(*lum->data, *lum->data + lum->total());
    sort(lums.begin(), lums.end());
    int diff = lums[lums.size() - 1] - lums[0];
    int tmp_endpoints = (diff / regions);
    vector<float> temp;
    for (int i = 0; i < regions; i++) {
        temp.push_back((tmp_endpoints*i) + lums[0]);
    }
    vector<uchar> table(256);
    for (int i = 0; i < 256; i++) {
        for (int j = 0; j < regions; j++) {
            int k = regions - j - 1;
            if (j == 0) {
                if (i >= temp[k])
                    table[i] = j;
            }
            else {
                if (i >= temp[k] && i < temp[k + 1])
                    table[i] = j;
            }

        }
    }

    LUT(*lum, table, *labels);
}


void synEF(Mat *prev, Mat *res, float g) {
    Mat &mprev = *(Mat *) prev;
    Mat &mres = *(Mat *) res;

    gamma(&mprev, g);

    Mat small = mprev;

    resize(mprev, small, Size(), 0.05, 0.05);

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
    Mat labels(lum.rows, lum.cols, CV_8UC1);


    //Generate Segments
    extract(&lum, regions, &labels);

    vector<double> a;
    vector<double> sums(regions, 0);
    vector<double> count(regions, 0);

    lum += 1;

    for (int i = 0; i < lum.rows; i++) {
        for (int j = 0; j < lum.cols; j++) {
            double idx = (double)lum.at<uchar>(i, j);
            double eps = 0.003;
            double m = max(idx, eps);
            double v = log(m);
            sums[labels.at<uchar>(i, j)] += v;
            count[labels.at<uchar>(i, j)] += 1;
        }
    }


    labels.release();

    //Calculate enhancement factors
    for (int i = 0; i < regions; i++) {
        double v = sums[i] / (count[i] + 0.003);
        a.push_back(0.18 / exp(v));
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
    for (int i = 0; i < regions; i++) {
        Mat temp;
        lum.convertTo(temp, CV_32F);
        temp = temp * a[i];
        exp.push_back(temp.clone());
    }

    //tone mapping
    for (int i = 0; i < regions; i++) {
        double max_v;
        int temp;
        minMaxIdx(exp[i], nullptr, &max_v, nullptr, &temp);
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
    vector<Mat> temp(3);
    for (int i = 0; i < regions; i++) {
        for (int j = 0; j < 3; j++) {
            temp[j] = p_chan[j].mul(exp[i]);
        }
        exp[i].release();
        Mat temp2;
        merge(temp, temp2);
        Mat temp3;
        temp3 = temp2 * 255;
        temp3.convertTo(temp3, CV_8UC3);
        gamma(&temp3, 1.0/g);
        pme.push_back(temp3.clone());
    }
    pme.push_back(mprev.clone());

    p.release();
    for (int i = 0; i < 3; i++) {
        p_chan[i].release();
    }
    gamma(&mprev, 1.0/g);

    //OpenCV Exposure Fusion
    Ptr<MergeMertens> merge = createMergeMertens();
    merge->process(pme, mres);

    for (int i = 0; i < pme.size(); i++) {
        pme[i].release();
    }

    //expo_fuse(pme, mres);

    mres.convertTo(mres, CV_8UC3, 255, 0);

    if(g == 2.2)
        correction(mres, mprev);
}

bool is_dark(Mat img) {
    Mat temp;
    cvtColor(img, temp, COLOR_RGB2HSV);
    Scalar m = mean(temp);
    return m[2] <= 85;
}

bool is_hdr(Mat img) {
    Mat temp;
    cvtColor(img, temp, COLOR_RGB2HSV);
    Mat br, dr;
    threshold(temp, br, 127, 0, THRESH_TOZERO);
    threshold(temp, dr, 127, 0, THRESH_TOZERO_INV);
    Scalar m = mean(temp);
    vector<Mat> chan;
    split(br, chan);
    int total = br.rows * br.cols;
    int n = countNonZero(chan[2]);
    m = sum(br);
    if (float(n) / total < 0.3 || float(n) / total > 0.7)
        return false;
    m = sum(dr);
    return m[2] / (total - n) < 25;
}



void runner(Mat &prev, Mat &res) {
    Mat contr, temp, ptemp = prev.clone(), ctemp;
    float g = 2.2;
    bool hdr = is_hdr(ptemp);

    if(hdr) gamma(&ptemp, 1/g);
    contrastEnhancement(ptemp, contr);
    if(hdr) gamma(&contr, g);
    ptemp = prev.clone();
    ctemp = contr.clone();

    if (is_dark(prev) || hdr) {
        synEF(&ptemp, &temp, 1 / g);
    }
    else {
        synEF(&ctemp, &temp, g);
    }

    vector<Mat> pme = {prev, contr, temp};
    Ptr<MergeMertens> merge = createMergeMertens();
    merge->process(pme, res);
    //expo_fuse(pme, res);

    res.convertTo(res, CV_8UC3, 255, 0);
}