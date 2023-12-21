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

#ifndef __CPUENV_H__
#define __CPUENV_H__

#include <verilated.h>
#include <verilated_vcd_c.h>
#include "common.h"

class Top {

public:
    VerilatedVcdC *m_trace;     // Waveform trace
    vluint64_t sim_time;        // simulation time
    bool trace;
    const char *name;
    bool finished;
    bool success;

    Top(int argc, char *argv[], const char *name, bool trace);
    ~Top();

    virtual void init_trace(const char *name, int level)=0;
    void dump();

    virtual void reset()=0;
    virtual void clk_tick()=0;
    virtual bool run(int step)=0;
    virtual void report()=0;
};

inline void Top::dump() {
    if (m_trace) {
        m_trace->dump(sim_time);
    }
}

#endif

