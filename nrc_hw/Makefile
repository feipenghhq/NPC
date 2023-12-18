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

# Select Target CPU
CPU ?= CORE_S

ifeq ($(CPU),CORE_S)
TOP = core_s
endif

# Include RTL filelist
include src/rtl/filelist.mk

# Include verilator
ifeq ($(FLOW),sim)
include scripts/verilator.mk
endif


ifeq ($(findstring $(MAKECMDGOALS),clean),)
$(info Target CPU: $(TOP))
$(info Running $(FLOW) Flow)
endif


# Clean
.PHONY: clean
clean:
	rm -rf $(BUILD_DIR)

