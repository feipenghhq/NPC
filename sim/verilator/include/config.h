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
#include "autoconf.h"

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
// ICS AM MMIO Map
//----------------------------------------------

// In RISC-V Arch, device is mapped to MMIO
#define MMIO_BASE   0xa0000000

#define SERIAL_PORT     (MMIO_BASE + 0x00003f8)
#define KBD_ADDR        (MMIO_BASE + 0x0000060)
#define RTC_ADDR        (MMIO_BASE + 0x0000048)
#define VGACTL_ADDR     (MMIO_BASE + 0x0000100)
#define AUDIO_ADDR      (MMIO_BASE + 0x0000200)
#define DISK_ADDR       (MMIO_BASE + 0x0000300)
#define FB_ADDR         (MMIO_BASE + 0x1000000)
#define AUDIO_SBUF_ADDR (MMIO_BASE + 0x1200000)

#define MMIO_SIZE       0x1400000
#define MMIO_END        (MMIO_BASE + MMIO_SIZE - 1)

#endif
