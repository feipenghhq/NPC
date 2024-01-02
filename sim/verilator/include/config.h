/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/18/2023
 *
 * ------------------------------------------------------------------------------------------------
 * Configuration
 * ------------------------------------------------------------------------------------------------
 */

#ifndef __CONFHG_H__
#define __CONFIG_H__

//----------------------------------------------
// CPU
//----------------------------------------------
#define PC_RESET_OFFSET 0x80000000
#define NUM_REG         32
#define XLEN            32
#define NUM_BYTE        4

//----------------------------------------------
// Memory
//----------------------------------------------
#define MEM_BASE        0x80000000
#define MSIZE           0x8000000
#define MEM_END         (MEM_BASE + MSIZE - 1)

//----------------------------------------------
// Debug
//----------------------------------------------
#define CONFIG_ITRACE
#define CONFIG_IRINGBUF_SIZE 128
#define CONFIG_IRINGBUF_LEN  8
#define CONFIG_MTRACE
#define CONFIG_FTRACE
#define CONFIG_DIFFTEST

#endif
