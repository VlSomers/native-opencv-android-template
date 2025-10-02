#include <jni.h>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#define TAG "NativeLib"

using namespace std;
using namespace cv;

extern "C" {

/**
 * Native function called from Java/Kotlin to process camera frames
 * This applies OpenCV adaptive threshold to convert grayscale image to binary
 * @param env JNI environment
 * @param instance calling object instance
 * @param matAddr memory address of OpenCV Mat object from Java
 */
void JNICALL
Java_com_example_nativeopencvandroidtemplate_MainActivity_adaptiveThresholdFromJNI(JNIEnv *env,
                                                                                   jobject instance,
                                                                                   jlong matAddr) {

    // Get Mat from memory address passed from Java/Kotlin
    Mat &mat = *(Mat *) matAddr;

    // Record start time for performance measurement
    clock_t begin = clock();

    // Apply OpenCV adaptive threshold
    // Parameters: input/output mat, max value, adaptive method, threshold type, block size, constant
    cv::adaptiveThreshold(mat, mat, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 9, 10);

    // Calculate and log processing time
    double totalTime = double(clock() - begin) / CLOCKS_PER_SEC;
    __android_log_print(ANDROID_LOG_INFO, TAG, "adaptiveThreshold computation time = %f seconds\n",
                        totalTime);
}

}