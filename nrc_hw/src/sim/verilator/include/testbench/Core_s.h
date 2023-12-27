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

#ifndef __CORE_S_H__
#define __CORE_S_H__

#include <verilated.h>
#include <verilated_vcd_c.h>
#include "Vcore_s.h"
#include "Vcore_s_core_s.h"
#include "Vcore_s_RegFile.h"
#include "Vcore_s__Dpi.h"
#include "Dut.h"

class Core_s: public Dut {

private:
    Vcore_s *top;
    int reset_cycle = 10;

public:
    Core_s(int argc, char *argv[], const test_info *info);
    ~Core_s();

    virtual void init_trace(const char *name, int level);
    virtual void reset();
    virtual void clk_tick();
    virtual bool run(int step);
    virtual word_t reg_id2val(int id);
};

#endif
