#include <math.h>
#include <algorithm>
#include <opencv2/opencv.hpp>

#include "contrast_enhancement.h"

#include"syn-ef.h"

using namespace std;
using namespace cv;


void correction(Mat &img, const Mat &prev) {
	/*
	Sometimes certain pixel values get close to 0 during synEF and afterwards it is difficult 
	to increase their intensity during multiplictions. This function takes input image and final image
	and correct such pixels.

	Parameters:
		img: Final image produced by synEF module
		prev: Input image
	*/

	//cout << "correction" << endl;
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
	/*
	Gamma Correction

	Parameters:
		img: Image to be gamma corrected
		g: gamma value
	*/

	Mat lookUpTable(1, 256, CV_8U);
	uchar* p = lookUpTable.ptr();
	for (int i = 0; i < 256; ++i)
		p[i] = saturate_cast<uchar>(pow(i / 255.0, g) * 255.0);
	LUT(*img, lookUpTable, *img);
}


void extract(Mat* lum, int regions, Mat* labels) {
	/*
	Segment image into regions based upon identical luminosity values

	Parameters:
		lum: Luminosity component of image to be segmented
		regions: Number of regions
		labels: Number of labels
	*/

	vector<uchar> lums = lum->reshape(0, 1);
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

vector<double> get_regions(Mat *prev) {
	/*
	Generates gamma correction paremeters

	Paremeters:
		prev: Input image
	
	Returns:
		arr: Array containing 3 gamma values
	*/
	Mat &mprev = *(Mat *)prev;

	//Reducing size of image while performing segmentation to improve speed
	Mat small;
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

	int regions = 3;
	Mat labels(lum.rows, lum.cols, CV_8UC1);


	//Generate Segments
	extract(&lum, regions, &labels);

	vector<double> a;	//Enhancement Factors
	vector<double> sums(regions, 0);
	vector<double> count(regions, 0);

	//To avoid division by 0
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

	vector<double> arr;

	//Calculate gamma correction parameters
	for (int i = 0; i < regions; i++) {
		double v = sums[i] / (count[i] + 0.003);
		a.push_back(0.33 / exp(v));
		//cout << v / 2.2 << endl;
		arr.push_back(v / 2.2);
	}
	return arr;
}


bool is_dark(Mat img) {
	/*
	Checks whether image is dark or not. If mean luminance of image is less than 
	or equal to 85, it is considered to be dark. Value of 85 is empirically selected.

	Parameters:
		img: Input image
	*/

	Mat temp;
	cvtColor(img, temp, COLOR_RGB2HSV);
	Scalar m = mean(temp);
	//cout << m[2] << endl;
	m = mean(temp);
	return m[2] <= 85;
}


bool is_hdr(Mat img) {
	/*
	Checks whether image is extreme high dyamic range image. If ratio of bright to dark pixels is 
	between 20% and 80% and the mean of dark pixels is less than 28, it is an extreme hdr image.

	Parameters:
		img: Input image
	*/

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
	if (float(n) / total < 0.2 || float(n) / total > 0.8)
		return false;
	m = sum(dr);
	if (m[2]/(total-n) < 28)
		return true;
	return false;
}


void synEF(Mat &prev, Mat &res) {
	Mat contr, temp, ptemp = prev.clone(), ctemp;
	vector<Mat> pme = { prev.clone() };		//vector containing images for exposure fusion.

	//check image category
	bool hdr = is_hdr(ptemp), dark = is_dark(prev);

	//get gamma parameters
	vector<double> arr = get_regions(&ptemp);

	//gamma parameter correction
	//cout << "dark: " << dark << " hdr: " << hdr <<endl;
	if (dark && !hdr) {
		//cout << "dark" << endl;
		arr[2] = arr[2];
		arr[1] = 1/arr[1];
		arr[0] = arr[0]/2;
	}
	else if (hdr) {
		//cout << "hdr" << endl;
		arr[1] = arr[1]/2;	
		arr[0] = arr[2]/(arr[0]-arr[2]);	
		arr[2] = arr[2];	
	}
	else {
		//cout << "normal" << endl;
		arr[1] = 2 / arr[2];		
		arr[0] = 1 / arr[0];
		arr[2] = 1/arr[2];
	}

	int mode;
	for (int i = 0; i < 3; i++) {
		ptemp = prev.clone();
		gamma(&ptemp, 1/arr[i]);	//gamma correction
		if((i==0 && dark && !hdr)||(i==1 && !dark && !hdr) || (hdr && dark && i==1) || (hdr && !dark && i==2)){
			mode = 1;
		}else if(dark && hdr && i==0){
			mode = 2;
		}else{
			mode=0;
		}
		contrastEnhancement(ptemp, res, mode);	//apply contrast enhancement
		pme.push_back(res.clone());		//add to eposure fusion stack
	}

	//exposure fusion
	Ptr<MergeMertens> merge = createMergeMertens();
	merge->process(pme, res);
	res.convertTo(res, CV_8UC3, 255, 0);

	/*
	for(int i = 0; i < pme.size(); i++){
		imshow(to_string(i), pme[i]);
	}
	imshow("res", res);
	waitKey();
	destroyAllWindows();
	*/
	return;
}
