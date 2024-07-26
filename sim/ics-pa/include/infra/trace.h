// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Author: Heqing Huang
// Date Created: 01/22/2024
//
// ------------------------------------------------------------------------------------------------

#ifndef __TRACE_H__
#define __TRACE_H__

#include "itrace.h"
#include "mtrace.h"
#include "ftrace.h"
#include "strace.h"

void init_trace(const char *elf);
void close_trace();
void print_trace();

#endif
