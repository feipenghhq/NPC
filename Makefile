# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

#------------------------------------------------
# Directory
#------------------------------------------------
WORK_DIR  = $(shell git rev-parse --show-toplevel)
BUILD_DIR = $(WORK_DIR)/build

#------------------------------------------------
# Select Flow and Target CPU Design
#------------------------------------------------

FLOW ?= sim

CPU ?= CORE_S

# Select Different Top based on CPU
ifeq ($(CPU),CORE_S)
TOP = core_s
endif

#------------------------------------------------
# Include RTL filelist
#------------------------------------------------
include $(WORK_DIR)/core/src/rtl/filelist.mk

#------------------------------------------------
# Include flow makefile
#------------------------------------------------
ifeq ($(FLOW),sim)
include $(WORK_DIR)/scripts/verilator.mk
endif

include $(WORK_DIR)/scripts/kconfig.mk

#------------------------------------------------
# List information about the Design and Flow
#------------------------------------------------
ifeq ($(findstring $(MAKECMDGOALS),clean,menuconfig),)
$(info Target CPU: $(TOP))
$(info Running $(FLOW) Flow)
endif

#------------------------------------------------
# Clean
#------------------------------------------------
.PHONY: clean
clean:
	rm -rf $(BUILD_DIR) *.log *.vcd

