// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Author: Heqing Huang
// Date Created: 01/22/2024
//
// ------------------------------------------------------------------------------------------------

#include "trace.h"

void init_trace(const char *elf) {
#ifdef CONFIG_ITRACE
    itrace_init();
#endif
#ifdef CONFIG_MTRACE
    mtrace_init();
#endif
#ifdef CONFIG_FTRACE
    ftrace_init(elf);
#endif
#ifdef CONFIG_STRACE
    strace_init(elf);
#endif
}

void close_trace() {
#ifdef CONFIG_ITRACE
    itrace_close();
#endif
#ifdef CONFIG_MTRACE
    mtrace_close();
#endif
#ifdef CONFIG_FTRACE
    ftrace_close();
#endif
#ifdef CONFIG_STRACE
    strace_close();
#endif
}

void print_trace() {
#ifdef CONFIG_ITRACE
    itrace_print();
#endif
#ifdef CONFIG_MTRACE
    mtrace_print();
#endif
#ifdef CONFIG_FTRACE
    ftrace_print();
#endif
#ifdef CONFIG_STRACE
    strace_print();
#endif
}
