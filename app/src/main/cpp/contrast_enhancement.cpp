#include <iostream>
#include <opencv2/opencv.hpp>

#include "contrast_enhancement.h"
#include "agarwal.h"

using namespace cv;
using namespace std;

void strech(const Mat &img, Mat &grey) {
	Mat chan[3];
	Mat temp;
	split(img, chan);
	chan[0] *= 0.299;
	chan[1] *= 0.587;
	chan[2] *= 0.114;
	grey = chan[0] + chan[1] + chan[2];
}

void hue_correction(Mat &prev, const Mat &img, const Mat &grey, Mat &enhanced) {
	vector<Mat> chan(3);
	split(img, chan);
	Mat temp, temp2;
	divide(265 - prev, 265 - grey, temp);
	for (int i = 0; i < 3; i++) {
		multiply(chan[i] - grey, temp, chan[i]);
		chan[i] += prev;
	}
	temp.release();
	Mat great;
	merge(chan, great);
	split(img, chan);
	divide(prev, grey, prev);
	double max_v;
	minMaxIdx(prev, nullptr, &max_v, nullptr, nullptr);
	for (int i = 0; i < 3; i++) {
		multiply(chan[i], prev, chan[i]);
	}
	Mat less;
	merge(chan, less);

	threshold(prev, prev, 1, 1, 0);
	for (int i = 0; i < 3; i++) {
		chan[i] = prev.clone();
	}
	merge(chan, prev);
	
	//Color Chaipi
	//swap(great, less);
	multiply(Scalar(1,1,1) - prev, great, great);
	multiply(prev, less, less);
	enhanced = great + less;
}

void global_contrast(const Mat &grey, Mat &global) {
	grey.convertTo(global, CV_8UC1);
	JHE(global, global);
	global.convertTo(global, CV_32FC1);
}

void local_contrast(const Mat &grey, Mat &local) {
	grey.convertTo(local, CV_8UC1);
	Ptr<CLAHE> clahe = createCLAHE();
	clahe->setClipLimit(2);
	clahe->apply(local, local);
	local.convertTo(local, CV_32FC1);
}

void contrastEnhancement(const cv::Mat& src, cv::Mat& dst)
{
	Mat img;
	normalize(src, img, 0, 255, NORM_MINMAX, CV_32FC3);
	Mat grey;
	strech(img, grey);	//0.5s
	Mat global, local;
	Mat g_enhanced, l_enhanced;
	global_contrast(grey, global);//1.96 s
	hue_correction(global, img, grey, g_enhanced);//1.59s
	global.release();
	local_contrast(grey, local);//0.48s
	hue_correction(local, img, grey, l_enhanced);//1.60s
	local.release();
	grey.release();
	img.release();
	
	//imshow("local", l_enhanced/255);
	//imshow("global", g_enhanced/255);

	//3.0s
	Mat lg, gg;
	strech(l_enhanced, lg);
	strech(g_enhanced, gg);

	Mat lpl, lpg, bl, bg;
	/*try {
		Laplacian(lg, lpl, CV_32FC1);
	}
	catch (cv::Exception& e){
		const char* err_msg = e.what();
		std::cout << "exception caught: " << err_msg << std::endl;
	}*/
	Laplacian(lg, lpl, CV_32FC1, 3, 1, 0, BORDER_DEFAULT);
	Laplacian(gg, lpg, CV_32FC1, 3, 1, 0, BORDER_DEFAULT);/*
	lpl.convertTo(lpl, CV_32FC1);
	lpg.convertTo(lpg, CV_32FC1);*/
	
	//1.99s
	Mat temp = lg - 0.5;
	multiply(temp, temp, temp);
	bl = temp / 0.08;
	temp = gg - 0.5;
	multiply(temp, temp, temp);
	bg = temp / 0.08;

	//1.61s
	Mat wdl, wdg;
	cv::min(bl, lpl, wdl);
	cv::min(bg, lpg, wdg);

	normalize(wdl, wdl, 255, 0, NORM_MINMAX);
	normalize(wdg, wdg, 255, 0, NORM_MINMAX);


	//lg.release(); gg.release();
	lpl.release(); lpg.release(); bl.release(); bg.release();

	Mat wl, wg;
	divide(wdl, wdl + wdg, wl);
	divide(wdg, wdl + wdg, wg);

	wdl.release(); wdg.release();
	
	//1.12s
	vector<Mat> chan(3);
	for (int i = 0; i < 3; i++) {
		chan[i] = wl.clone();
	}
	merge(chan, wl);
	for (int i = 0; i < 3; i++) {
		chan[i] = wg.clone();
	}
	merge(chan, wg);
	for (int i = 0; i < 3; i++) {
		chan[i].release();
	}

	//0.8s
	/*multiply(wl, l_enhanced, l_enhanced);
	multiply(wg, g_enhanced, g_enhanced);*/

	/*vector<Mat> gaussianA, gaussianB, lapA, lapB, lap;

	int pyrScale = 1;

	for (int i = 0; i < pyrScale; i++) {
		gaussianA.push_back(lg);
		pyrDown(lg, lg);	
	}

	for (int i = 0; i < pyrScale; i++) {
		gaussianB.push_back(gg);
		pyrDown(gg, gg);
	}

	lapA.push_back(gaussianA[pyrScale-1]);
	for (int i = pyrScale-1; i > 0; i--) {
		pyrUp(gaussianA[i], lg);
		lapA.push_back(gaussianA[i - 1] - lg);
	}

	lapB.push_back(gaussianB[pyrScale-1]);
	for (int i = pyrScale-1; i > 0; i--) {
		pyrUp(gaussianB[i], gg);
		lapB.push_back(gaussianB[i - 1] - gg);
	}
	
	

	for (int i = pyrScale-1; i >= 0; i--) {
		gaussianA[i] = wl.clone();
		pyrDown(wl, wl);
	}

	for (int i = pyrScale-1; i >= 0; i--) {
		gaussianB[i] = wg.clone();
		pyrDown(wg, wg);
	}

	for (int i = 0; i < pyrScale; i++) {
		multiply(gaussianA[i], lapA[i], lg);
		multiply(gaussianB[i], lapB[i], gg);
		lap.push_back(lg + gg);
	}

	dst = lap[0];
	for (int i = 1; i < pyrScale; i++) {
		pyrUp(dst, dst);
		add(dst, lap[i], dst);
	}*/

	vector<Mat> gaussianA, gaussianB, lapA, lapB, lap;

	int pyrScale = 1;

	lg = l_enhanced; gg = g_enhanced;

	for (int i = 0; i < pyrScale; i++) {
		gaussianA.push_back(lg);
		pyrDown(lg, lg);
	}

	for (int i = 0; i < pyrScale; i++) {
		gaussianB.push_back(gg);
		pyrDown(gg, gg);
	}

	lapA.push_back(gaussianA[pyrScale - 1]);
	for (int i = pyrScale - 1; i > 0; i--) {
		pyrUp(gaussianA[i], lg);
		lapA.push_back(gaussianA[i - 1] - lg);
	}

	lapB.push_back(gaussianB[pyrScale - 1]);
	for (int i = pyrScale - 1; i > 0; i--) {
		pyrUp(gaussianB[i], gg);
		lapB.push_back(gaussianB[i - 1] - gg);
	}



	for (int i = pyrScale - 1; i >= 0; i--) {
		gaussianA[i] = wl.clone();
		pyrDown(wl, wl);
	}

	for (int i = pyrScale - 1; i >= 0; i--) {
		gaussianB[i] = wg.clone();
		pyrDown(wg, wg);
	}

	for (int i = 0; i < pyrScale; i++) {
		multiply(gaussianA[i], lapA[i], lg);
		multiply(gaussianB[i], lapB[i], gg);
		lap.push_back(lg + gg);
	}

	dst = lap[0];
	for (int i = 1; i < pyrScale; i++) {
		pyrUp(dst, dst);
		add(dst, lap[i], dst);
	}
	

	//0.66s
	/*dst = l_enhanced + g_enhanced;*/
	dst.convertTo(dst, CV_8UC3);

	/*cvtColor(src, temp, COLOR_RGB2YUV);
	split(temp, chan);
	chan[0] = dst.clone();
	merge(chan, dst);
	cvtColor(dst, dst, COLOR_YUV2RGB);*/
}