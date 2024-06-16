# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 01/05/2023
# ------------------------------------------------------------------------------------------------

## Makefile for simulation flow

## --------------------------------------------------------
## Directories
## --------------------------------------------------------

REPO = $(shell git rev-parse --show-toplevel)
OUTPUT_DIR= $(REPO)/output/sim
VERIL_DIR = $(REPO)/sim/verilator
BUILD_DIR = $(OUTPUT_DIR)/build

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
C_HDRS += $(shell find $(REPO)/include/generated -name "*.h")

C_INCS += $(sort $(dir $(C_HDRS)))

C_OBJS += $(patsubst %.c,$(BUILD_DIR)/%.o,$(C_SRCS))

## --------------------------------------------------------
## C Build flags
## --------------------------------------------------------

CFLAGS += -g -Wall -O2 -Werror -rdynamic -MMD
CFLAGS += -mcmodel=large
CFLAGS += $(shell llvm-config --cflags)
CFLAGS += $(addprefix -I,$(C_INCS))
CFLAGS += $(shell sdl2-config --cflags)

LDFLAGS +=-lreadline
LDFLAGS += $(shell llvm-config --ldflags --libs)
LDFLAGS += $(shell sdl2-config --libs)

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

TOP ?= CoreN

include $(REPO)/core/src/filelist.mk

RTL_SRCS += $(VERILOG_SRCS)
RTL_SRCS += $(addprefix -I,$(VERILOG_INCS))


## --------------------------------------------------------
## Verilator Flags
## --------------------------------------------------------

CXX_SRCS += $(shell find $(VERIL_DIR) -name "*.cc")

CXX_INCS += $(REPO)/include/generated \
			$(VERIL_DIR)/include \
			$(VERIL_DIR)/include/testbench

VERIL_SRCS += $(CXX_SRCS)

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
	@verilator $(RTL_SRCS) $(VERIL_SRCS) $(C_TARGET) $(VFLAGS) && touch $@

### Lint the RTL
lint: $(VERILOG_SRCS)
	$(info --> Linting RTL)
	@verilator --lint-only $(RTL_OPTS) $(RTL_SRCS)

## --------------------------------------------------------
## Set the test suite and command to run simulation
## --------------------------------------------------------

TEST_SUITES ?= ics2023

### File to store test result
RESULT = $(OUTPUT_DIR)/.result
$(shell mkdir -p $(OUTPUT_DIR))
$(shell > $(RESULT))

### Include the target makefile
include $(VERIL_DIR)/scripts/target.mk

## --------------------------------------------------------
## MISC
## --------------------------------------------------------

sim.clean:
	@rm -rf $(OUTPUT_DIR)
