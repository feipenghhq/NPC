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

# Select Flow
FLOW ?= sim

# Select Target CPU Design
CPU ?= CORE_S

# Select Different Top based on CPU
ifeq ($(CPU),CORE_S)
TOP = core_s
endif

# Include RTL filelist
include core/src/rtl/filelist.mk

# Include flow makefile
ifeq ($(FLOW),sim)
include sim/verilator/scripts/verilator.mk
endif

# List information about the Design and Flow
ifeq ($(findstring $(MAKECMDGOALS),clean),)
$(info Target CPU: $(TOP))
$(info Running $(FLOW) Flow)
endif

# Clean
.PHONY: clean
clean:
	rm -rf $(BUILD_DIR) *.log *.vcd

