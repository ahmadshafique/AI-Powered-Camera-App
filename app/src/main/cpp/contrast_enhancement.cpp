#include <iostream>
#include <opencv2/opencv.hpp>

#include "contrast_enhancement.h"
#include "aie.h"
#include "agarwal.h"

using namespace cv;
using namespace std;


void strech(const Mat &img, Mat &grey) {
	/*
	Get luminance channel of image

	Parameters:
		img: Input image
		grey: output image
	*/

	Mat chan[3];
	Mat temp;
	split(img, chan);
	chan[0] *= 0.299;
	chan[1] *= 0.587;
	chan[2] *= 0.114;
	grey = chan[0] + chan[1] + chan[2];
}


void hue_correction(Mat &prev, const Mat &img, const Mat &grey, Mat &enhanced) {
	/*
	Performs hue correction.

	Paraeters:
		prev: Input enhanced image
		img: Original image
		grey: luminance channel of original image
		enhanced: output enhanced image qith hue correctionn
	*/

	vector<Mat> chan(3);
	split(img, chan);
	Mat temp, temp2;
	divide(265 - prev, 265 - grey, temp);
	for (int i = 0; i < 3; i++) {
		multiply(chan[i] - grey, temp, chan[i]);
		chan[i] += prev;
	}
	temp.release();
	Mat less;
	merge(chan, less);
	split(img, chan);
	divide(prev, grey, prev);
	double max_v;
	minMaxIdx(prev, nullptr, &max_v, nullptr, nullptr);
	for (int i = 0; i < 3; i++) {
		multiply(chan[i], prev, chan[i]);
	}
	Mat great;
	merge(chan, great);

	threshold(prev, prev, 1, 1, 0);
	for (int i = 0; i < 3; i++) {
		chan[i] = prev.clone();
	}
	merge(chan, prev);
	
	multiply(Scalar(1,1,1) - prev, less, less);
	multiply(prev, great, great);
	enhanced = less + great;
}


void adaptive_contrast(const Mat &img, Mat &adaptive) {
	Mat temp;
	img.convertTo(temp, CV_8UC3);
	adaptiveImageEnhancement(temp, adaptive);
	strech(adaptive, adaptive);
}


void global_contrast(const Mat &grey, Mat &global) {
	/*
	Global contrast enhancement. For JHE, see agarwal.h

	Parameters:
		grey: Luminance channel of original image
		global: global contrast enhanced output image
	*/
	grey.convertTo(global, CV_8UC1);
	JHE(global, global);
}

void local_contrast(const Mat &grey, Mat &local) {
	/*
	Local  contrast enhancement. CLAHE is used.

	Parameters:
		grey: Luminance channel of original image
		local: local contrast enhanced output image
	*/

	grey.convertTo(local, CV_8UC1);
	Ptr<CLAHE> clahe = createCLAHE();
	clahe->setClipLimit(8);
	clahe->setTilesGridSize(Size(2, 2));
	clahe->apply(local, local);
}

void contrastEnhancement(const cv::Mat& src, cv::Mat& dst, int mode)
{
	/*
	Contrast Enhancement.

	Parameters:
		src: Input image
		dst: contrast enhanced output
	*/
	Mat img;
	normalize(src, img, 0, 255, NORM_MINMAX, CV_32FC3);
	Mat grey;
	strech(img, grey);
	Mat global, local, adaptive;
	Mat g_enhanced, l_enhanced, a_enhanced;
	global_contrast(grey, global);
	local_contrast(grey, local);
	adaptive_contrast(img, adaptive);

	//cout<<mode<<endl;
	if(mode==1){
		global = adaptive.clone();
	} else if(mode==2){
		adaptive = global.clone();
	}
	
	global.convertTo(global, CV_32FC1);
	local.convertTo(local, CV_32FC1);
	adaptive.convertTo(adaptive, CV_32FC1);

	
	hue_correction(global, img, grey, g_enhanced);
	hue_correction(local, img, grey, l_enhanced);
	hue_correction(adaptive, img, grey, a_enhanced);
	local.release();
	global.release();
	adaptive.release();
	grey.release();
	img.release();

	/*
	imshow("global", g_enhanced/Scalar(255,255,255));
	imshow("local", l_enhanced/Scalar(255,255,255));
	imshow("adaptive", a_enhanced/Scalar(255,255,255));
	waitKey();
	destroyAllWindows();
	*/


	//Weight Maps Calculation
	Mat lg, gg, ag;
	strech(l_enhanced, lg);
	strech(g_enhanced, gg);
	strech(a_enhanced, ag);

	Mat lpl, lpg, lpa, bl, bg, ba;
	Laplacian(lg, lpl, CV_32FC1, 3, 1, 0, BORDER_DEFAULT);
	Laplacian(gg, lpg, CV_32FC1, 3, 1, 0, BORDER_DEFAULT);
	Laplacian(ag, lpa, CV_32FC1, 3, 1, 0, BORDER_DEFAULT);
	
	Mat temp = lg - 0.5;
	multiply(temp, temp, temp);
	bl = temp / 0.08;
	temp = gg - 0.5;
	multiply(temp, temp, temp);
	bg = temp / 0.08;
	temp = ag - 0.5;
	multiply(temp, temp, temp);
	ba = temp / 0.08;
	temp.release();

	Mat wdl, wdg, wda;
	cv::min(bl, lpl, wdl);
	cv::min(bg, lpg, wdg);
	cv::min(ba, lpa, wda);

	normalize(wdl, wdl, 255, 0, NORM_MINMAX);
	normalize(wdg, wdg, 255, 0, NORM_MINMAX);
	normalize(wda, wda, 255, 0, NORM_MINMAX);

	lpl.release(); lpg.release(); lpa.release(); bl.release(); bg.release(); ba.release();

	Mat wl, wg, wa;
	divide(wdl, wdl + wdg + wda, wl);
	divide(wdg, wdl + wdg + wda, wg);
	divide(wda, wdl + wdg + wda, wa);

	wdl.release(); wdg.release(); wda.release();
	
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
		chan[i] = wa.clone();
	}
	merge(chan, wa);
	for (int i = 0; i < 3; i++) {
		chan[i].release();
	}

	//Weight and Images Blending using Image Pyramids
	vector<Mat> gaussianA, gaussianB, gaussianC, lapA, lapB, lapC, lap;

	int pyrScale = 1;

	lg = l_enhanced; gg = g_enhanced; ag = a_enhanced;

	for (int i = 0; i < pyrScale; i++) {
		gaussianA.push_back(lg);
		pyrDown(lg, lg);
	}

	for (int i = 0; i < pyrScale; i++) {
		gaussianB.push_back(gg);
		pyrDown(gg, gg);
	}

	for (int i = 0; i < pyrScale; i++) {
		gaussianC.push_back(ag);
		pyrDown(ag, ag);
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

	lapC.push_back(gaussianC[pyrScale - 1]);
	for (int i = pyrScale - 1; i > 0; i--) {
		pyrUp(gaussianC[i], ag);
		lapC.push_back(gaussianC[i - 1] - ag);
	}

	for (int i = pyrScale - 1; i >= 0; i--) {
		gaussianA[i] = wl.clone();
		pyrDown(wl, wl);
	}

	for (int i = pyrScale - 1; i >= 0; i--) {
		gaussianB[i] = wg.clone();
		pyrDown(wg, wg);
	}

	for (int i = pyrScale - 1; i >= 0; i--) {
		gaussianC[i] = wa.clone();
		pyrDown(wa, wa);
	}


	for (int i = 0; i < pyrScale; i++) {
		multiply(gaussianA[i], lapA[i], lg);
		multiply(gaussianB[i], lapB[i], gg);
		multiply(gaussianC[i], lapC[i], ag);
		lap.push_back(lg + gg + ag);
	}

	dst = lap[0];
	for (int i = 1; i < pyrScale; i++) {
		pyrUp(dst, dst);
		add(dst, lap[i], dst);
	}
	
	dst.convertTo(dst, CV_8UC3);
}
