/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/19/2023
 *
 * ------------------------------------------------------------------------------------------------
 *  Core_s class: Provide environment for Core_s CPU design
 * ------------------------------------------------------------------------------------------------
 */

#include "Core_s.h"
#include "memory.h"

#define MAX_SIM_TIME 10000

// ---------------------------------------------
// Function prototype and global variable
// ---------------------------------------------

bool dpi_ebreak;

// ---------------------------------------------
// Class functions
// ---------------------------------------------

Core_s::Core_s(int argc, char *argv[], const test_info *info):Dut(argc, argv, info) {
    top = new Vcore_s("core_s");
}

Core_s::~Core_s() {
    delete top;
}

void Core_s::init_trace(const char *name, int level) {
    Verilated::traceEverOn(true);
    if (info->trace) {
        m_trace = new VerilatedVcdC;
        top->trace(m_trace, level);
        m_trace->open(name);
    }
}

void Core_s::clk_tick() {
    top->clk ^= 1;
    top->eval();
    dump();
    sim_time++;
}

void Core_s::reset() {
    top->clk = 1; // initialize clock
    top->rst_b = 0;
    top->eval();
    dump();
    sim_time++;
    for (int i = 0; i < reset_cycle; i++) {
        clk_tick();
    }
    top->rst_b = 1;
    assert(top->clk == 1); // we want to change data on negedge
}

bool Core_s::run(uint64_t step) {
    int cnt = 0;
    while(!finished && ((step < 0 && sim_time < MAX_SIM_TIME)  || cnt < step)) {
        clk_tick();
        // trace need to be put here because the next_pc is updated at this point
        // while the change has not been committed yet
        trace(top->pc, top->core_s->u_IFU->next_pc, top->core_s->inst);
        clk_tick();
        // diff test need to be put here as the change has been committed at this point
        difftest();
        cnt++;
        check();
    }
    return finished;
}

word_t Core_s::reg_id2val(int id) {
    return top->core_s->u_RegFile->regs[id];
}

// ---------------------------------------------
// DPI function
// ---------------------------------------------

extern "C" void dpi_set_ebreak() {
    dpi_ebreak = true;
}

extern "C" void dpi_pmem_read(int addr, int *rdata, svBit ifetch) {
    *rdata = pmem_read(addr, ifetch);
}

extern "C" void dpi_pmem_write(int addr, int data, char strb) {
    pmem_write(addr, data, strb);
}
