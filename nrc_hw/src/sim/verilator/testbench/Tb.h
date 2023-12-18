// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/17/2023
// ------------------------------------------------------------------------------------------------
// Tb: Base testbench class
// ------------------------------------------------------------------------------------------------

#ifndef __TEST_H__
#define __TEST_H__

#include <iostream>
#include "Env.h"

using namespace std;

template <class M>
class Tb {
public:
    Env<M> *env;
    char *name;
    bool success;

    Tb(int argc, char *argv[], char *name);
    ~Tb();
    virtual void init(int rst_cycle=10);
    virtual bool finish()=0;
    virtual bool check()=0;
    virtual bool run_once();
    virtual bool run(int step);
    virtual void report();
};

/**
 * Constructor
 */
template <class M>
Tb<M>::Tb(int argc, char *argv[], char *name) {
    this->name = name;
    env = new Env<M>(argc, argv);
}

/**
 * Destructor
 */
template <class M>
Tb<M>::~Tb() {
    delete env;
}

/**
 * Initialize the test
 */
template <class M>
void Tb<M>::init(int rst_cycle) {
    env->init_env();
    env->reset(rst_cycle);
}

/**
 * Run test for one clock cycle
 */
template <class M>
bool Tb<M>::run_once() {

    env->tick();
    env->dump();

    // check if test is finished or not and check result
    bool finished = finish();
    if (finished) success = check();
    return finished;
}

/**
 * Run test for multiple clock cycle
 * @param step: number of step to run. -1 means running till the test finishes
 */
template <class M>
bool Tb<M>::run(int step) {
    int cnt = 0;
    bool finished = false;

    while(!finished && (step < 0 || cnt < step)) {
        finished = run_once();
        cnt++;
    }
    return finished;
}

/**
 * Report test result
 */
template <class M>
void Tb<M>::report() {
    cout<<"Tb "<<name<<": "<<success<<endl;
}

#endif
