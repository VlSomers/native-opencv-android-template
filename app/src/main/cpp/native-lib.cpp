#include <jni.h>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#define TAG "NativeLib"

using namespace std;
using namespace cv;

extern "C" {
void JNICALL Java_com_example_nativeopencvandroidtemplate_MainActivity_adaptiveThresholdFromJNI(JNIEnv *env, jobject instance, jlong inputAddr, jlong outputAddr) {

    Mat &input = *(Mat *) inputAddr;
    Mat &output = *(Mat *) outputAddr;

    clock_t begin = clock();

    cv::adaptiveThreshold(input, output, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 21, 5);

    double total_time = double (clock() - begin ) / CLOCKS_PER_SEC;
    __android_log_print(ANDROID_LOG_INFO, TAG, "adaptiveThreshold computation time = %f seconds\n",  total_time);
}
}