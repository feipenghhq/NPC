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
// https://raywang.tech/2017/12/04/Using-the-LLVM-MC-Disassembly-API/
// https://llvm.org/doxygen/group__LLVMCDisassembler.html#gab2235be6ece819e49dbde7cd52c3a2d8

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
const char unknow[] = "     ???";

void init_disasm() {
    LLVMInitializeAllTargetInfos();
    LLVMInitializeAllTargetMCs();
    LLVMInitializeAllAsmParsers();
    LLVMInitializeAllDisassemblers();
    dc = LLVMCreateDisasm("riscv64", NULL, 0, NULL, NULL);
}

char *disasm(word_t *inst, word_t pc) {
    size_t rc = LLVMDisasmInstruction(dc, (uint8_t*) inst, 4, pc, instbuf, INST_LEN);
    //Check(rc, "Failed to disassemble instruction 0x%08x", *inst);
    if (!rc) {
        strcpy(instbuf, unknow);
    }
    return instbuf;
}

#undef INST_LEN

#endif
