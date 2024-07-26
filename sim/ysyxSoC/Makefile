# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 07/03/2024
# ------------------------------------------------------------------------------------------------

## Makefile for simulation flow

## --------------------------------------------------------
## Directories
## --------------------------------------------------------

REPO = $(shell git rev-parse --show-toplevel)
OUTPUT_DIR= $(REPO)/output/sim/ysyxSoC
TB_DIR = $(REPO)/sim/ysyxSoC/testbench
INC_DIR = $(REPO)/sim/ysyxSoC/include
BUILD_DIR = $(OUTPUT_DIR)/build
# Note: Change this to the path of the ysyxSoC
YSYX_SOC_DIR = $(REPO)/ysyxSoC

$(mkdir -p $(OUTPUT_DIR))
$(mkdir -p $(BUILD_DIR))

## --------------------------------------------------------
## Tool
## --------------------------------------------------------

CC = gcc
AR = ar

## --------------------------------------------------------
## RTL source files CPP source files
## --------------------------------------------------------

TOP ?= ysyxSoCFull

include $(REPO)/core/src/filelist.mk

VERILOG_SRCS += $(YSYX_SOC_DIR)/build/ysyxSoCFull.v
VERILOG_SRCS += $(shell find $(YSYX_SOC_DIR)/perip -name "*.v")

VERILOG_INCS += $(YSYX_SOC_DIR)/perip/uart16550/rtl
VERILOG_INCS += $(YSYX_SOC_DIR)/perip/spi/rtl

RTL_SRCS += $(VERILOG_SRCS)
RTL_SRCS += $(addprefix -I,$(VERILOG_INCS))

## --------------------------------------------------------
## Verilator Flags
## --------------------------------------------------------

CXX_SRCS += $(shell find $(TB_DIR) -name "*.c")
CXX_SRCS += $(shell find $(TB_DIR) -name "*.cc")

CXX_INCS += $(REPO)/include/generated \
			$(INC_DIR)

VERIL_SRCS += $(CXX_SRCS)

## verilator build flags and options
VFLAGS += --x-assign unique --x-initial unique
VFLAGS += --cc --exe -j 0
VFLAGS += --Mdir $(BUILD_DIR) --top-module $(TOP)
VFLAGS += --trace
VFLAGS += -O3
VFLAGS += -CFLAGS  "$(addprefix -I, $(abspath $(CXX_INCS)))"
VFLAGS += --timescale "1ns/1ns"
VFLAGS += --no-timing
VFLAGS += --autoflush

## --------------------------------------------------------
## build verilator executable
## --------------------------------------------------------

OBJECT = V$(TOP)
VPASS  = $(BUILD_DIR)/.VPASS
BPASS  = $(BUILD_DIR)/.BPASS

### Compile the RTL and TB
verilating: $(VPASS)

$(VPASS): $(VERILOG_SRCS) $(CXX_SRCS) $(C_TARGET) $(CXX_INCS)
	$(info --> Verilatring)
	@mkdir -p $(BUILD_DIR)
	@verilator $(RTL_SRCS) $(VERIL_SRCS) $(C_TARGET) $(VFLAGS) && touch $@

### Build the verilator executable
build: $(OBJECT)

$(OBJECT): $(BPASS)

$(BPASS): $(VPASS)
	$(info --> Building Verilator Executable)
	@rm -f $(BUILD_DIR)/$(OBJECT)
	@$(MAKE) V$(TOP) -C $(BUILD_DIR) -f V$(TOP).mk -s && touch $@

## --------------------------------------------------------
## Others
## --------------------------------------------------------

### Lint the RTL
lint: $(VERILOG_SRCS)
	$(info --> Linting RTL)
	@verilator --lint-only $(RTL_OPTS) $(RTL_SRCS)

sim_ysyx.clean:
	@rm -rf $(OUTPUT_DIR)
