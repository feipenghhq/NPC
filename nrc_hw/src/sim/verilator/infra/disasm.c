// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/28/2023
// ------------------------------------------------------------------------------------------------
// disasm: Disassemble the instruction
// ------------------------------------------------------------------------------------------------

// Reference
// https://stackoverflow.com/questions/21462853/llvmcreatedisasm-returns-null
// https://www.pauladamsmith.com/blog/2015/01/how-to-get-started-with-llvm-c-api.html
// https://llvm.org/doxygen/group__LLVMCDisassembler.html#gab2235be6ece819e49dbde7cd52c3a2d8
// https://raywang.tech/2017/12/04/Using-the-LLVM-MC-Disassembly-API/
//
// For some reason I can't make this file compiling correctly in verilator so I
// packet it as a .a file and then load the .a file in verilator


#include <llvm-c/Disassembler.h>
#include <llvm-c/Target.h>
#include <assert.h>
#include <stdio.h>
#include "common.h"
#include "config.h"

#ifdef CONFIG_ITRACE

#define INST_LEN 33

static LLVMDisasmContextRef dc;
char instbuf[INST_LEN];

void init_disasm() {
    LLVMInitializeAllTargetInfos();
    LLVMInitializeAllTargetMCs();
    LLVMInitializeAllDisassemblers();
    dc = LLVMCreateDisasm("riscv64-linux-gnu", NULL, 0, NULL, NULL);
}

char *disasm(word_t *inst, word_t pc) {
    size_t rc = LLVMDisasmInstruction(dc, (uint8_t*) inst, 4, pc, instbuf, INST_LEN);
    Check(rc, "Failed to disassemble instruction 0x%08x", *inst);
    return instbuf;
}

#undef INST_LEN

#endif
