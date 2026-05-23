cmake_minimum_required(VERSION 3.10.2)
project(flycast LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_library(glesv2-lib GLESv2)
find_library(android-lib android)
find_library(log-lib log)
find_library(dl-lib dl)
find_library(z-lib z)

# Root paths
set(FLYCAST_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../)
set(CORE_DIR ${FLYCAST_ROOT}/core)

# -------------------------
# Core sources
# -------------------------

file(GLOB CORE_ROOT_SRCS              "${CORE_DIR}/*.cpp")
file(GLOB CORE_LOG_SRCS               "${CORE_DIR}/log/*.cpp")
file(GLOB_RECURSE CORE_REIOS_SRCS     "${CORE_DIR}/reios/*.cpp")

file(GLOB CORE_CFG_SRCS               "${CORE_DIR}/cfg/*.cpp")
file(GLOB CORE_INPUT_SRCS             "${CORE_DIR}/input/*.cpp")
file(GLOB CORE_UI_SRCS                "${CORE_DIR}/ui/*.cpp")
file(GLOB_RECURSE CORE_HW_SRCS        "${CORE_DIR}/hw/*.cpp")
file(GLOB_RECURSE CORE_OS_SRCS        "${CORE_DIR}/oslib/*.cpp")
file(GLOB CORE_ARCHIVE_SRCS           "${CORE_DIR}/archive/*.cpp")
file(GLOB CORE_MD5_SRCS               "${CORE_DIR}/deps/md5/*.cpp")
file(GLOB CORE_CART_SRCS              "${CORE_DIR}/hw/cart/*.cpp")
file(GLOB CORE_NETWORK_SRCS           "${CORE_DIR}/network/*.cpp")

# imgread: explicit list so CHD/libchdr is excluded
set(CORE_IMGREAD_SRCS
    ${CORE_DIR}/imgread/isofs.cpp
    ${CORE_DIR}/imgread/gdi.cpp
    ${CORE_DIR}/imgread/cue.cpp
    ${CORE_DIR}/imgread/cdi.cpp
    ${CORE_DIR}/imgread/common.cpp
    ${CORE_DIR}/imgread/ImgReader.cpp
    ${CORE_DIR}/imgread/cdio.cpp
)

# Minimal Android-safe renderer sources
set(CORE_REND_SRCS
    ${CORE_DIR}/rend/osd.cpp
    ${CORE_DIR}/rend/CustomTexture.cpp
    ${CORE_DIR}/rend/TexCache.cpp
)

# -------------------------
# Android exclusions
# -------------------------

# log / desktop / unsupported
list(FILTER CORE_LOG_SRCS EXCLUDE REGEX ".*/NetworkListener.*\\.cpp$")
list(FILTER CORE_REIOS_SRCS EXCLUDE REGEX ".*/reios_elf\\.cpp$")

list(FILTER CORE_UI_SRCS EXCLUDE REGEX ".*/discord\\.cpp$")
list(FILTER CORE_UI_SRCS EXCLUDE REGEX ".*/gui_chat\\.cpp$")
list(FILTER CORE_UI_SRCS EXCLUDE REGEX ".*/gui_achievements\\.cpp$")

list(FILTER CORE_ARCHIVE_SRCS EXCLUDE REGEX ".*/ZipArchive\\.cpp$")
list(FILTER CORE_ARCHIVE_SRCS EXCLUDE REGEX ".*/rzip\\.cpp$")

list(FILTER CORE_HW_SRCS EXCLUDE REGEX ".*/dsp_arm64\\.cpp$")
list(FILTER CORE_HW_SRCS EXCLUDE REGEX ".*/arm7_rec_arm64\\.cpp$")
list(FILTER CORE_HW_SRCS EXCLUDE REGEX ".*/naomi/.*\\.cpp$")

# network disabled for this Android build
list(FILTER CORE_NETWORK_SRCS EXCLUDE REGEX ".*/dns\\.cpp$")
list(FILTER CORE_NETWORK_SRCS EXCLUDE REGEX ".*/miniupnp\\.cpp$")
list(FILTER CORE_NETWORK_SRCS EXCLUDE REGEX ".*/picoppp\\.cpp$")
list(FILTER CORE_NETWORK_SRCS EXCLUDE REGEX ".*/naomi_network\\.cpp$")
list(FILTER CORE_NETWORK_SRCS EXCLUDE REGEX ".*/net_handshake\\.cpp$")
list(FILTER CORE_NETWORK_SRCS EXCLUDE REGEX ".*/dcnet\\.cpp$")
list(FILTER CORE_NETWORK_SRCS EXCLUDE REGEX ".*/ggpo\\.cpp$")

# -------------------------
# Native library
# -------------------------

add_library(zamdc SHARED
    flycast_jni.cpp
    android_input.cpp
    jni_util.cpp
    ${FLYCAST_ROOT}/shell/android-studio/flycast/src/main/jni/src/Android.cpp
    android_missing_stubs.cpp

    ${CORE_ROOT_SRCS}
    ${CORE_LOG_SRCS}
    ${CORE_REIOS_SRCS}
    ${CORE_CFG_SRCS}
    ${CORE_INPUT_SRCS}
    ${CORE_UI_SRCS}
    ${CORE_HW_SRCS}
    ${CORE_OS_SRCS}
    ${CORE_ARCHIVE_SRCS}
    ${CORE_IMGREAD_SRCS}
    ${CORE_MD5_SRCS}
    ${CORE_CART_SRCS}
    ${CORE_NETWORK_SRCS}
    ${CORE_REND_SRCS}

    ${CORE_DIR}/deps/imgui/imgui.cpp
    ${CORE_DIR}/deps/imgui/imgui_draw.cpp
    ${CORE_DIR}/deps/imgui/imgui_tables.cpp
    ${CORE_DIR}/deps/imgui/imgui_widgets.cpp
    ${CORE_DIR}/hw/maple/maple_cfg.cpp 
)

# -------------------------
# Include directories
# -------------------------

target_include_directories(zamdc PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/../jni/src
    ${FLYCAST_ROOT}/shell/android-studio/flycast/src/main/jni/src
    ${CORE_DIR}
    ${CORE_DIR}/deps
    ${CORE_DIR}/deps/nowide/include
    ${CORE_DIR}/deps/imgui
    ${CORE_DIR}/deps/stb

    # xxhash
    ${CORE_DIR}/deps/xxhash
    ${CORE_DIR}/deps/xxhash/include
    ${CORE_DIR}/deps/xxHash
    ${CORE_DIR}/deps/xxHash/include

    ${CORE_DIR}/deps/json
    ${CORE_DIR}/deps/glm

    # keep glad path available in case other files include it later
    ${CORE_DIR}/deps/glad/include
)

# -------------------------
# Compile options / definitions
# -------------------------

target_compile_options(zamdc PRIVATE
    -O2
    -ffunction-sections
    -fdata-sections
    -fvisibility=hidden
    -fexceptions
)

target_compile_definitions(zamdc PRIVATE
    ANDROID
    __ANDROID__
    DISABLE_NAOMI
    DISABLE_CARD_READER
    DISABLE_DISCORD
    DISABLE_NETWORK
    DISABLE_SERIALIZATION
    NO_NETWORK
    NO_GGPO
    NO_BARCODE
    NO_LIBZIP
    NO_MINIUPNPC
)

# -------------------------
# Link libs
# -------------------------

target_link_libraries(zamdc
    ${glesv2-lib}
    ${android-lib}
    ${log-lib}
    ${dl-lib}
    ${z-lib}
)

target_link_options(zamdc PRIVATE
    -Wl,-z,max-page-size=16384
)

