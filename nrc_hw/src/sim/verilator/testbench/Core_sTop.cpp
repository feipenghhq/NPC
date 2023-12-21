/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/19/2023
 *
 * ------------------------------------------------------------------------------------------------
 *  Core_sTop class: Provide environment for Core_s CPU design
 * ------------------------------------------------------------------------------------------------
 */

#include "Core_sTop.h"

#define MAX_SIM_TIME 100

Core_sTop::Core_sTop(int argc, char *argv[], const char *name, bool trace):Top(argc, argv, name, trace) {
    top = new Vcore_s();
}

Core_sTop::~Core_sTop() {
    delete top;
}

void Core_sTop::init_trace(const char *name, int level) {
    Verilated::traceEverOn(true);
    if (trace) {
        m_trace = new VerilatedVcdC;
        top->trace(m_trace, level);
        m_trace->open(name);
    }
}

void Core_sTop::clk_tick() {
    top->clk ^= 1;
    top->eval();
    dump();
    sim_time++;
}

void Core_sTop::reset() {
    top->clk = 1; // initialize clock
    top->rst_b = 0;
    top->eval();
    dump();
    sim_time++;
    for (int i = 0; i < reset_cycle; i++) {
        clk_tick();
    }
    top->rst_b = 1;
}

bool Core_sTop::run(int step) {
    int cnt = 0;
    assert(top->clk == 1); // we want to change data on negedge
    while(!finished && ((step < 0 && sim_time < MAX_SIM_TIME)  || cnt < step)) {
        clk_tick();
        // FIXME: update the instruction
        clk_tick();
        cnt++;
        // FIXME: implement the finish and success
        //finished = ts->check_finish();
        //if (finished) success = ts->check_success();
    }
    return finished;
}

void Core_sTop::report() {}

