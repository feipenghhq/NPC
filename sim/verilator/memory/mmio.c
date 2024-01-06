/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/01/2023
 *
 * ------------------------------------------------------------------------------------------------
 */

#include "common.h"
#include "device.h"
#include "config.h"

//----------------------------------------------
// Function prototype, global variable
//-----------------------------------------------

void difftest_skip_ref();

// assign the mmio space into stack
static byte_t mmio[MMIO_SIZE];

//----------------------------------------------
// Functions
//-----------------------------------------------

#define ADDR_MASK (UINTPTR_MAX-0x3)

/**
 * read mmio space.
 */
word_t mmio_read(word_t addr) {
#ifdef CONFIG_DIFFTEST
    difftest_skip_ref();
#endif
    uintptr_t offset = addr - MMIO_BASE;
    uintptr_t paddr = (uintptr_t) mmio + offset;
    paddr = paddr & ADDR_MASK; // make addr align to word boundary
    // call device callback function first then read
    device_read(addr, mmio);
    return *((word_t *) paddr);
}

/**
 * Write to mmio space
 */
void mmio_write(word_t addr, word_t data) {
#ifdef CONFIG_DIFFTEST
    difftest_skip_ref();
#endif
    uintptr_t offset = addr - MMIO_BASE;
    uintptr_t paddr = (uintptr_t) mmio + offset;
    paddr = paddr & ADDR_MASK; // make addr align to word boundary
    // write first then call device callback function
    *((word_t *) paddr) = data;
    device_write(addr, data, mmio);
}

/**
 * Get the pointer to mmio
 */
byte_t *mmio_ptr() {
    return mmio;
}
