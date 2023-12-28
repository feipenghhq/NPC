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

// ----------------------------------------------
// Instruction Trace
// ----------------------------------------------

#define MSG_LEN 48

void itrace_write(word_t pc, word_t inst) {
    char msg[MSG_LEN];
    sprintf(msg, "0x%08x: 0x%08x", pc, inst);
#ifdef CONFIG_ITRACE
    fprintf(itrace_fp, "%s\n", msg);
#endif
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
    fprintf(stderr, "Instruction sequence to error instruction (Dump from iringbuf):\n");
    fprintf(stderr, "     PC          Instruction\n");
    fprintf(stderr, "     ----------- -----------\n");
    for (; start < CONFIG_IRINGBUF_LEN; start++)
        if (iringbuf.vld[start]) fprintf(stderr, "     %s\n", iringbuf.buf[start]);

    for (start = 0; start < iringbuf.end - 1; start++)
        if (iringbuf.vld[start]) fprintf(stderr, "     %s\n", iringbuf.buf[start]);

    fprintf(stderr, "---> %s\n", iringbuf.buf[iringbuf.end]);
}

#undef MSG_LEN

#endif
