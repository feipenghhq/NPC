# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

# 1. Set build flags, RTL source files and C/CPP source files

## Directory
VERILATOR_DIR = $(BUILD_DIR)/verilator

## verilator build flags and options
VERILATOR_FLAGS += --x-assign unique --x-initial unique
VERILATOR_FLAGS += --cc --exe -j 0
VERILATOR_FLAGS += --Mdir $(VERILATOR_DIR) --top-module $(TOP)
VERILATOR_FLAGS += --trace

## CFLAGS for g++ build
CFLAGS += -CFLAGS -mcmodel=large

## Include CPP filelist
include src/sim/verilator/filelist.mk

## RTL source file
RTL_SRCS  += $(V_SRCS)
RTL_SRCS  += $(addprefix -I,$(V_INCS))

## TB source file
TB_SRCS   += $(CPP_SRCS)
TB_SRCS   += $(addprefix -CFLAGS -I, $(abspath $(CPP_INCS)))

## Verilator trace
WAVE	 ?= 0
ARG_WAVE ?=
ifeq ($(WAVE),1)
ARG_WAVE = --wave
endif

# File to store test result
RESULT = $(VERILATOR_DIR)/.result
$(shell > $(RESULT))

## Object
OBJECT = V$(TOP)
VPASS = $(VERILATOR_DIR)/.VPASS
BPASS = $(VERILATOR_DIR)/.BPASS

## Terminal color
COLOR_RED   = \033[1;31m
COLOR_GREEN = \033[1;32m
COLOR_NONE  = \033[0m

TEST_NAME_MAX_LEN ?= 10

# 2. Set test suites
TEST_SUITES ?= ics-am-cpu-test

## Include the test suites specific makefile
include src/sim/verilator/scripts/$(TEST_SUITES).mk

# 3. Commands

## Define function to run simulation
### Usage: $(call run_sim,image,suite,test,dut)
define run_sim
	$(info --> Running Test)
	@/bin/echo -e "run:\n\t@cd $(VERILATOR_DIR) && ./$(OBJECT) --image $(1) --suite $(2) --test $(3) --dut $(4) $(ARG_WAVE)" \
		>> $(VERILATOR_DIR)/makefile.$(3)
	@if make -s -f $(VERILATOR_DIR)/makefile.$(3); then \
		printf "[%$(TEST_NAME_MAX_LEN)s] $(COLOR_GREEN)%s!$(COLOR_NONE)\n" $(3) PASS >> $(RESULT); \
	else \
		printf "[%$(TEST_NAME_MAX_LEN)s] $(COLOR_RED)%s!$(COLOR_NONE)\n" $(3) FAIL >> $(RESULT); \
	fi
	@rm $(VERILATOR_DIR)/makefile.$(3)
endef

## Build the Verilator executable
build: $(OBJECT)

$(OBJECT): $(BPASS)

$(BPASS): $(VPASS)
	$(info --> Building Verilator Executable)
	@$(MAKE) V$(TOP) -C $(VERILATOR_DIR) -f V$(TOP).mk -s && touch $@

## Compile the RTL and TB
compile: $(VPASS)

$(VPASS): $(V_SRCS) $(V_INCS) $(CPP_SRCS)
	$(info --> Verilatring)
	@mkdir -p $(BUILD_DIR)
	@verilator $(VERILATOR_FLAGS) $(CFLAGS) $(RTL_SRCS) $(TB_SRCS) && touch $@

## Lint the RTL
lint: $(V_SRCS)
	$(info --> Linting RTL)
	@verilator --lint-only $(RTL_OPTS) $(RTL_SRCS)

## clean
clean_verilator:
	rm -rf $(VERILATOR_DIR)
