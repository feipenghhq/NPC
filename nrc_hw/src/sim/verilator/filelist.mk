# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/16/2023
# ------------------------------------------------------------------------------------------------

# path
VERILATOR_PATH = src/sim/verilator

# Folder
FOLDERS = testbench

# Verilator cpp source file
CPP_SRCS += $(foreach folder,$(FOLDER), $(shell find $(VERILATOR_PATH)/$(foder) -name "*.cpp"))

# main function file
CPP_SRCS += $(VERILATOR_PATH)/tests/$(TOP)_main.cpp

# Verilator include directory
CPP_INCS += $(addprefix $(VERILATOR_PATH)/,$(FOLDERS))

