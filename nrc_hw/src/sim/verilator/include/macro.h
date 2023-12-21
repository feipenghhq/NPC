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

#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <assert.h>

// -------------------------------
// Debug Macro
// ------------------------------
// Reference: <Learn C the hard way> ZED'S AWESOME DEBUG MACROS

#define clean_errno() (errno == 0 ? "None": strerror(errno))

#define log_err(msg, ...) fprintf(stderr, "[ERROR] (%s:%d: errno: %s) " msg "\n", \
        __FILE__, __LINE__, clean_errno(), ##__VA_ARGS__)

#define log_warn(msg, ...) fprintf(stderr, "[WARN] (%s:%d: errno: %s) " msg "\n", \
        __FILE__, __LINE__, clean_errno(), ##__VA_ARGS__)

#define log_info(msg, ...) fprintf(stderr, "[INFO] (%s:%d) " msg "\n", \
        __FILE__, __LINE__, ##__VA_ARGS__)

#define Check(cond, msg, ...) do {if(!(cond)) {log_err(msg, ##__VA_ARGS__); errno=0; exit(1);}} while(0)

#endif
