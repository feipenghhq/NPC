// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/27/2023
// ------------------------------------------------------------------------------------------------
// Itrace: Instruction trace
// ------------------------------------------------------------------------------------------------

#include <string.h>
#include "config.h"
#include "common.h"

#ifdef CONFIG_ITRACE

// ----------------------------------------------
// Global Variable and Function prototype
// ----------------------------------------------

extern FILE *itrace_fp;

static void iringbuf_write(char *str);
static void iringbuf_print();
char *disasm(word_t *inst, word_t pc);

// ----------------------------------------------
// Instruction Trace
// ----------------------------------------------

#define MSG_LEN  128
#define INST_LEN 33

void itrace_write(word_t pc, word_t inst) {
    char msg[MSG_LEN];
    sprintf(msg, "0x%08x: 0x%08x%s", pc, inst, disasm(&inst, pc));
    fprintf(itrace_fp, "%s\n", msg);
    fflush(itrace_fp);
    iringbuf_write(msg);
}


void itrace_print() {
    iringbuf_print();
}


// ----------------------------------------------
// Instruction ring buffer
// ----------------------------------------------

typedef struct iringbuf {
    char buf[CONFIG_IRINGBUF_LEN][CONFIG_IRINGBUF_SIZE];
    bool vld[CONFIG_IRINGBUF_LEN];
    int end;  // point to the end of the buffer
} iringbuf_s;

static iringbuf_s iringbuf = {.vld = {false}, .end = 0};

#define iringbuf_inc_wrap(var) do { \
    if (var == CONFIG_IRINGBUF_LEN - 1) \
      var = 0; \
    else \
      var++; \
    } while(0)

static void iringbuf_write(char *str) {
    iringbuf_inc_wrap(iringbuf.end);
    char *rc = strcpy(iringbuf.buf[iringbuf.end], str);
    Check(rc, "Failed to write to iringbuf");
    iringbuf.vld[iringbuf.end] = true;
}

static void iringbuf_print() {
    int start = iringbuf.end;
    iringbuf_inc_wrap(start);
    Log("Instruction sequence to error instruction (Dump from iringbuf):\n");
    Log("     PC          MCode          Instruction\n");
    Log("     ----------- ----------     -----------\n");
    for (; start < CONFIG_IRINGBUF_LEN; start++)
        if (iringbuf.vld[start]) Log("     %s\n", iringbuf.buf[start]);

    for (start = 0; start < iringbuf.end; start++)
        if (iringbuf.vld[start]) Log("     %s\n", iringbuf.buf[start]);

    Log("---> %s\n", iringbuf.buf[iringbuf.end]);
    Log("\n");
}

#undef INST_LEN
#undef MSG_LEN

#endif
