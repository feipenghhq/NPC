# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

#------------------------------------------------
# Help message
#------------------------------------------------
help:
	@echo "This is the main Makefile for the repo. It is used to run all the supported flows and designs."
	@echo "To run simulation: "

#------------------------------------------------
# Directory
#------------------------------------------------
WORK_DIR  = $(shell git rev-parse --show-toplevel)
MAKE_DIR  = $(WORK_DIR)/scripts
SIM_ICS_PA_DIR = $(WORK_DIR)/sim/ics-pa
SIM_YSYX_DIR = $(WORK_DIR)/sim/ysyxSoC
PWD_DIR   = $(PWD)

#------------------------------------------------
# Select Flow and Target Design
#------------------------------------------------

#FLOW ?= sim_ics_pa
FLOW ?= sim_ysyx

#------------------------------------------------
# Include flow makefile
#------------------------------------------------

#kconfig flow
include $(MAKE_DIR)/kconfig.mk

#------------------------------------------------
# Different Flow
#------------------------------------------------

# Simulating the cpu core using ICS PA
ifeq ($(FLOW), sim_ics_pa)
TOP  ?= CoreNSoC
include $(SIM_ICS_PA_DIR)/Makefile
endif

# Simulating the ysyx SoC
ifeq ($(FLOW), sim_ysyx)
TOP  ?= ysyxSoCFull
include $(SIM_YSYX_DIR)/Makefile
endif

#------------------------------------------------
# Clean
#------------------------------------------------
.PHONY: clean
clean: $(FLOW).clean
	rm -rf $(BUILD_DIR) *.log *.vcd

