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

#ifndef __DUT_H__
#define __DUT_H__

#include <verilated.h>
#include <verilated_vcd_c.h>
#include "common.h"
#include "config.h"

class Dut {

public:
    VerilatedVcdC *m_trace;     // Waveform trace
    vluint64_t sim_time;        // simulation time
    word_t regs[NUM_REG];
    const test_info *info;
    bool finished;
    bool pass;

    Dut(int argc, char *argv[], const test_info *info);
    ~Dut();

    virtual void init_trace(const char *name, int level)=0;
    void dump();

    // common simulation task
    virtual void reset()=0;
    virtual void clk_tick()=0;
    virtual bool run(uint64_t step)=0;
    virtual void trace(word_t pc, word_t nxtpc, word_t inst);
    virtual void difftest(word_t pc);
    virtual void check();
    virtual bool report();

    // register access function
    virtual word_t reg_str2val(const char *s);
    virtual word_t reg_id2val(int id)=0;
    void read_reg();
    void report_reg();
};

#endif

