// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Author: Heqing Huang
// Date Created: 12/27/2023
//
// ------------------------------------------------------------------------------------------------
// Itrace: Instruction trace
// ------------------------------------------------------------------------------------------------

#include <string.h>
#include "itrace.h"

#ifdef CONFIG_ITRACE

// ----------------------------------------------
// Global Variable and Function prototype
// ----------------------------------------------

void init_disasm();
extern FILE *itrace_fp;
char *disasm(word_t *inst, word_t pc);
static ringbuf *rb = NULL;

#define MSG_LEN  CONFIG_IRINGBUF_SIZE

// ----------------------------------------------
// Instruction Trace
// ----------------------------------------------

void itrace_init() {
    rb = ringbuf_create(CONFIG_IRINGBUF_ENTRY, CONFIG_IRINGBUF_SIZE);
    init_disasm();
}

void itrace_close() {
    ringbuf_delete(rb);
}

void itrace_write(word_t pc, word_t inst) {
    char msg[MSG_LEN];
    snprintf(msg, MSG_LEN, "0x%08x: 0x%08x%s", pc, inst, disasm(&inst, pc));
#ifdef CONFIG_ITRACE_WRITE_LOG
    fprintf(itrace_fp, "%s\n", msg);
    fflush(itrace_fp);
#endif
    ringbuf_write(rb, msg);
}

void itrace_print() {
    Log("Instruction sequence to error instruction (Dump from iringbuf):\n");
    Log("     PC          MCode          Instruction\n");
    Log("     ----------- ----------     -----------\n");
    ringbuf_print(rb);
}

#undef MSG_LEN

#endif
