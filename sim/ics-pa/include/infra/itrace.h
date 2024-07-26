// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Author: Heqing Huang
// Date Created: 01/22/2024
//
// ------------------------------------------------------------------------------------------------


#ifndef __ITRACE_H__
#define __ITRACE_H__

#include "config.h"

#ifdef CONFIG_ITRACE

#include <string.h>
#include "common.h"
#include "ringbuf.h"

void itrace_init();
void itrace_close();
void itrace_write(word_t pc, word_t inst);
void itrace_print();

#endif
#endif
