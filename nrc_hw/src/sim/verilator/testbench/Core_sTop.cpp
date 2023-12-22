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
#include "memory.h"

#define MAX_SIM_TIME 100

bool check_finish(Top *top, const char *suite);
bool check_pass(Top *top, const char *suite);
extern bool dpi_ebreak;

Core_sTop::Core_sTop(int argc, char *argv[], const test_info_s *test_info):Top(argc, argv, test_info) {
    top = new Vcore_s("core_s");
}

Core_sTop::~Core_sTop() {
    delete top;
}

void Core_sTop::init_trace(const char *name, int level) {
    Verilated::traceEverOn(true);
    if (test_info->trace) {
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
        top->inst = mem_read(top->pc);
        clk_tick();
        clk_tick();
        cnt++;
        finished = check_finish(this, test_info->suite);
        if (finished) {
            success = check_pass(this, test_info->suite);
            return finished;
        }
    }
    return finished;
}

word_t Core_sTop::reg_id2val(int id) {
    return top->core_s->u_RegFile->regs[id];
}

void dpi_set_ebreak() {
    dpi_ebreak = true;
}

