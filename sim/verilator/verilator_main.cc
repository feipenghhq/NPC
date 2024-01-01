// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/17/2023
// ------------------------------------------------------------------------------------------------
// Main function for the verilator
// ------------------------------------------------------------------------------------------------

#include "utils.h"

int tb_exec(int, char *[]);

int main(int argc, char *argv[]) {
    bool success;
    success = tb_exec(argc, argv);
    return !success; // return 0 if test pass
}

