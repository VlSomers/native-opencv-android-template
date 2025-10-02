# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

project("native-lib")

# Use OpenCV_DIR passed from gradle (via arguments in build.gradle)
# This eliminates the need for hardcoded paths and uses the opencvsdk gradle property
if(NOT DEFINED OpenCV_DIR)
    message(FATAL_ERROR "OpenCV_DIR not set. Make sure it's passed from gradle build.gradle")
endif()

# Include OpenCV headers
include_directories(${OpenCV_DIR}/include)

# Add prebuilt OpenCV library
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(opencv_java4 PROPERTIES
    IMPORTED_LOCATION ${OpenCV_DIR}/../libs/${ANDROID_ABI}/libopencv_java4.so)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp )

# Configure 16 KB page size compatibility
# Add linker flags for proper alignment to support 16 KB page sizes
set_target_properties(native-lib PROPERTIES
    LINK_FLAGS "-Wl,-z,max-page-size=16384"
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # OpenCV
                       opencv_java4

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
