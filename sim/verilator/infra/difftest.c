// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/27/2023
// ------------------------------------------------------------------------------------------------
// difftest: Diff test
// ------------------------------------------------------------------------------------------------

#include <dlfcn.h>
#include "common.h"
#include "config.h"

#ifdef CONFIG_DIFFTEST

// ------------------------------------
// Function prototype, global variable
// ------------------------------------

#define CONFIG_DIFFTEST_VERBOSE

byte_t *mem_ptr();
void init_ref(size_t mem_size);
const char *reg_id2str(int id);

static void *lib = NULL;
static bool is_skip_ref = false;

enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };

typedef void (*difftest_init_t)(int port);
typedef void (*difftest_memcpy_t) (uint32_t addr, void *buf, size_t n, bool direction);
typedef void (*difftest_exec_t)(uint64_t n);
typedef void (*difftest_regcpy_t)(void *reg, void *pc, bool direction);

static difftest_init_t   difftest_init;
static difftest_memcpy_t difftest_memcpy;
static difftest_exec_t   difftest_exec;
static difftest_regcpy_t difftest_regcpy;
// ------------------------------------
// Functions
// ------------------------------------

#define load_from_so(name) \
    name = (name##_t) dlsym(lib, #name); \
    Check(name, "Failed to load from shared lib")

void init_difftest(char *ref, size_t mem_size) {
    lib = dlopen(ref, RTLD_NOW);
    Check(lib, "Failed to open shared lib: %s. %s.", ref, dlerror());
    load_from_so(difftest_init);
    load_from_so(difftest_memcpy);
    load_from_so(difftest_exec);
    load_from_so(difftest_regcpy);
    init_ref(mem_size);
}


/**
 * initialize the reference model
 */
void init_ref(size_t mem_size) {
    difftest_init(0);
    difftest_memcpy(MEM_BASE, mem_ptr(), mem_size, DIFFTEST_TO_REF);
    log_info("Initialized difftest");
}

/**
 * Set to skip the execution in reference model
 */
void difftest_skip_ref() {
    is_skip_ref = true;
}


/**
 * execute the reference model
 */
void ref_exec(uint64_t n, word_t *dut_reg, word_t *dut_pc) {
    Check(n == 1, "Only support execute ref one step");
    if (is_skip_ref) {
        difftest_regcpy(dut_reg, dut_pc, DIFFTEST_TO_REF);
        is_skip_ref = false;
    }
    else {
        difftest_exec(n);
    }
}

/**
 * Compare the result between dut and ref
 * @param dut_pc: the DUT PC value of the next instruction (because we have already completed this instruction)
 */
bool difftest_compare(word_t *dut_reg, word_t dut_pc ) {
    bool pass = true;
    word_t ref_reg[NUM_REG];
    word_t ref_pc;
    difftest_regcpy(ref_reg, &ref_pc, DIFFTEST_TO_DUT);
    // make sure that the pc of the instruction being compared is the same
    if (ref_pc != dut_pc) {
        log_err("difftest: PC of the compared instruction mismatch. Ref: 0x%08x. Dut: 0x%08x",
                ref_pc, dut_pc);
        return false;
    }
    // make sure register is the same
    for (int i = 0; i < NUM_REG; i++) {
      if (ref_reg[i] != dut_reg[i]) {
        log_err("difftest: Register Value mismatch after executing instruction on PC: 0x%08x. "
                "Reg %s ($%d). Ref: 0x%08x. Dut: 0x%08x",
                dut_pc, reg_id2str(i), i, ref_reg[i], dut_reg[i]);
        pass = false;
      }
    }

    return pass;
}

#endif
