/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/01/2023
 *
 * ------------------------------------------------------------------------------------------------
 */

#ifndef __MEMORY_MMIO_H__
#define __MEMORY_MMIO_H__

#include "common.h"

word_t mmio_read(word_t addr);
void mmio_write(word_t addr, word_t data);
byte_t *mmio_ptr();

#endif
