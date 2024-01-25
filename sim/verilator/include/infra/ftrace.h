// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Author: Heqing Huang
// Date Created: 01/22/2024
//
// ------------------------------------------------------------------------------------------------


#ifndef __FTRACE_H__
#define __FTRACE_H__

#include "config.h"

#ifdef CONFIG_FTRACE

#include <string.h>
#include "common.h"
#include "ringbuf.h"

void ftrace_init(const char *elf);
void ftrace_close();
void ftrace_write(word_t pc, word_t nextpc, word_t inst);
void ftrace_print();

#endif
#endif
