cmake_minimum_required(VERSION 3.6)

# dummy target to bring libc++_shared.so into packages
add_library(opencv_jni_shared STATIC dummy.cpp)
add_compile_definitions(OPENCV_DISABLE_FILESYSTEM_SUPPORT=ON)
