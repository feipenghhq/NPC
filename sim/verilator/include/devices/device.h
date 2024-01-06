/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/01/2024
 *
 * ------------------------------------------------------------------------------------------------
 * device: device related function
 * ------------------------------------------------------------------------------------------------
 */


#include "common.h"

typedef void (*device_callback)(word_t addr, word_t data, bool is_write, byte_t *mmio);

typedef struct IOMap {
    const char *name;
    void *start;
    void *end;
    device_callback callback;
} IOMap;

void add_device(const char *name, void *start, void *end, device_callback callback);
void init_device();
void update_device();
void device_write(word_t addr, word_t data, byte_t *mmio);
void device_read(word_t addr, byte_t *mmio);

