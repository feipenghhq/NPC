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
MAKE_DIR  = $(WORK_DIR)/scripts
VERIL_DIR = $(WORK_DIR)/sim/verilator
PWD_DIR   = $(PWD)

#------------------------------------------------
# Select Flow and Target CPU Design
#------------------------------------------------

FLOW ?= sim
TOP  ?= CoreN

#------------------------------------------------
# Include flow makefile
#------------------------------------------------

#kconfig flow
include $(MAKE_DIR)/kconfig.mk

#flow
ifeq ($(FLOW), sim)
include $(VERIL_DIR)/Makefile
endif

#------------------------------------------------
# Clean
#------------------------------------------------
.PHONY: clean
clean: $(FLOW).clean
	rm -rf $(BUILD_DIR) *.log *.vcd

