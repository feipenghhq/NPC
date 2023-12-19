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

#include "common.h"
#include "memory.h"
#include "config.h"

static byte_t mem[MSIZE];

/**
 * Load the image to memory
 * The image file should be a binary file
 */
void load_image(const char *img) {
    Assert(img, "Please specify the image file");
    log_info("Loading image file: %s", img);
    FILE *fp = fopen(img, "rb");
    Assert(fp, "Can't open file %s", img);

    fseek(fp, 0, SEEK_END);
    size_t size = ftell(fp);
    size_t rc = fread((void *) mem, size, 1, fp);
    Assert(rc, "Failed to read the image file: %s", img);
    log_info("Image file loaded");
}

