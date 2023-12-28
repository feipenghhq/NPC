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

#define PC_RESET_OFFSET 0x80000000
#define MEM_OFFSET      0x80000000
#define MSIZE           0x80000000

#define CONFIG_ITRACE
#define CONFIG_IRINGBUF_SIZE 128
#define CONFIG_IRINGBUF_LEN  8

#define CONFIG_MTRACE

#endif
