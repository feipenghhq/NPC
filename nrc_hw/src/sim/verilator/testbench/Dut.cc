/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/19/2023
 *
 * ------------------------------------------------------------------------------------------------
 *  Dut class: Provide environment for different CPU design
 *  - Provide API to access the design internal signal and data
 *  - Provide API to common simulation task
 *  - Provide basic simulation flow
 * ------------------------------------------------------------------------------------------------
 */

#include "Dut.h"

// ---------------------------------------------
// Function prototype and global variable
// ---------------------------------------------

int reg_str2id(const char *);
void itrace_print();

// ---------------------------------------------
// Class functions
// ---------------------------------------------

Dut::Dut(int argc, char *argv[], const test_info *info) {
    Verilated::commandArgs(argc, argv);
    this->info = info;
    sim_time = 0;
    m_trace = NULL;
    finished = false;
    success = false;
}

Dut::~Dut() {
    if (m_trace) {
        m_trace->close();
        delete m_trace;
    }
}

word_t Dut::reg_str2val(const char *s) {
    int id = reg_str2id(s);
    return reg_id2val(id);
}

void Dut::dump() {
    if (m_trace) {
        m_trace->dump(sim_time);
    }
}

bool Dut::report() {
    log_info("Test finished at %ld cycle.", sim_time);
    if (success) {
        log_info_color("Test PASS!", ANSI_FG_GREEN);
    }
    else {
    #ifdef CONFIG_ITRACE
        itrace_print();
    #endif
        log_err("Test FAIL!");
    }
    return success;
}

