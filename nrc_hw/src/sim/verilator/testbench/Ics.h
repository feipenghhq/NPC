// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/17/2023
// ------------------------------------------------------------------------------------------------
// Ics: testbench class for tests from NJU ICS Lab
// https://nju-projectn.github.io/ics-pa-gitbook/ics2023/
// ------------------------------------------------------------------------------------------------

#ifndef __ICS_H__
#define __ICS_H__

#include "Tb.h"

template <class M> class Ics: public Tb<M> {
public:
    Ics(int argc, char *argv[], char *name);
    virtual bool finish();
    virtual bool check();
};
template <class M>
Ics<M>::Ics(int argc, char *argv[], char *name): Tb<M>(argc, argv, name) {}

template <class M>
bool Ics<M>::finish() {
    return false;
}

template <class M>
bool Ics<M>::check() {
    return false;
}

#endif
