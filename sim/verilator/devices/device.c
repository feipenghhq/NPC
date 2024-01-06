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

#ifdef CONFIG_HAS_DEVICE

#include "mmio.h"
#include "device.h"
#include <SDL2/SDL.h>

#define NUM_DEVICE 8
static IOMap devices[NUM_DEVICE];
static int nr_device = 0;
bool NRC_SDL_quit = false;

void init_serial();
void init_timer();
void init_vgactl();
void init_framebuffer();
void init_keyboard();
void vga_update_screen();
void vga_close_screen();
void send_key(SDL_Event *event);
void sdl();

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
#ifdef CONFIG_HAS_TIMER
    init_timer();
#endif
#ifdef CONFIG_HAS_VGACTL
    init_vgactl();
    init_framebuffer();
#endif
#ifdef CONFIG_HAS_KEYBOARD
    init_keyboard();
#endif
}

/**
 * Initialize the device
 */
void update_device() {
#ifdef CONFIG_VGA_SHOW_SCREEN
    vga_update_screen();
#endif
    sdl();
}

/**
 * Access device
 */
void device_access(word_t addr, word_t data, bool is_write, byte_t *mmio) {
    int device = search_device(addr);
    if (devices[device].callback) {
        devices[device].callback(addr, data, is_write, mmio);
    }
}

/**
 * Write to the device
 */
void device_write(word_t addr, word_t data, byte_t *mmio) {
    device_access(addr, data, true, mmio);
}

/**
 * read to the device
 */
void device_read(word_t addr, byte_t *mmio) {
    device_access(addr, 0, false, mmio);
}

/**
 * SDL related task
 */
void sdl() {
    SDL_Event event;
    while(SDL_PollEvent(&event)) {
        switch(event.type) {
            case SDL_QUIT: {
                vga_close_screen();
                NRC_SDL_quit = true;
                return;
            }
        #ifdef CONFIG_HAS_KEYBOARD
            case SDL_KEYUP:
            case SDL_KEYDOWN: {
                send_key(&event);
                break;
            }
        #endif
            default: {}
        }
    }
}

#else

void init_device() {}
void update_device() {}

#endif




