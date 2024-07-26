// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Author: Heqing Huang
// Date Created: 01/24/2024
//
// ------------------------------------------------------------------------------------------------


#ifndef __STRACE_H__
#define __STRACE_H__

#include "config.h"

#ifdef CONFIG_STRACE

#include <string.h>
#include "common.h"
#include "ringbuf.h"

void strace_init();
void strace_close();
void strace_write(word_t pc, word_t code);
void strace_print();

#endif
#endif
