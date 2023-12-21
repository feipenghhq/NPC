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
word_t mem_read(word_t addr);


#endif
