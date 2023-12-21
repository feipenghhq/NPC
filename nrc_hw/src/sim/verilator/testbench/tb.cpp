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
#include "common.h"
#include "utils.h"
#include "memory.h"

Top *top = NULL;

static void select_top(int argc, char *argv[], test_info_s *test_info) {
    if (strcmp(test_info->dut, DUT_CORE_S) == 0)
        top = new Core_sTop(argc, argv, test_info);
    else {
        log_err("Undefined dut: %s", test_info->dut);
        exit(0);
    }
}


int run_test(int argc, char *argv[], const argu_s *argu) {
    test_info_s test_info = {.suite=argu->suite, .test=argu->test, .dut=argu->dut,
        .trace=argu->trace};
    select_top(argc, argv, &test_info);
    top->init_trace("waveform.vcd", 99);
    load_image(argu->image);
    top->reset();
    top->run(-1);
    top->report();
    delete top;
    return 0;
}
