cmake_minimum_required(VERSION 3.10.2)
project("liverSdk")

aux_source_directory(./librtmp  RTMP_DIR)
set(CPP_PATH /Users/lihui/AndroidStudioProjects/Liver/LiverSdk/src/main/cpp)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")


aux_source_directory(${CPP_PATH} SOURCE_DIRECTORY)


add_library(
        liverSdk
        SHARED
        ${SOURCE_DIRECTORY} ${RTMP_DIR})

find_library(
        log-lib
        log)



target_link_libraries(
        liverSdk

        ${log-lib})