# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

# path
REPO ?= $(shell git rev-parse --show-toplevel)
RTL_PATH = $(REPO)/core
GEN_PATH = $(RTL_PATH)/src/gen

# Verilog source file
ifeq ($(TOP),CoreN)
VERILOG_SRCS += $(RTL_PATH)/src/gen/CoreN.v
VERILOG_SRCS += $(RTL_PATH)/src/verilog/core/CoreNDPI.sv
VERILOG_SRCS += $(RTL_PATH)/src/verilog/misc/SramDpi.sv
endif

$(GEN_PATH)/CoreN.v:
	cd $(RTL_PATH) && sbt "runMain core.CoreNVerilog"
