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
PWD_DIR   = $(PWD)
SIM_ICS_PA_DIR = $(WORK_DIR)/sim/ics-pa

#------------------------------------------------
# Select Flow and Target CPU Design
#------------------------------------------------

FLOW ?= sim
TOP  ?= CoreNSoC

#------------------------------------------------
# Include flow makefile
#------------------------------------------------

#kconfig flow
include $(MAKE_DIR)/kconfig.mk

#flow
ifeq ($(FLOW), sim)
include $(SIM_ICS_PA_DIR)/Makefile
endif

#------------------------------------------------
# Clean
#------------------------------------------------
.PHONY: clean
clean: $(FLOW).clean
	rm -rf $(BUILD_DIR) *.log *.vcd

