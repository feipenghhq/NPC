# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 01/05/2023
# ------------------------------------------------------------------------------------------------

## Makefile for simulation flow

all: $(OBJECT)

## --------------------------------------------------------
## Directories
## --------------------------------------------------------

OUTPUT_DIR= $(WORK_DIR)/output/sim
BUILD_DIR = $(OUTPUT_DIR)/build
VERIL_DIR = $(WORK_DIR)/sim/verilator

$(mkdir -p $(OUTPUT_DIR))
$(mkdir -p $(BUILD_DIR))

## --------------------------------------------------------
## Tool
## --------------------------------------------------------

CC = gcc
AR = ar

## --------------------------------------------------------
## C source files
## --------------------------------------------------------

C_SRCS += $(shell realpath $(shell find $(VERIL_DIR) -name "*.c") --relative-to .)

C_HDRS += $(shell find $(VERIL_DIR) -name "*.h")
C_HDRS += $(shell find include/generated -name "*.h")

C_INCS += $(sort $(dir $(C_HDRS)))

C_OBJS += $(patsubst %.c,$(BUILD_DIR)/%.o,$(C_SRCS))

## --------------------------------------------------------
## C Build flags
## --------------------------------------------------------

CFLAGS += -g -Wall -O2 -Werror -rdynamic -MMD
CFLAGS += -mcmodel=large
CFLAGS += $(shell sdl2-config --cflags)
CFLAGS += $(shell llvm-config --cflags)
CFLAGS += $(addprefix -I,$(C_INCS))

LDFLAGS += $(shell sdl2-config --libs)
LDFLAGS += $(shell llvm-config --ldflags --libs)
LDFLAGS +=-lreadline

## --------------------------------------------------------
## Build the C source file to a library
## --------------------------------------------------------

C_TARGET = $(BUILD_DIR)/verilator_c_lib.a

$(C_TARGET): $(C_OBJS)
	@echo +AR "->" $(shell realpath $@ --relative-to .)
	@$(AR) rcs $@ $(C_OBJS)

$(BUILD_DIR)/%.o: %.c $(C_HDRS)
	@mkdir -p $(dir $@)
	@echo +CC $<
	@$(CC) $(CFLAGS) -c -o $@ $<

## --------------------------------------------------------
## RTL source files CPP source files
## --------------------------------------------------------

RTL_SRCS += $(VERILOG_SRCS)
RTL_SRCS += $(addprefix -I,$(VERILOG_INCS))

CXX_SRCS += $(shell find $(VERIL_DIR) -name "*.cc")

CXX_INCS += include/generated \
			$(VERIL_DIR)/include \
			$(VERIL_DIR)/include/testbench

VERIL_SRCS += $(CXX_SRCS)

## --------------------------------------------------------
## Verilator Flags
## --------------------------------------------------------

## verilator build flags and options
VFLAGS += --x-assign unique --x-initial unique
VFLAGS += --cc --exe -j 0
VFLAGS += --Mdir $(BUILD_DIR) --top-module $(TOP)
VFLAGS += --trace
VFLAGS += -O3
VFLAGS += -CFLAGS  "$(addprefix -I, $(abspath $(CXX_INCS)))"
VFLAGS += -LDFLAGS "$(LDFLAGS)"

## --------------------------------------------------------
## Build Verilator Executable
## --------------------------------------------------------

OBJECT = V$(TOP)
VPASS  = $(BUILD_DIR)/.VPASS
BPASS  = $(BUILD_DIR)/.BPASS
REF_SO = $(VERIL_DIR)/difftest/nemu/riscv32-nemu-interpreter-so

build: $(OBJECT)

$(OBJECT): $(BPASS)

$(BPASS): $(VPASS)
	$(info --> Building Verilator Executable)
	@rm -f $(BUILD_DIR)/$(OBJECT)
	@$(MAKE) V$(TOP) -C $(BUILD_DIR) -f V$(TOP).mk -s && touch $@

### Compile the RTL and TB
verilating: $(VPASS)

$(VPASS): $(VERILOG_SRCS) $(CXX_SRCS) $(C_TARGET)
	$(info --> Verilatring)
	@verilator $(VFLAGS) $(RTL_SRCS) $(VERIL_SRCS) $(C_TARGET) && touch $@

### Lint the RTL
lint: $(VERILOG_SRCS)
	$(info --> Linting RTL)
	@verilator --lint-only $(RTL_OPTS) $(RTL_SRCS)

## --------------------------------------------------------
## Set the test suite and command to run simulation
## --------------------------------------------------------

COLOR_RED   = \033[1;31m
COLOR_GREEN = \033[1;32m
COLOR_NONE  = \033[0m

TEST_SUITES ?= ics-am-test

### File to store test result
RESULT = $(OUTPUT_DIR)/.result
$(shell mkdir -p $(OUTPUT_DIR))
$(shell > $(RESULT))

### Define function to run simulation. Usage: $(call run_sim,image,elf,suite,test,dut,maxlen)
define run_sim
	@/bin/echo -e " \
		run:\n\tcd $(OUTPUT_DIR) && $(BUILD_DIR)/$(OBJECT) \
		--image $(1) --elf $(2) --suite $(3) --test $(4) --dut $(5) --ref $(REF_SO) \
		" \
		>> $(OUTPUT_DIR)/makefile.$(4)
	@if make -s -f $(OUTPUT_DIR)/makefile.$(4); then \
		printf "[%$(6)s] $(COLOR_GREEN)%s!$(COLOR_NONE)\n" $(4) PASS >> $(RESULT); \
	else \
		printf "[%$(6)s] $(COLOR_RED)%s!$(COLOR_NONE)\n" $(4) FAIL >> $(RESULT); \
	fi
	-@rm $(OUTPUT_DIR)/makefile.$(4)
endef

### Include the test suites makefile
include $(VERIL_DIR)/scripts/$(TEST_SUITES).mk

## --------------------------------------------------------
## MISC
## --------------------------------------------------------

sim.clean:
	@rm -rf $(OUTPUT_DIR)
