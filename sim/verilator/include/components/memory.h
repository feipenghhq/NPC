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

#ifndef __MEMORY_H__
#define __MEMORY_H__

#include "common.h"

size_t load_image(const char *img);

word_t pmem_read(word_t addr, bool ifetch);

void pmem_write(word_t addr, word_t data, char strb);

#endif
