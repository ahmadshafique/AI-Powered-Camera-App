#include <opencv2/opencv.hpp>
using namespace cv;

/*
	Implementation of "A novel joint histogram equalization based image contrast enhancement"

	Reference:
		Agrawal, Sanjay & Panda, Rutuparna & Mishro, P.K. & Abraham, Ajith. (2019). 
		A novel joint histogram equalization based image contrast enhancement. 
		Journal of King Saud University - Computer and Information Sciences. 10.1016/j.jksuci.2019.05.010. 

	Code Obtained From:
		https://github.com/dengyueyun666/Image-Contrast-Enhancement/blob/master/src/JHE.cpp
*/

void JHE(const cv::Mat & src, cv::Mat & dst);
