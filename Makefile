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
MAKE_DIR  = $(WORK_DIR)/scripts/Makefile
PWD_DIR   = $(PWD)

#------------------------------------------------
# Select Flow and Target CPU Design
#------------------------------------------------

FLOW ?= sim

#------------------------------------------------
# Include RTL filelist
#------------------------------------------------
#include $(WORK_DIR)/core/src/rtl/filelist.mk

#------------------------------------------------
# Include flow makefile
#------------------------------------------------

#flow makefile
include $(MAKE_DIR)/flow.$(FLOW).mk

#kconfig flow
include $(MAKE_DIR)/kconfig.mk

#------------------------------------------------
# Clean
#------------------------------------------------
.PHONY: clean
clean: $(FLOW).clean
	rm -rf $(BUILD_DIR) *.log *.vcd

