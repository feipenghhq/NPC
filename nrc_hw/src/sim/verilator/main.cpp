// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/17/2023
// ------------------------------------------------------------------------------------------------
// Main function for the core_s
// ------------------------------------------------------------------------------------------------

#include "utils.h"

extern argu_s argu;
int run_test(int, char *[], const argu_s *);

int main(int argc, char *argv[]) {
    parse_args(argc, argv);
    run_test(argc, argv, &argu);
    return 0;
}

