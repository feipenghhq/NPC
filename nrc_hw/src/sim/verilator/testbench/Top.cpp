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

Top::Top(int argc, char *argv[], const test_info_s *test_info) {
    Verilated::commandArgs(argc, argv);
    this->test_info = test_info;
    sim_time = 0;
    finished = false;
    success = false;
    m_trace = NULL;
}

Top::~Top() {
    log_info("Test finished at %ld cycle.", sim_time);
    if (m_trace) {
        m_trace->close();
        delete m_trace;
    }
}

