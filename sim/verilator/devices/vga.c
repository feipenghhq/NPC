/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/03/2024
 *
 * ------------------------------------------------------------------------------------------------
 */

#include "config.h"

#ifdef CONFIG_HAS_VGACTL
#include "device.h"
#include "mmio.h"

// only support 400x300
#define SCREEN_W 400
#define SCREEN_H 300

static void init_screen();

//-----------------------------------------------
// VGA control and framebuffer
//-----------------------------------------------
#define VGACTL_SIZE 8
#define VGACTL_BASE VGACTL_ADDR
#define VGACTL_END  (VGACTL_BASE + VGACTL_SIZE - 1)

#define FB_SIZE 400 * 300 * 4
#define FB_BASE FB_ADDR
#define FB_END  (FB_BASE + FB_SIZE - 1)

// 2 registers defined for VGA CTRL
// offset 0
//  - [15:0]  - screen height
//  - [31:16] - screen width
// offset 4
//  - [0:0]   - sync

static const char vga_name[] = "vgactl";
static const char fb_name[] = "framebuffer";
static uint32_t *vgactl_regs = NULL;
static uint32_t *framebuffer = NULL;

void init_vgactl() {
    add_device(vga_name, (void *) VGACTL_BASE, (void *) VGACTL_END, NULL /*No callback function*/);
    vgactl_regs = (uint32_t *) (mmio_ptr() + (VGACTL_BASE - MMIO_BASE));
    vgactl_regs[0] = SCREEN_H | SCREEN_W << 16;
    vgactl_regs[1] = 0;
    init_screen();
    log_info("Initialized VGACTL device");
}

void init_framebuffer() {
    add_device(fb_name, (void *) FB_BASE, (void *) FB_END, NULL /*No callback function*/);
    framebuffer = (uint32_t *) (mmio_ptr() + (FB_BASE - MMIO_BASE));
    log_info("Initialized Frame Buffer");
}


//-----------------------------------------------
// Use SDL2 to show screen
//-----------------------------------------------
#ifdef CONFIG_VGA_SHOW_SCREEN
#include <SDL2/SDL.h>

static SDL_Window *window = NULL;
static SDL_Renderer *renderer = NULL;
static SDL_Texture *texture = NULL;

static void init_screen() {
    window = SDL_CreateWindow("NRC", 0, 0, SCREEN_W * 2, SCREEN_H * 2, 0);
    renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
    texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_ARGB8888,
        SDL_TEXTUREACCESS_STATIC, SCREEN_W, SCREEN_H);
    SDL_RenderPresent(renderer);
}

static void update_screen() {
    SDL_UpdateTexture(texture, NULL, framebuffer, SCREEN_W * sizeof(uint32_t));
    SDL_RenderClear(renderer);
    SDL_RenderCopy(renderer, texture, NULL, NULL);
    SDL_RenderPresent(renderer);
}

void vga_update_screen() {
  if (vgactl_regs[1]) {
    update_screen();
    vgactl_regs[1] = 0;
  }
}

void vga_close_screen() {
    SDL_DestroyWindow(window);
}

#else

static void init_screen() {}
static void update_screen() {}
void vga_update_screen() {}
void vga_close_screen() {}

#endif

#endif
