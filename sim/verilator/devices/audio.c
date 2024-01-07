/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/07/2024
 *
 * ------------------------------------------------------------------------------------------------
 */

#include "config.h"

#ifdef CONFIG_HAS_AUDIO
#include "device.h"
#include "mmio.h"
#include <SDL2/SDL.h>

#define AUDIO_SIZE 24
#define AUDIO_BASE AUDIO_ADDR
#define AUDIO_END  (AUDIO_BASE + AUDIO_SIZE - 1)

#define SBUF_SIZE 64 * 1024
#define SBUF_BASE AUDIO_SBUF_ADDR
#define SBUF_END  (SBUF_BASE + SBUF_SIZE - 1)

enum {
  reg_freq,
  reg_channels,
  reg_samples,
  reg_sbuf_size,
  reg_init,
  reg_count,
  nr_reg
};

static const char audio_name[] = "Audio";
static const char sbuf_name[] = "Sbuf";

static uint8_t *sbuf = NULL;
static uint32_t *audio_regs = NULL;
static int sbuf_tail = 0;

void init_audio_SDL();

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
static void audio_callback(word_t addr, word_t data, bool is_write, byte_t *mmio) {
#pragma GCC diagnostic pop
    if (is_write) {
        word_t offset = addr - AUDIO_BASE;
        switch (offset / 4) {
            case reg_init: {
                init_audio_SDL();
                break;
            }
            default:
        }
    }
}

void init_audio() {
    add_device(audio_name, (void *) AUDIO_BASE, (void *) AUDIO_END, audio_callback);
    audio_regs = (uint32_t *) (mmio_ptr() + (AUDIO_BASE - MMIO_BASE));
    audio_regs[reg_sbuf_size] = SBUF_SIZE; // set the sound buff size
    log_info("Initialized AUDIO device");
}

void init_sbuf() {
    add_device(sbuf_name, (void *) SBUF_BASE, (void *) SBUF_END, NULL /*No callback function*/);
    sbuf = (uint8_t *) (mmio_ptr() + (SBUF_BASE - MMIO_BASE));
    log_info("Initialized Sbuf");
}

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
void sdl_audio_callback(void *userata, uint8_t *stream, int len) {
#pragma GCC diagnostic pop
  int count = audio_regs[reg_count];

  // If we don't have enough data, only fill what we have
  int i, size = len > count ? count : len;
  for (i = 0; i < size; i++) {
    stream[i] = sbuf[sbuf_tail];
    sbuf_tail++;
    if (sbuf_tail == SBUF_SIZE) sbuf_tail = 0;
  }
  // Fill rest of the data to 0 if len > count
  for (; i < len; i++) {
    stream[i] = 0;
  }
  // update the count register
  audio_regs[reg_count] -= size;
}

void init_audio_SDL() {
  SDL_AudioSpec s;
  SDL_zero(s);
  s.freq = audio_regs[reg_freq];
  s.format = AUDIO_S16SYS;
  s.channels = audio_regs[reg_channels];
  s.samples = audio_regs[reg_samples];
  s.callback = sdl_audio_callback;
  s.userdata = NULL;

  SDL_InitSubSystem(SDL_INIT_AUDIO);
  SDL_OpenAudio(&s, NULL);
  SDL_PauseAudio(0);
}

#endif
