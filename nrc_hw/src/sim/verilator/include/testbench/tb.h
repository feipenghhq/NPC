/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/22/2023
 *
 * ------------------------------------------------------------------------------------------------
 *  Header file for tb related functions
 * ------------------------------------------------------------------------------------------------
 */

#ifndef __TB_H__
#define __TB_H__

#include "Dut.h"

bool check_finish(Dut *top, const char *suite);

bool check_pass(Dut *top, const char *suite);

#endif
