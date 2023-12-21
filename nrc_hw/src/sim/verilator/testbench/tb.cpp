// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/20/2023
// ------------------------------------------------------------------------------------------------
// tb: test bench
// ------------------------------------------------------------------------------------------------

#include "Core_sTop.h"
#include "IcsTestSuite.h"
#include "common.h"
#include "utils.h"


#define CORE_S "core_s"


static Top *top = NULL;

static void select_top(int argc, char *argv[], const char *dut, bool trace) {
    if (strcmp(dut, CORE_S) == 0)
        top = new Core_sTop(argc, argv, dut, trace);
    else {
        log_err("Undefined dut: %s", dut);
        exit(0);
    }
}

int run_test(int argc, char *argv[], const argu_s *argu) {
    select_top(argc, argv, argu->dut, argu->trace);
    top->init_trace("waveform.vcd", 99);
    top->reset();
    top->run(-1);
    top->report();
    delete top;
    return 0;
}
