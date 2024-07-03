/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/19/2023
 *
 * ------------------------------------------------------------------------------------------------
 *  CoreN class: Provide environment for CPU design
 * ------------------------------------------------------------------------------------------------
 */

#ifndef __CORE_H__
#define __CORE_H__


#include <verilated.h>
#include <verilated_vcd_c.h>
#include "VCoreNSoC.h"
#include "VCoreNSoC_CoreNSoC.h"
#include "VCoreNSoC_CoreN.h"
#include "VCoreNSoC_IFU.h"
#include "VCoreNSoC_IDU.h"
#include "VCoreNSoC_EXU.h"
#include "VCoreNSoC_RegisterFile.h"
#include "VCoreNSoC__Dpi.h"
#include "Dut.h"

#define TOP  CoreNSoC
#define VTOP VCoreNSoC

#define PC              top->CoreNSoC->core->uIFU->pc
#define NEXT_PC         top->CoreNSoC->core->uIFU->nextPC
#define INSTRUCTION     top->CoreNSoC->core->uIFU->instruction
#define DONE            top->CoreNSoC->core->uEXU->done
#define REGS            top->CoreNSoC->core->uIDU->rf->regs

class TOP: public Dut {

private:
    VTOP *top;
    int reset_cycle = 10;

public:
    TOP(int argc, char *argv[], const test_info *info);
    ~TOP();

    virtual void init_trace(const char *name, int level);
    virtual void reset();
    virtual void clk_tick();
    virtual bool run(uint64_t step);
    virtual word_t reg_id2val(int id);
};

#endif
