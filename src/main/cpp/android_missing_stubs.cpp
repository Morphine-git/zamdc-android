#include <cstdio>
#include <string>
#include <vector>

#include "serialize.h"
#include "archive/rzip.h"
#include "oslib/storage.h"
#include "network/net_handshake.h"
#include "hw/naomi/naomi_cart.h"

// -------------------------
// Serialization stubs
// -------------------------

Serializer::Serializer(void* data, unsigned long limit, bool rollback)
    : SerializeBase(limit, rollback) {}

Deserializer::Deserializer(const void* data, unsigned long limit, bool rollback)
    : SerializeBase(limit, rollback) {}

void dc_serialize(Serializer&) {}
void dc_deserialize(Deserializer&) {}

// -------------------------
// REIOS / cartridge stubs
// -------------------------

Cartridge* CurrentCartridge = nullptr;

bool reios_loadElf(const std::string&) {
    return false;
}

// -------------------------
// Core/system stubs
// -------------------------

void dc_exit() {}

void InitAudio() {}

void TermAudio() {}

void os_SetThreadName(const char*) {}

// -------------------------
// GGPO stubs
// -------------------------

namespace ggpo {

bool active() {
    return false;
}

void nextFrame() {}

} // namespace ggpo

// -------------------------
// Card reader stubs
// -------------------------

namespace card_reader {

bool barcodeAvailable() {
    return false;
}

bool readerAvailable() {
    return false;
}

void barcodeSetCard(const std::string&) {
}

void insertCard(int) {
}

} // namespace card_reader

// -------------------------
// Network / Naomi stubs
// -------------------------

bool NaomiNetworkSupported() {
    return false;
}

void NetworkHandshake::term() {}

InputDescriptors* NaomiGameInputs = nullptr;

void naomi_reg_Reset(bool) {
}

unsigned short NaomiBoardIDRead() {
    return 0;
}

void NaomiBoardIDWrite(unsigned short) {
}

// -------------------------
// AICA / ARM stubs
// -------------------------

namespace aica {
namespace dsp {

void recInit() {
}

void recTerm() {
}

} // namespace dsp

namespace arm {

void arm7backend_flush() {
}

} // namespace arm
} // namespace aica

// -------------------------
// Virtual memory stubs
// -------------------------

namespace virtmem {

void region_lock(void*, unsigned long) {
}

void region_unlock(void*, unsigned long) {
}

} // namespace virtmem

// -------------------------
// RZipFile stubs
// -------------------------

bool RZipFile::Open(FILE*, bool) {
    return false;
}

size_t RZipFile::Write(const void*, unsigned long) {
    return 0;
}

size_t RZipFile::Read(void*, unsigned long) {
    return 0;
}

void RZipFile::Close() {
}

// -------------------------
// hostfs storage/screenshot stubs
// -------------------------

namespace hostfs {

class DummyCustomStorage : public CustomStorage {
public:
    bool isKnownPath(const std::string&) override {
        return false;
    }

    std::vector<FileInfo> listContent(const std::string&) override {
        return {};
    }

    FILE* openFile(const std::string&, const std::string&) override {
        return nullptr;
    }

    std::string getParentPath(const std::string&) override {
        return "";
    }

    std::string getSubPath(const std::string&, const std::string& subpath) override {
        return subpath;
    }

    FileInfo getFileInfo(const std::string&) override {
        return {};
    }

    bool exists(const std::string&) override {
        return false;
    }

    bool addStorage(bool,
                    bool,
                    const std::string&,
                    void (*)(bool, std::string),
                    const std::string&) override {
        return false;
    }
};

CustomStorage& customStorage() {
    static DummyCustomStorage storage;
    return storage;
}

void saveScreenshot(const std::string&, const std::vector<unsigned char>&) {
}

} // namespace hostfs
