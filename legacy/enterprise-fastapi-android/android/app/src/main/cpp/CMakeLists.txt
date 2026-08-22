cmake_minimum_required(VERSION 3.22.1)
project("lennit_rust_core")

# Import the pre-built Rust .so from jniLibs
add_library(
    lennit_genesis_core
    SHARED
    IMPORTED
)

set_target_properties(
    lennit_genesis_core
    PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/liblennit_genesis_core.so
)
