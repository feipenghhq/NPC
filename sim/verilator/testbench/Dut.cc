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
// C Function prototype
// ---------------------------------------------

extern "C" {
    int reg_str2id(const char *);
    const char *reg_id2str(int id);
    void ref_exec(uint64_t n, word_t *dut_reg, word_t *dut_pc);
    bool difftest_compare(word_t *dut_reg, word_t dut_pc);
    void init_trace(const char *elf);
    void close_trace();
    void print_trace();
    void itrace_write(word_t pc, word_t inst);
    void ftrace_write(word_t pc, word_t nxtpc, word_t inst);
}

// ---------------------------------------------
// Function prototype and global variable
// ---------------------------------------------

bool check_finish(Dut *top, const char *suite);
bool check_pass(Dut *top, const char *suite);

// ---------------------------------------------
// Class functions
// ---------------------------------------------

Dut::Dut(int argc, char *argv[], const test_info *info) {
    Verilated::commandArgs(argc, argv);
    this->info = info;
    sim_time = 0;
    m_trace = NULL;
    finished = false;
    pass = false;
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
#ifdef CONFIG_WAVE
    if (m_trace && sim_time >= CONFIG_WAVE_START && sim_time <= CONFIG_WAVE_END) {
        m_trace->dump(sim_time);
    }
#endif
}

void Dut::check() {
    // only checks if test is not finished.
    // test might be terminated by difftest if there are errors
    if (!finished) {
        finished = check_finish(this, info->suite);
        if (finished) {
            pass = check_pass(this, info->suite);
        }
    }
}

bool Dut::report() {
    log_info("Test finished at %ld cycle.", sim_time);
    if (pass) {
        log_info_color("Test PASS!", ANSI_FG_GREEN);
    }
    else {
        print_trace();
        report_reg();
        log_err("Test FAIL!");
    }
    return pass;
}


void Dut::read_reg() {
    for (int i = 0; i < NUM_REG; i++) {
        regs[i] = reg_id2val(i);
    }
    regs[0] = 0;
}

void Dut::report_reg() {
    Log("Dump register value\n");
    Log("     Reg      Value\n");
    Log("     -------- ----------\n");
    for (int i = 1; i < NUM_REG; i++) {
        Log("    %3s (%2d): 0x%08x\n", reg_id2str(i), i, reg_id2val(i));
        regs[i] = reg_id2val(i);
    }
}

void Dut::trace(word_t pc, word_t nxtpc, word_t inst) {
#ifdef CONFIG_ITRACE
    itrace_write(pc, inst);
#endif
#ifdef CONFIG_FTRACE
    ftrace_write(pc, nxtpc, inst);
#endif
}

void Dut::difftest(word_t pc) {
#ifdef CONFIG_DIFFTEST
    read_reg(); // read the register from DUT
    ref_exec(1, regs, &pc);
    bool diffresult = difftest_compare(regs, pc);
    if (!diffresult) {
        pass = false;
        finished = true;
    }
#endif
}
