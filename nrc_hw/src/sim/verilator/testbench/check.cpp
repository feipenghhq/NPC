// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/20/2023
// ------------------------------------------------------------------------------------------------
// check the status of the simulation
// ------------------------------------------------------------------------------------------------

#include "Core_sTop.h"
#include <string.h>
#include <assert.h>

extern bool dpi_ebreak;

inline bool ics_check_finish() {
    return dpi_ebreak;
}

bool check_finish(Top *top, const char *suite) {
    if (strcmp(suite, SUITE_ICS_AM_CPU_TEST) == 0)
        return ics_check_finish();
    assert(0);
}
