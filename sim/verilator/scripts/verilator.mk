# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------


## --------------------------------------------------------
## 1. Set build flags
## --------------------------------------------------------

### Directory
OUTPUT_DIR = $(BUILD_DIR)/verilator

### Path
VERILATOR_PATH = $(shell realpath sim/verilator)

## verilator build flags and options
VERILATOR_FLAGS += --x-assign unique --x-initial unique
VERILATOR_FLAGS += --cc --exe -j 0
VERILATOR_FLAGS += --Mdir $(OUTPUT_DIR) --top-module $(TOP)
VERILATOR_FLAGS += --trace

### CFLAGS for g++ build in verilator
CFLAGS += -CFLAGS -mcmodel=large
#### for difftest nemu shared lib
CFLAGS += -CFLAGS -lreadline
#### for LLVM disasm
CFLAGS += $(addprefix -CFLAGS ,$(shell llvm-config --cflags))

### LDFLAGS for g++ build in verilator
#### for LLVM disasm
LDFLAGS += $(addprefix -LDFLAGS ,$(shell llvm-config --ldflags --libs))

### Verilator trace
WAVE	 ?= 0
ARG_WAVE ?=
ifeq ($(WAVE),1)
ARG_WAVE = --wave
endif

### Misc config
TEST_NAME_MAX_LEN ?= 10

COLOR_RED   = \033[1;31m
COLOR_GREEN = \033[1;32m
COLOR_NONE  = \033[0m

## --------------------------------------------------------
## 2. RTL source files and C/CPP source files
## --------------------------------------------------------

### RTL source file
RTL_SRCS  += $(VERILOG_SRCS)
RTL_SRCS  += $(addprefix -I,$(VERILOG_INCS))

### C/CPP source file

### Verilator c and cpp source file
C_SRCS   += $(shell find $(VERILATOR_PATH) -name "*.c")
CXX_SRCS += $(shell find $(VERILATOR_PATH) -name "*.cc")

### Verilator include directory
C_INCS    += $(sort $(dir $(shell find $(VERILATOR_PATH) -name "*.h")))

### All C/CPP files
TB_SRCS   += $(C_SRCS)
TB_SRCS   += $(CXX_SRCS)
TB_SRCS   += $(addprefix -CFLAGS -I, $(abspath $(C_INCS)))

## --------------------------------------------------------
## 3. Target and Commands to build executable
## --------------------------------------------------------

### Object
OBJECT = V$(TOP)
VPASS = $(OUTPUT_DIR)/.VPASS
BPASS = $(OUTPUT_DIR)/.BPASS
REF_SO = $(VERILATOR_PATH)/lib/riscv32-nemu-interpreter-so

### Build the Verilator executable
build: $(OBJECT)

$(OBJECT): $(BPASS)

$(BPASS): $(VPASS)
	$(info --> Building Verilator Executable)
	@$(MAKE) V$(TOP) -C $(OUTPUT_DIR) -f V$(TOP).mk -s && touch $@

### Compile the RTL and TB
compile: $(VPASS)

$(VPASS): $(VERILOG_SRCS) $(C_SRCS) $(CXX_SRCS)
	$(info --> Verilatring)
	@mkdir -p $(BUILD_DIR)
	@verilator $(VERILATOR_FLAGS) $(CFLAGS) $(LDFLAGS) $(RTL_SRCS) $(TB_SRCS) && touch $@

### Lint the RTL
lint: $(VERILOG_SRCS)
	$(info --> Linting RTL)
	@verilator --lint-only $(RTL_OPTS) $(RTL_SRCS)

## --------------------------------------------------------
## 4. Set the test suite and command to run simulation
## --------------------------------------------------------

TEST_SUITES ?= ics-am-test

### File to store test result
RESULT = $(OUTPUT_DIR)/.result
$(shell mkdir -p $(OUTPUT_DIR))
$(shell > $(RESULT))

### Define function to run simulation. Usage: $(call run_sim,image,elf,suite,test,dut,maxlen)
define run_sim
	@/bin/echo -e "run:\n\t $(OUTPUT_DIR)/$(OBJECT) \
		--image $(1) --elf $(2) --suite $(3) --test $(4) --dut $(5) $(ARG_WAVE)" \
		--ref $(REF_SO) \
		>> $(OUTPUT_DIR)/makefile.$(4)
	@if make -s -f $(OUTPUT_DIR)/makefile.$(4); then \
		printf "[%$(6)s] $(COLOR_GREEN)%s!$(COLOR_NONE)\n" $(4) PASS >> $(RESULT); \
	else \
		printf "[%$(6)s] $(COLOR_RED)%s!$(COLOR_NONE)\n" $(4) FAIL >> $(RESULT); \
	fi
	@rm $(OUTPUT_DIR)/makefile.$(4)
endef

### Include the test suites makefile
include sim/verilator/scripts/$(TEST_SUITES).mk
