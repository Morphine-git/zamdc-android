#include <cstring>
#include <string>

#include "serialize.h"
#include "oslib/oslib.h"

// ============================================================
// Custom texture stubs
// ============================================================

bool custom_texture = false;

namespace CustomTexture {
    void Terminate() {}
}

// ============================================================
// Serialization helpers required by nullDC / emulator
// ============================================================

void dc_serialize(Serializer&) {}
void dc_deserialize(Deserializer&) {}

// ============================================================
// OS helpers
// ============================================================

void os_SetThreadName(const char*) {}

// ============================================================
// MD5 stubs (used by stdclass / savestates)
// ============================================================

extern "C" {

typedef struct {
    unsigned char dummy[128];
} MD5_CTX;

int MD5_Init(MD5_CTX*) { return 1; }
int MD5_Update(MD5_CTX*, const void*, unsigned long) { return 1; }
int MD5_Final(unsigned char* out, MD5_CTX*) {
    if (out) std::memset(out, 0, 16);
    return 1;
}

}

// ============================================================
// GD-ROM (libGDR) stubs — required by reios + gdrom_hle
// ============================================================

enum DiskArea { SingleDensity, DoubleDensity };

int libGDR_GetDiscType() { return 0; }

int libGDR_GetTrackNumber(unsigned int, unsigned int&) { return 0; }

int libGDR_GetTrackAdrAndControl(unsigned int, unsigned char&, unsigned char&) {
    return 0;
}

int libGDR_ReadSector(unsigned char*, unsigned int, unsigned int, unsigned int, bool) {
    return 0;
}

int libGDR_GetToc(unsigned int*, DiskArea) { return 0; }

int libGDR_GetTrack(unsigned int, unsigned int&, unsigned int&) { return 0; }

// ============================================================
// reios globals expected by reios.cpp
// ============================================================

int CurrentCartridge = 0;
void* disc = nullptr;

bool reios_loadElf(const std::string&) {
    return false;
}
