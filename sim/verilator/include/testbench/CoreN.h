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

#ifndef __CORE_N_H__
#define __CORE_N_H__

#include <verilated.h>
#include <verilated_vcd_c.h>
#include "VCoreN.h"
#include "VCoreN_CoreN.h"
#include "VCoreN_IFU.h"
#include "VCoreN_IDU.h"
#include "VCoreN_RegisterFile.h"
#include "VCoreN__Dpi.h"
#include "Dut.h"

class CoreN: public Dut {

private:
    VCoreN *top;
    int reset_cycle = 10;

public:
    CoreN(int argc, char *argv[], const test_info *info);
    ~CoreN();

    virtual void init_trace(const char *name, int level);
    virtual void reset();
    virtual void clk_tick();
    virtual bool run(uint64_t step);
    virtual word_t reg_id2val(int id);
};

#endif
