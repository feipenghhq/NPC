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

VER_OPTS  += --Mdir $(BUILD_DIR) --top-module $(TOP)
VER_SRCS  += $(V_SRCS)
VER_SRCS  += $(addprefix -I,$(V_INCS))

all: run

# Run simulation
run: rtl
	make V$(TOP) -C build -f V$(TOP).mk

# Compile the RTL
rtl: $(BUILD_DIR)/.PASS

$(BUILD_DIR)/.PASS: $(V_SRCS) $(V_INCS)
	verilator $(CFLAGS) $(VER_OPTS) $(VER_SRCS) && touch $(BUILD_DIR)/.PASS

# Lint the RTL
lint: $(V_SRCS)
	verilator --lint-only $(VER_OPTS) $(VER_SRCS)

# Clean
.PHONY: clean
clean:
	rm -rf $(BUILD_DIR)

