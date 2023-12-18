// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/17/2023
// ------------------------------------------------------------------------------------------------
// Main function for the core_s
// ------------------------------------------------------------------------------------------------

#include <Vcore_s.h>
#include "Tb.h"
#include "Ics.h"

int main(int argc, char *argv[]) {

    Tb<Vcore_s> *tb;
    tb = new Ics<Vcore_s>(argc, argv, NULL);
    tb->init();
    tb->run(-1);
    return 0;
}
