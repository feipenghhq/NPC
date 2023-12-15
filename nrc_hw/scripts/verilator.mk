# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

# verilator build flags
CFLAGS += --x-assign unique --x-initial unique
CFLAGS += --cc --exe -j 0

# Directory
WORK_DIR  = $(shell pwd)
BUILD_DIR = $(WORK_DIR)/build

all: run

# Run simulation
run: rtl
	make V$(TOP) -C build -f V$(TOP).mk

# Compile the RTL
rtl: $(BUILD_DIR)/.PASS

$(BUILD_DIR)/.PASS: $(V_SRCS)
	verilator $(CFLAGS) --Mdir $(BUILD_DIR) --top-module $(TOP) $^
	touch $(BUILD_DIR)/.PASS

# Lint the RTL
lint: $(V_SRCS)
	verilator --lint-only --top-module $(TOP) $^

# Clean
.PHONY: clean
clean:
	rm -rf $(BUILD_DIR)

