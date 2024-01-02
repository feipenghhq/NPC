/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/01/2024
 *
 * ------------------------------------------------------------------------------------------------
 */

#include "config.h"
#include "device.h"
#include "mmio.h"

#define NUM_DEVICE 10
static IOMap devices[NUM_DEVICE];
static int nr_device = 0;

void init_serial();

/**
 * Add a device to device list
 */
void add_device(const char *name, void *start, void *end, device_callback callback) {
    Check(nr_device < NUM_DEVICE, "Can't add more device, please increase NUM_DEVICE");
    devices[nr_device].name = name;
    devices[nr_device].start = start;
    devices[nr_device].end = end;
    devices[nr_device].callback = callback;
    nr_device++;
}

/**
 * search for device index in device list
 */
static int search_device(word_t addr) {
    int i;
    for (i = 0; i < nr_device; i++) {
        if ((size_t) addr >= (size_t) devices[i].start && (size_t) addr < (size_t) devices[i].end)
            return i;
    }
    Panic("Failed to find device at addr 0x%08x", addr);
}

/**
 * Initialize the device
 */
void init_device() {
    log_info("Initializing device.");
#ifdef CONFIG_HAS_SERIAL
    init_serial();
#endif
}

/**
 * Access device
 */
inline void device_access(word_t addr, word_t data, bool is_write) {
    int device = search_device(addr);
    if (devices[device].callback) {
        devices[device].callback(addr, data, is_write);
    }
}

/**
 * Write to the device
 */
void device_write(word_t addr, word_t data) {
    device_access(addr, data, true);
}

/**
 * read to the device
 */
void device_read(word_t addr) {
    device_access(addr, 0, false);
}

