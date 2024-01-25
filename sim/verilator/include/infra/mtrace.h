// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Author: Heqing Huang
// Date Created: 01/22/2024
//
// ------------------------------------------------------------------------------------------------


#ifndef __MTRACE_H__
#define __MTRACE_H__

#include "config.h"

#ifdef CONFIG_MTRACE

#include <string.h>
#include "common.h"
#include "ringbuf.h"

void mtrace_init();
void mtrace_close();
void mtrace_write(word_t addr, word_t data, word_t strb, bool is_write, bool ifetch);
void mtrace_print();

#endif
#endif
