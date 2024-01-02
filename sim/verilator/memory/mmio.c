/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/01/2023
 *
 * ------------------------------------------------------------------------------------------------
 */

#include "config.h"
#include "common.h"
#include "device.h"

//----------------------------------------------
// Function prototype, global variable
//-----------------------------------------------

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
    device_read(addr);
    uintptr_t offset = addr - MMIO_BASE;
    uintptr_t paddr = (uintptr_t) mmio + offset;
    paddr = paddr & ADDR_MASK; // make addr align to word boundary
    // call device callback function first then read
    device_read(addr);
    return *((word_t *) paddr);
}

/**
 * Write to mmio space
 */
void mmio_write(word_t addr, word_t data) {
    uintptr_t offset = addr - MMIO_BASE;
    uintptr_t paddr = (uintptr_t) mmio + offset;
    paddr = paddr & ADDR_MASK; // make addr align to word boundary
    // write first then call device callback function
    *((word_t *) paddr) = data;
    device_write(addr, data);
}

