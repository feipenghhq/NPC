/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/18/2023
 *
 * ------------------------------------------------------------------------------------------------
 */

#include <stdio.h>
#include <stdint.h>
#include "config.h"
#include "paddr.h"
#include "mmio.h"

//----------------------------------------------
// Function prototype, global variable
//-----------------------------------------------

#define ADDR_MASK (UINTPTR_MAX-0x3)

extern FILE *mtrace_fp;
static byte_t mem[MSIZE]; // assign the memory into stack

void mtrace_write(word_t addr, word_t data, word_t strb, bool is_write, bool ifetch);

//----------------------------------------------
// Functions
//-----------------------------------------------

/**
 * Load the image to memory
 * The image file should be a binary file
 */
size_t load_image(const char *img) {
    Check(img, "Please specify the image file");
    log_info("Loading image file: %s", img);
    FILE *fp = fopen(img, "rb");
    Check(fp, "Can't open file %s", img);

    fseek(fp, 0, SEEK_END);
    size_t size = ftell(fp);
    rewind(fp);

    size_t rc = fread((void *) mem, size, 1, fp);
    Check(rc, "Failed to read the image file: %s", img);

    return size;
}

/**
 * read memory. always read word_t size
 */
word_t pmem_read(word_t addr, bool ifetch) {
    uintptr_t offset = addr - MEM_BASE;
    uintptr_t paddr = (uintptr_t) mem + offset;
#ifdef CONFIG_MTRACE
    word_t data = *((word_t *) paddr); // this is byte aligned not word aligned
    mtrace_write(addr, data, 0, false, ifetch);
#endif
    // make the addr word boundary aligned because the hardware always read
    // a word each time
    paddr = paddr & ADDR_MASK;
    return *((word_t *) paddr);
}

/**
 * write memory. always write word_t size
 */
void pmem_write(word_t addr, word_t data, char strb) {
    uintptr_t offset = addr - MEM_BASE;
    uintptr_t paddr = (uintptr_t) mem + offset;
    // make the addr word boundary aligned
    paddr = paddr & ADDR_MASK;
    // only support for 32b data
    for (int i = 0; i < NUM_BYTE; i++) {
        if ((strb & (0x1 << i)) != 0) {
            *((byte_t *) (paddr + i)) = (byte_t) (data >> (8*i));
        }
    }
#ifdef CONFIG_MTRACE
    mtrace_write(addr, data, strb, true, false);
#endif
}

byte_t *mem_ptr() {
    return mem;
}

/**
 * Check if the addr is out of memory bound
 */
static void out_of_bound(word_t addr) {
    Panic("paddr out of bound. addr: 0x%08x", addr);
}

/**
 * Write on physical address
 */
void paddr_write(word_t addr, word_t data, char strb) {
    if (likely(in_pmem(addr))) return pmem_write(addr, data, strb);
#ifdef CONFIG_HAS_DEVICE
    return mmio_write(addr, data);
#endif
    out_of_bound(addr);
}

/**
 * read on physical address
 */
word_t paddr_read(word_t addr, bool ifetch) {
    if (likely(in_pmem(addr))) return pmem_read(addr, ifetch);
#ifdef CONFIG_HAS_DEVICE
    return mmio_read(addr);
#endif
    out_of_bound(addr);
    return 0;
}

