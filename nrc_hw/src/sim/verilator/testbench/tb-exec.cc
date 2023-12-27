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
#include "tb.h"
#include <getopt.h>

// ------------------------------------
// Function prototype, global variable
// ------------------------------------

static test_info info;

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
    printf("\t-w,--wave             Dump the waveform trace\n");
    printf("\n");
}

#define check_arg(arg, name, err) \
    do { \
        if (!(arg)) { \
            fprintf(stderr, "[ERROR] Missing argument: %s\n", name); \
            err = true;} \
    } while(0)

/**
 * Check if all the required args has been specified
 */
static void check_args(const char *prog) {
    bool err = false;
    check_arg(info.image, "image", err);
    check_arg(info.suite, "suite", err);
    check_arg(info.test, "test", err);
    check_arg(info.dut, "dut", err);
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
        {"wave",  no_argument      , 0, 'w'},
        // Add more option here if needed
        {0      , 0                , 0,  0 },
    };
    const char *optstring = "i:s:t:w";
    int c = -1; // assign c to -1 so if argc < 2 switch will go to default
    while(argc < 2 || (c = getopt_long(argc, argv, optstring, long_options, NULL)) != -1) {
        switch(c) {
            case 'i': info.image = optarg; break;
            case 's': info.suite = optarg; break;
            case 't': info.test = optarg; break;
            case 'd': info.dut = optarg; break;
            case 'w': info.trace = true; break;
            default:
                print_usage(argv[0]);
                exit(0);
        }
    };
    check_args(argv[0]);
    return 0;
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
    Dut *dut = select_dut(argc, argv, &info);
    load_image(info.image);

    dut->init_trace("waveform.vcd", 99);
    dut->reset();
    dut->run(-1); // run till the end of the test
    bool success = dut->report();
    delete dut;
    return success;
}