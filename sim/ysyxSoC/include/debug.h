/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 07/15/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Debug Macro
 * ------------------------------------------------------------------------------------------------
 */

#ifndef __DEBUG_H__
#define __DEBUG_H__

#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <assert.h>

#define ANSI_FG_RED     "\33[1;31m"
#define ANSI_FG_GREEN   "\33[1;32m"
#define ANSI_FG_YELLOW  "\33[1;33m"
#define ANSI_FG_BLUE    "\33[1;34m"
#define ANSI_FG_WHITE   "\33[1;37m"
#define ANSI_NONE       "\33[0m"

#define ANSI_FMT(str, fmt) fmt str ANSI_NONE

#define log_write(...) \
    ;
    //do { \
    //    extern FILE *log_fp; \
    //    fprintf(log_fp, __VA_ARGS__); \
    //    fflush(log_fp); \
    //} while (0)

#define Log(...) \
    do { \
        printf(__VA_ARGS__); \
        log_write(__VA_ARGS__); \
    } while (0)

// Reference: <Learn C the hard way> ZED'S AWESOME DEBUG MACROS

#define clean_errno() (errno == 0 ? "None": strerror(errno))

#define log_err(msg, ...) \
    Log(ANSI_FMT("[ERROR] (%s:%d %s: errno: %s) " msg "\n", ANSI_FG_RED), \
    basename(__FILE__), __LINE__, __func__, clean_errno(), ##__VA_ARGS__)

#define log_warn(msg, ...) \
    Log(ANSI_FMT("[WARN] (%s:%d %s: errno: %s) " msg "\n", ANSI_FG_YELLOW), \
    basename(__FILE__), __LINE__, __func__, clean_errno(), ##__VA_ARGS__)

#define log_info(msg, ...) \
    Log(ANSI_FMT("[INFO] (%s:%d %s) " msg "\n", ANSI_FG_BLUE), \
    basename(__FILE__), __LINE__, __func__, ##__VA_ARGS__)

#define log_info_color(msg, color, ...) \
    do {Log(ANSI_FMT("[INFO] (%s:%d %s) ", ANSI_FG_BLUE), basename(__FILE__), __LINE__, __func__); \
        Log(ANSI_FMT( msg "\n", color), ##__VA_ARGS__); \
    } while(0)

#define Check(cond, msg, ...) \
    do {if(!(cond)) {log_err(msg, ##__VA_ARGS__); errno=0; exit(1);}} while(0)

#define CheckMalloc(cond, ...) Check(cond, "malloc error");

#define Panic(msg, ...) \
    do {log_err(msg, ##__VA_ARGS__); errno=0; exit(1);} while(0)

#endif
