/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/05/2024
 *
 * ------------------------------------------------------------------------------------------------
 */

// The keyboard implementation is pretty much the same as the one in NJU ICS Native keyboard

#include "config.h"

#ifdef CONFIG_HAS_KEYBOARD
#include "device.h"
#include <SDL2/SDL.h>

#define KEYBOARD_SIZE 4
#define KEYBOARD_BASE KBD_ADDR
#define KEYBOARD_END  (KEYBOARD_BASE + KEYBOARD_SIZE - 1)

#define KEYDOWN_MASK 0x8000
#define KEYQUEUE_LEN 1024

// The AM (abstract machine) Key map from NJU ICS Lab
#define NRC_KEYS(_) \
  _(ESCAPE) _(F1) _(F2) _(F3) _(F4) _(F5) _(F6) _(F7) _(F8) _(F9) _(F10) _(F11) _(F12) \
  _(GRAVE) _(1) _(2) _(3) _(4) _(5) _(6) _(7) _(8) _(9) _(0) _(MINUS) _(EQUALS) _(BACKSPACE) \
  _(TAB) _(Q) _(W) _(E) _(R) _(T) _(Y) _(U) _(I) _(O) _(P) _(LEFTBRACKET) _(RIGHTBRACKET) _(BACKSLASH) \
  _(CAPSLOCK) _(A) _(S) _(D) _(F) _(G) _(H) _(J) _(K) _(L) _(SEMICOLON) _(APOSTROPHE) _(RETURN) \
  _(LSHIFT) _(Z) _(X) _(C) _(V) _(B) _(N) _(M) _(COMMA) _(PERIOD) _(SLASH) _(RSHIFT) \
  _(LCTRL) _(APPLICATION) _(LALT) _(SPACE) _(RALT) _(RCTRL) \
  _(UP) _(DOWN) _(LEFT) _(RIGHT) _(INSERT) _(DELETE) _(HOME) _(END) _(PAGEUP) _(PAGEDOWN)

#define AM_KEY_NAMES(key) AM_KEY_##key,

enum {
  NRC_KEY_NONE = 0,
  NRC_KEYS(AM_KEY_NAMES)
};

const char name[] = "keyboard";

// Map the SDL_SCANCODE to AM_KEY_CODE
#define XX(k) [SDL_SCANCODE_##k] = AM_KEY_##k,

static const int keymap[256] = {
  NRC_KEYS(XX)
};

static int keyqueue[KEYQUEUE_LEN];
static int head = 0;
static int tail = 0;
static int depth = 0;

static void keyqueue_enqueue(int key) {
    keyqueue[head] = key;
    head = (head == KEYQUEUE_LEN - 1) ? 0 : head + 1;
    depth++;
    Check(depth <= KEYQUEUE_LEN, "key buffer overflow");
}

static int keyqueue_dequeue() {
    int key = NRC_KEY_NONE;
    if (depth > 0) {
        key = keyqueue[tail];
        tail = (tail == KEYQUEUE_LEN - 1) ? 0 : tail + 1;
        depth--;
    }
    return key;
}

/**
 * Pop one key from keyqueue and write the data to the MMIO register
 */
static void keyboard_callback(word_t addr, word_t data, bool is_write, byte_t *mmio) {
    uint32_t *keyboard_regs = (uint32_t *) (mmio + (KEYBOARD_BASE - MMIO_BASE));
    *keyboard_regs = keyqueue_dequeue();
}

void init_keyboard() {
    add_device(name, (void *) KEYBOARD_BASE, (void *) KEYBOARD_END, keyboard_callback);
    log_info("Initialized KEYBOARD device");
}

void send_key(SDL_Event *event) {
    int keydown = (event->key.type == SDL_KEYDOWN);
    SDL_Keysym ksym = event->key.keysym;
    SDL_Scancode scode = ksym.scancode;
    int key = keymap[scode];
    if (key != NRC_KEY_NONE) {
        // add keydown info to the key and push it to the queue
        key = key | (keydown ? KEYDOWN_MASK : 0);
        keyqueue_enqueue(key);
    }
}

#endif

