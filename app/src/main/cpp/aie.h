#include <opencv2/opencv.hpp>

using namespace cv;


/*
	Implementation of "Adaptive image enhancement method for correcting low-illumination images"

	Reference:
		Wencheng Wang, Zhenxue Chen, Xiaohui Yuan, Xiaojin Wu,
        Adaptive image enhancement method for correcting low-illumination images,
        Information Sciences,
        Volume 496,
        2019,
        Pages 25-41,
        ISSN 0020-0255,
        https://doi.org/10.1016/j.ins.2019.05.015.
        (https://www.sciencedirect.com/science/article/pii/S0020025519304104)

	Code Obtained From:
		https://github.com/dengyueyun666/Image-Contrast-Enhancement/blob/master/src/adaptiveImageEnhancement.cpp
*/

void adaptiveImageEnhancement(const cv::Mat& src, cv::Mat& dst);
