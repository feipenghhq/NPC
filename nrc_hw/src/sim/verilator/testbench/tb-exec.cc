// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/20/2023
// ------------------------------------------------------------------------------------------------
// tb: test bench
// ------------------------------------------------------------------------------------------------

#include "testbench/Core_s.h"
#include "memory.h"
#include "common.h"
#include "config.h"
#include <getopt.h>

// ------------------------------------
// Function prototype, global variable
// ------------------------------------

static test_info info = {
    .trace=false,
    .elf=NULL,
    .ref=NULL,
};

// File pointer for log
const char itrace_log[] = "itrace.log";
const char mtrace_log[] = "mtrace.log";
const char ftrace_log[] = "ftrace.log";
const char log_name[]   = "run.log";

FILE *itrace_fp = NULL;
FILE *mtrace_fp = NULL;
FILE *ftrace_fp = NULL;
FILE *log_fp = NULL;

// ------------------------------------
// Functions
// ------------------------------------

/**
 * Print program usage
 */
static void print_usage(const char *prog) {
    printf("Usage: \n\t%s args [options]\n\n", prog);
    printf("REQUIRED ARGS\n\n");
    printf("\t-i,--image IMAGE      Binary Image file for the program\n");
    printf("\t-s,--suite SUITE      Test Suite\n");
    printf("\t-t,--test TEST        Test Name\n");
    printf("\t-d,--dut DUT          DUT Top module name\n");
    printf("\t--wave                Dump the waveform trace\n");
    printf("\t--elf ELF             ELF file for the program\n");
    printf("\t--ref REF_SO          Reference for diff test\n");
    printf("\n");
}

#define check_arg(arg, name, err) \
    do { \
        if (!(arg)) { \
            printf("[ERROR] Missing argument: %s\n", name); \
            err = true;} \
    } while(0)

/**
 * Check if all the required args has been specified
 */
static void check_args(const char *prog) {
    bool err = false;
    check_arg(info.image, "image", err);
    check_arg(info.suite, "suite", err);
    check_arg(info.test,  "test",  err);
    check_arg(info.dut,   "dut",   err);
    if (err) {
        printf("\n");
        print_usage(prog);
        exit(0);
    }
}

/**
 * Parse argument
 */
int parse_args(int argc, char *argv[]) {
    const struct option long_options[] = {
        {"image", required_argument, 0, 'i'},
        {"suite", required_argument, 0, 's'},
        {"test",  required_argument, 0, 't'},
        {"dut",   required_argument, 0, 'd'},
        {"wave",  no_argument      , 0, '0'},
        {"elf",   required_argument, 0, '1'},
        {"ref",   required_argument, 0, '2'},
        // Add more option here if needed
        {0      , 0                , 0,  0 },
    };
    const char *optstring = "i:s:t:d:";
    int c = -1; // assign c to -1 so if argc < 2 switch will go to default
    while(argc < 2 || (c = getopt_long(argc, argv, optstring, long_options, NULL)) != -1) {
        switch(c) {
            case 'i': info.image = optarg; break;
            case 's': info.suite = optarg; break;
            case 't': info.test = optarg; break;
            case 'd': info.dut = optarg; break;
            case '0': info.trace = true; break;
            case '1': info.elf = optarg; break;
            case '2': info.ref = optarg; break;
            default:
                print_usage(argv[0]);
                exit(0);
        }
    };
    check_args(argv[0]);
    return 0;
}

/**
 * Initialize the log files
 */
static void init_log() {
#ifdef CONFIG_ITRACE
    itrace_fp = fopen(itrace_log, "w");
    Check(itrace_fp, "Failed to open %s", itrace_log);
#endif
#ifdef CONFIG_MTRACE
    mtrace_fp = fopen(mtrace_log, "w");
    Check(mtrace_fp, "Failed to open %s", mtrace_log);
#endif
#ifdef CONFIG_FTRACE
    ftrace_fp = fopen(ftrace_log, "w");
    Check(ftrace_fp, "Failed to open %s", ftrace_log);
#endif
    log_fp = fopen(log_name, "w");
    assert(log_fp);
}

static void close_log() {
    if (itrace_fp) fclose(itrace_fp);
    if (mtrace_fp) fclose(mtrace_fp);
    if (ftrace_fp) fclose(ftrace_fp);
    if (log_fp) fclose(log_fp);
    // remove ANSI color coding in log file
    char cmd[] = "sed -i 's/\x1b\[[0-9;]*m//g' run.log"; // Note: the log name is hard coded here
    int rc = system(cmd);
}


/**
 * Select and create different top based on the DUT
 */
static Dut *select_dut(int argc, char *argv[], test_info *info) {
    Dut *dut = NULL;
    if (strcmp(info->dut, DUT_CORE_S) == 0)
        dut = new Core_s(argc, argv, info);
    else {
        log_err("Undefined dut: %s", info->dut);
        exit(0);
    }
    return dut;
}

/**
 * Function to run the test
 */

int tb_exec(int argc, char *argv[]) {

    parse_args(argc, argv);
    init_log();
    Dut *dut = select_dut(argc, argv, &info);
    size_t mem_size = load_image(info.image);
#ifdef CONFIG_ITRACE
    void init_disasm();
    init_disasm();
#endif
#ifdef CONFIG_FTRACE
    void init_ftrace(const char *elf);
    init_ftrace(info.elf);
#endif
#ifdef CONFIG_DIFFTEST
    void init_difftest(char *ref, size_t mem_size);
    init_difftest(info.ref, mem_size);
#endif

    dut->init_trace("waveform.vcd", 99);
    dut->reset();
    dut->run(-1); // run till the end of the test
    bool success = dut->report();

    close_log();
    delete dut;
    return success;
}
