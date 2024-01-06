/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/01/2024
 *
 * ------------------------------------------------------------------------------------------------
 */

// https://en.wikibooks.org/wiki/Serial_Programming/8250_UART_Programming
// NOTE: this is compatible to 16550

#include "config.h"

#ifdef CONFIG_HAS_SERIAL
#include "device.h"

#define SERIAL_SIZE 8
#define SERIAL_BASE SERIAL_PORT
#define SERIAL_END  (SERIAL_PORT + SERIAL_SIZE - 1)

static const char name[] = "serial";

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
static void serial_callback(word_t addr, word_t data, bool is_write, byte_t *mmio) {
#pragma GCC diagnostic pop
    word_t offset = addr - SERIAL_PORT;
    Check(is_write, "Serial only support write mode");
    Check(offset == 0, "Serial only support offset 0x0 for now. offset = 0x%08x", offset);
    putchar(data);
}

void init_serial() {
    add_device(name, (void *) SERIAL_BASE, (void *) SERIAL_END, serial_callback);
    log_info("Initialized SERIAL device");
}

#endif
