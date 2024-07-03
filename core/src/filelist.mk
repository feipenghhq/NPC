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
ifeq ($(TOP),CoreNSoC)
VERILOG_SRCS += $(RTL_PATH)/src/gen/CoreNSoC.v
VERILOG_SRCS += $(RTL_PATH)/src/verilog/core/CoreNDpi.sv
VERILOG_SRCS += $(RTL_PATH)/src/verilog/misc/SramDpi.sv
endif

SCALA_SRCS += $(wildcard $(RTL_PATH)/src/spinal/*/*.scala)

$(GEN_PATH)/CoreNSoC.v: $(SCALA_SRCS)
	cd $(RTL_PATH) && sbt "runMain soc.CoreNSoCVerilog"
