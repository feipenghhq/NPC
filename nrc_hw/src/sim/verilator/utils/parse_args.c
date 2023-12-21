// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/20/2023
// ------------------------------------------------------------------------------------------------
// Parse argument
// ------------------------------------------------------------------------------------------------

#include <getopt.h>
#include "utils.h"

argu_s argu = {.image=NULL, .suite=NULL, .test=NULL, .dut=NULL, .trace=false};

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

static void check_args(const char *prog) {
    bool err = false;
    check_arg(argu.image, "image", err);
    check_arg(argu.suite, "suite", err);
    check_arg(argu.test, "test", err);
    check_arg(argu.dut, "dut", err);
    if (err) {
        printf("\n");
        print_usage(prog);
        exit(0);
    }
}

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
            case 'i': argu.image = optarg; break;
            case 's': argu.suite = optarg; break;
            case 't': argu.test = optarg; break;
            case 'd': argu.dut = optarg; break;
            case 'w': argu.trace = true; break;
            default:
                print_usage(argv[0]);
                exit(0);
        }
    };
    check_args(argv[0]);
    return 0;
}

