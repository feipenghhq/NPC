// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 12/27/2023
// ------------------------------------------------------------------------------------------------
// ftrace: Function trace
// ------------------------------------------------------------------------------------------------

#include <elf.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <assert.h>
#include "common.h"
#include "config.h"

#ifdef CONFIG_FTRACE

#define OPCODE(inst)  ((inst)  & 0x0000007F)
#define RD(inst)      (((inst) & 0x00000F80) >> 7)
#define RS1(inst)     (((inst) & 0x000F8000) >> 15)
#define JAL           0x6F
#define JALR          0x67

// use a linked list data structure to store the function information
struct func_info {
    uint32_t start;
    uint32_t end;
    char *name;
    struct func_info *next;
};

extern FILE *ftrace_fp;
static int level = 0;
static char unknow[] = "???";
static struct func_info *start = NULL;

static void find_func_info(const char *);
void print_func_info();

void init_ftrace(const char *elf) {
  find_func_info(elf);
}

/**
 * find all the functions from elf file
 */
static void find_func_info(const char *elf) {

    log_info("Read ELF file for ftrace: %s.", elf);
    // open the file and get the file size
    struct stat statbuf;
    int fd = open(elf, O_RDONLY);
    Check(fd, "Can open file %s.\n", elf);
    int rc = fstat(fd, &statbuf);
    assert(rc == 0); // fstat return 0 if no error

    // map the file to memory
    char *addr = (char *) mmap(NULL, statbuf.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    Check(addr, "mmap failed to map elf file into memory");

    // read the elf header
    Elf32_Ehdr *elf32_hdr;
    elf32_hdr = (Elf32_Ehdr *) addr;

    // find the # of entries in section header table. See man 5 elf e_shnum for more detail
    size_t _sh_size = ((Elf32_Shdr *) (addr + elf32_hdr->e_shoff))->sh_size;
    size_t e_shnum = elf32_hdr->e_shnum ? elf32_hdr->e_shnum : _sh_size;

    // find the symbol table and symbol string table
    Elf32_Shdr *elf32_shdr, *sym_shdr = NULL, *str_shdr = NULL;
    for (int symtab_idx = 0; symtab_idx < e_shnum; symtab_idx++) {
        elf32_shdr = ((Elf32_Shdr *) (addr + elf32_hdr->e_shoff)) + symtab_idx;
        if (elf32_shdr->sh_type == SHT_SYMTAB) {
            int strtab_idx = elf32_shdr->sh_link;
            sym_shdr = elf32_shdr;
            str_shdr = ((Elf32_Shdr *) (addr + elf32_hdr->e_shoff)) + strtab_idx;
            break;
        }
    }

    // check if we found symbol table and symbol string table
    Check(sym_shdr, "Failed to find symbol table in ELF file %s", elf);
    Check(str_shdr, "Failed to find symbol string table in ELF file %s", elf);

    // get all function in the symbol table
    size_t num_symbol = sym_shdr-> sh_size / sym_shdr -> sh_entsize;
    for (int i = 0; i < num_symbol; i++) {
        Elf32_Sym *sym = ((Elf32_Sym *) (addr + sym_shdr->sh_offset)) + i;
        if (ELF32_ST_TYPE(sym->st_info) == STT_FUNC) {
            char *name = ((char *) (addr + str_shdr->sh_offset)) + sym->st_name;
            struct func_info *node = (struct func_info *) malloc(sizeof(struct func_info));
            assert(node);
            node->start = sym->st_value;
            node->end = sym->st_value + sym->st_size;
            node->name = name;
            node->next = start; // insert to the front of the list
            start = node;
        }
    }
}

void print_func_info() {
    struct func_info *s = start;
    for (; s != NULL; s = s->next) {
        Log("[0x%08x, 0x%08x): %s\n", s->start, s->end, s->name);
    }
}

/**
 * find function name given its address
 */
char *find_func_name(word_t addr) {
  struct func_info *s = start;
  for (; s != NULL; s = s->next) {
    if (addr >= s->start && addr < s->end) {
      return s->name;
    }
  }
  return unknow;
}

// Function call instruction: jal ra label or jalr ra rd imm
static bool is_func_call(word_t inst) {
  unsigned char opcode = OPCODE(inst);
  unsigned char rd = RD(inst);
  return (rd == 1) && ((opcode == JAL) || (opcode == JALR));
}

// Function return is ret instruction which is jalr x0, 0(x1)
static bool is_func_ret(word_t inst) {
  unsigned char opcode = OPCODE(inst);
  unsigned char rd = RD(inst);
  unsigned char rs1 = RS1(inst);
  return (rd == 0) && (rs1 == 1) && (opcode == JALR);
}

static void trace_func_call(word_t pc, word_t nxtpc, word_t inst) {
  if (is_func_call(inst)) {
    fprintf(ftrace_fp, "0x%x: ", pc);
    for (int i = 0; i < level; i++) fprintf(ftrace_fp, "  ");
    fprintf(ftrace_fp, "call [%s@0x%x]\n", find_func_name(nxtpc), nxtpc);
    level++;
  }
}

static void trace_func_ret(word_t pc, word_t nxtpc, word_t inst) {
  if (is_func_ret(inst)) {
    fprintf(ftrace_fp, "0x%x: ", pc);
    for (int i = 0; i < level; i++) fprintf(ftrace_fp, "  ");
    fprintf(ftrace_fp, "ret [%s@0x%x]\n", find_func_name(nxtpc), nxtpc);
    level--;
  }
}

void ftrace_write(word_t pc, word_t nxtpc, word_t inst) {
  trace_func_call(pc, nxtpc, inst);
  trace_func_ret(pc, nxtpc, inst);
}

#undef OPCODE
#undef RD
#undef RS1
#undef JAL
#undef JALR

#endif
