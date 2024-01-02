/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/18/2023
 *
 * ------------------------------------------------------------------------------------------------
 * Macro
 * ------------------------------------------------------------------------------------------------
 */


#ifndef __MACRO_H__
#define __MACRO_H__

#include "debug.h"

#define DUT_CORE_S "core_s"

#define ARRLEN(arr) (sizeof(arr) / sizeof(arr[0]))

#define likely(x)      __builtin_expect((x), 1)
#define unlikely(x)    __builtin_expect((x), 0)

#endif
