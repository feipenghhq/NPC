/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/19/2023
 *
 * ------------------------------------------------------------------------------------------------
 *  Top class: Provide environment for different CPU design
 *  - Provide API to access the design internal signal and data
 *  - Provide API to common simulation task
 *  - Provide basic simulation flow
 * ------------------------------------------------------------------------------------------------
 */

#include "Top.h"

int reg_str2id(const char *);

Top::Top(int argc, char *argv[], const test_info_s *test_info) {
    Verilated::commandArgs(argc, argv);
    this->test_info = test_info;
    sim_time = 0;
    finished = false;
    success = false;
    m_trace = NULL;
}

Top::~Top() {
    if (m_trace) {
        m_trace->close();
        delete m_trace;
    }
}

word_t Top::reg_str2val(const char *s) {
    int id = reg_str2id(s);
    return reg_id2val(id);
}

bool Top::report() {
    log_info("Test finished at %ld cycle.", sim_time);
    if (success) {
        log_info_color("Test PASS!", ANSI_FG_GREEN);
    }
    else {
        log_err("Test FAIL!");
    }
    return success;
}

