/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/18/2023
 *
 * ------------------------------------------------------------------------------------------------
 * memory: memory related functions for the verilator testbench
 * ------------------------------------------------------------------------------------------------
 */

#include "memory.h"
#include "common.h"
#include "config.h"
#include <stdio.h>

// assign the memory into stack
static byte_t mem[MSIZE];

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

word_t pmem_read(word_t addr) {
    uintptr_t offset = addr - MEM_OFFSET;
    uintptr_t paddr = (uintptr_t) mem + offset;
    return *((word_t *) paddr);
}

void pmem_write(word_t addr, word_t data, char strb) {
    uintptr_t offset = addr - MEM_OFFSET;
    uintptr_t paddr = (uintptr_t) mem + offset;
    // only support for 32b data
    for (int i = 0; i < 3; i++) {
        if (strb & (0x1 << i)) *(((byte_t *) paddr) + i) = (byte_t) (strb >> (8*i));
    }
}

