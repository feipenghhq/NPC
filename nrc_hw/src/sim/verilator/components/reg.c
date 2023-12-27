// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/21/2023
// ------------------------------------------------------------------------------------------------
// Register related function
// ------------------------------------------------------------------------------------------------

#include "common.h"

static const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

int reg_str2id(const char *s) {
    int i;
    for (i = 0; i < ARRLEN(regs); i++) {
        if (strcmp(regs[i], s) == 0) {
            return i;
        }
    }
    return -1;
}
