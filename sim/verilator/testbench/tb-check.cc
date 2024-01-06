// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/20/2023
// ------------------------------------------------------------------------------------------------
// check the status of the simulation
// ------------------------------------------------------------------------------------------------

#include <string.h>
#include <assert.h>
#include "Dut.h"

// ---------------------------------------------
//variable
// ---------------------------------------------

#define ICS_AM_TEST "ics-am-test"

extern bool dpi_ebreak;
extern bool NRC_SDL_quit;

// ---------------------------------------------
// Check test finished or not
// ---------------------------------------------

/**
 * tests from NJU ics lab
 */
static bool ics_check_finish(Dut *top) {
    return dpi_ebreak;
}

/**
 * select and run different check finish function based on test suite
 */
bool check_finish(Dut *top, const char *suite) {
    if (NRC_SDL_quit) return true;
    if (strcmp(suite, ICS_AM_TEST) == 0)
        return ics_check_finish(top);
    assert(0);
}

// ---------------------------------------------
// Check test pass or fail
// ---------------------------------------------

/**
 * test from NJU ics lab
 */
static bool ics_check_pass(Dut *top) {
    return top->reg_str2val("a0") == 0;
}

/**
 * select and run different check pass function based on test suite
 */
bool check_pass(Dut *top, const char *suite) {
    if (NRC_SDL_quit) return true;
    if (strcmp(suite, ICS_AM_TEST) == 0)
        return ics_check_pass(top);
    assert(0);
}

