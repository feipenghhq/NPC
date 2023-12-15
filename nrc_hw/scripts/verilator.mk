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
CFLAGS += --x-assign unique --x-initial unique
CFLAGS += --cc --exe -j 0

VERILATOR_OPTS  += --Mdir $(BUILD_DIR) --top-module $(TOP)
VERILATOR_SRCS  += $(V_SRCS)
VERILATOR_SRCS  += $(addprefix -I,$(V_INCS))

all: run

# Run simulation
run: rtl
	make V$(TOP) -C build -f V$(TOP).mk

# Compile the RTL
rtl: $(BUILD_DIR)/.PASS

$(BUILD_DIR)/.PASS: $(V_SRCS) $(V_INCS)
	verilator $(CFLAGS) $(VERILATOR_OPTS) $(VERILATOR_SRCS) && touch $(BUILD_DIR)/.PASS

# Lint the RTL
lint: $(V_SRCS)
	verilator --lint-only $(VERILATOR_OPTS) $(VERILATOR_SRCS)

# Clean
.PHONY: clean
clean:
	rm -rf $(BUILD_DIR)

