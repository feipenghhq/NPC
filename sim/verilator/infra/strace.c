// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Author: Heqing Huang
// Date Created: 01/24/2024
//
// ------------------------------------------------------------------------------------------------

#include <string.h>
#include "strace.h"

#ifdef CONFIG_STRACE

#define MSG_LEN CONFIG_SRINGBUF_SIZE

extern FILE *strace_fp;
static ringbuf *rb = NULL;

void strace_init() {
    rb = ringbuf_create(CONFIG_SRINGBUF_ENTRY, CONFIG_SRINGBUF_SIZE);
}

void strace_close() {
    ringbuf_delete(rb);
}

void strace_write(word_t pc, word_t code) {
    char msg[MSG_LEN];
    snprintf(msg, MSG_LEN, "[Strace]: System call 0x%08x @PC 0x%08x", code, pc);
#ifdef CONFIG_STRACE_WRITE_LOG
    fprintf(strace_fp, "%s\n", msg);
    fflush(strace_fp);
#endif
    ringbuf_write(rb, msg);
}

void strace_print() {
    Log("System Call trace (Dump from mringbuf):\n");
    ringbuf_print(rb);
}

#undef MSG_LEN

#endif
