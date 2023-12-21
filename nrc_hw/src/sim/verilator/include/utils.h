// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/20/2023
// ------------------------------------------------------------------------------------------------
// Uitils
// ------------------------------------------------------------------------------------------------

#ifndef __UTILS_H__
#define __UTILS_H__

#include "common.h"

typedef struct argu {
    char *image;    // image file
    char *suite;    // test suite name
    char *test;     // test name
    char *dut;      // rtl top level
    bool trace;     // dump the waveform
} argu_s;

int parse_args(int argc, char *argv[]);


#endif
