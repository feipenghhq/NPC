/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/19/2023
 *
 * ------------------------------------------------------------------------------------------------
 *  CoreN class: Provide environment for CoreN CPU design
 * ------------------------------------------------------------------------------------------------
 */

#include "CoreN.h"

#define MAXNIM_TIME 10000

// ---------------------------------------------
// C Function prototype
// ---------------------------------------------

extern "C" {
    void paddr_write(word_t addr, word_t data, char strb);
    word_t paddr_read(word_t addr, bool ifetch);
    void strace_write(word_t pc, word_t code);
    void update_device();
}

extern FILE *strace_fp;

// ---------------------------------------------
// Function prototype and global variable
// ---------------------------------------------

bool dpi_ebreak;
word_t dpi_mem_access_pc;

// ---------------------------------------------
// Class functions
// ---------------------------------------------

CoreN::CoreN(int argc, char *argv[], const test_info *info):Dut(argc, argv, info) {
    top = new VCoreN("CoreN");
}

CoreN::~CoreN() {
    delete top;
}

void CoreN::init_trace(const char *name, int level) {
#ifdef CONFIG_WAVE
    Verilated::traceEverOn(true);
    m_trace = new VerilatedVcdC;
    top->trace(m_trace, level);
    m_trace->open(name);
#endif
}

void CoreN::clk_tick() {
    top->clk ^= 1;
    top->eval();
    dump();
    sim_time++;
}

void CoreN::reset() {
    top->clk = 1; // initialize clock
    top->resetn = 0;
    top->eval();
    dump();
    sim_time++;
    for (int i = 0; i < reset_cycle; i++) {
        clk_tick();
    }
    top->resetn = 1;
    assert(top->clk == 1); // we want to change data on negedge
}

bool CoreN::run(uint64_t step) {
    int cnt = 0;
    int done = 0;
    while(!finished && ((step < 0 && sim_time < MAXNIM_TIME)  || cnt < step)) {
        clk_tick();
        // trace need to be put here because the next_pc is updated at this point
        // while the change has not been committed yet
        if (done) trace(top->CoreN->uIFU->pc, top->CoreN->uIFU->nextPC, top->CoreN->uIFU->instruction);
        clk_tick();
        // Due to the cpu being multiple cycle now, we need to have a flag to tell when we can do
        // difftest. We set the done signal when EX stage is done. It will be set at the beginning
        // of the clock cycle after verilator evaluate the signal. So we should run difftest on
        // the beginning of next clock so that all the data has been commited
        if (done) {
            difftest(top->CoreN->uIFU->pc);
            done = 0;
        }
        if (top->CoreN->uEXU->done) done = 1;
        update_device();
        check();
        cnt++;
    }
    return finished;
}

word_t CoreN::reg_id2val(int id) {
    return top->CoreN->uIDU->rf->regs[id];
}

// ---------------------------------------------
// DPI function
// ---------------------------------------------

extern "C" {

    void dpi_set_ebreak() {
        dpi_ebreak = true;
    }

    void dpi_pmem_read(int pc, int addr, int *rdata, svBit ifetch) {
        *rdata = paddr_read(addr, ifetch);
        dpi_mem_access_pc = pc;
    }

    void dpi_pmem_write(int pc, int addr, int data, char strb) {
        paddr_write(addr, data, strb);
        dpi_mem_access_pc = pc;
    }

    void dpi_strace(int pc, int code) {
    #ifdef CONFIG_STRACE
        strace_write(pc, code);
    #endif
    }
}
