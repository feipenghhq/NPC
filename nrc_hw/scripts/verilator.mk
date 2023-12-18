# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

# Directory
WORK_DIR  = $(shell pwd)
BUILD_DIR = $(WORK_DIR)/build

# verilator build flags and options
VERILATOR_FLAGS += --x-assign unique --x-initial unique
VERILATOR_FLAGS += --cc --exe -j 0
VERILATOR_FLAGS += --Mdir $(BUILD_DIR) --top-module $(TOP)

# Include CPP filelist
include src/sim/verilator/filelist.mk

# RTL source file
RTL_SRCS  += $(V_SRCS)
RTL_SRCS  += $(addprefix -I,$(V_INCS))

# TB source file
TB_SRCS   += $(CPP_SRCS)
CFLAGS    += $(addprefix -I, $(abspath $(CPP_INCS)))

all: run

# Run simulation
run: compile
	make V$(TOP) -C build -f V$(TOP).mk

# Compile the RTL and TB
compile: $(BUILD_DIR)/.PASS

$(BUILD_DIR)/.PASS: $(V_SRCS) $(V_INCS) $(CPP_SRC)
	verilator $(VERILATOR_FLAGS) -CFLAGS $(CFLAGS) $(RTL_SRCS) $(TB_SRCS) && touch $(BUILD_DIR)/.PASS

# Lint the RTL
lint: $(V_SRCS)
	verilator --lint-only $(RTL_OPTS) $(VERILATOR_SRCS)

# Clean
.PHONY: clean
clean:
	rm -rf $(BUILD_DIR)

