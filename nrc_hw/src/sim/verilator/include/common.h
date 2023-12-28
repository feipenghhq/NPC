/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/18/2023
 *
 * ------------------------------------------------------------------------------------------------
 * Common defines
 * ------------------------------------------------------------------------------------------------
 */

#ifndef __COMMON_H__
#define __COMMON_H__

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <assert.h>

#include "macro.h"

// define different data width
typedef uint8_t  byte_t;
typedef uint32_t half_t;
typedef uint32_t word_t;

// define name for DUT
#define DUT_CORE_S "core_s"

// define name for suite
#define SUITE_ICS_AM_CPU_TEST "ics-am-cpu-test"

// test information
typedef struct test_info {
    char *image;    // test image
    char *suite;    // test suite name
    char *test;     // test name
    char *dut;      // rtl top level
    bool trace;     // dump the waveform
    char *elf;      // test elf file
    char *ref;      // Reference for difftest
} test_info;

#endif
