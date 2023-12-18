/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/16/2023
 *
 * ------------------------------------------------------------------------------------------------
 * Env: Environment Class for the verilator testbench
 * ------------------------------------------------------------------------------------------------
 */

#ifndef __ENV_H__
#define __ENV_H__

#include <verilated.h>
#include <verilated_vcd_c.h>

// -------------------------------
// Class definition
// -------------------------------

template <class M> class Env {
public:

    VerilatedVcdC *m_trace;     // Waveform trace
    M *dut;                     // DUT module
    vluint64_t sim_time;        // simulation time

    Env(int argc, char *argv[]);
    virtual ~Env();

    void init_trace(const char *name="waveform.vcd", int level=99);
    virtual void init_env();
    virtual void reset(int cycle=10);
    virtual void tick();
    virtual void dump();
};

// -------------------------------
// Class Implementation
// -------------------------------

/**
 * Constructor
 */
template <class M>
Env<M>::Env(int argc, char *argv[]) {
    Verilated::commandArgs(argc, argv);
    dut = new M;
    sim_time = 0;
    m_trace = NULL;
}

/**
 * Destructor
 */
template <class M>
Env<M>::~Env() {
    delete m_trace;
    delete dut;
}

/**
 * Initialize the waveform trace
 */
template <class M>
void Env<M>::init_trace(const char *name, int level) {
    // FIXME: trace does not exist if --trace is not added in verilator
    // Need to figure out how to only run this when --trace is enabled
    /*
    Verilated::traceEverOn(true);
    m_trace = new VerilatedVcdC;
    top->dut->trace(m_trace, level);
    m_trace->open(name);
    */
}

/**
 * Initialize the environment
 */
template <class M>
void Env<M>::init_env() {
    this->init_trace();
}

/**
 * Reset the design
 */
template <class M>
void Env<M>::reset(int cycle) {
    dut->rst_b = 0;
    for (int i = 0; i < cycle; i++)
        this->tick();
    dut->rst_b = 1;
}

/**
 * tick the clock for one cycle
 */
template <class M>
void Env<M>::tick() {
    sim_time++;
    dut->clk = 0;
    dut->eval();
    dut->clk = 1;
    dut->eval();
}

/**
 * Dump the waveform
 */
template <class M>
void Env<M>::dump() {
    // FIXME: trace does not exist if --trace is not added in verilator
    // Need to figure out how to only run this when --trace is enabled
    /*
    if (!m_trace) m_trace->dump(sim_time);
    */
}

#endif
