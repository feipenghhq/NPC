# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/16/2023
# ------------------------------------------------------------------------------------------------

# path
_PATH = $(shell realpath src/sim/verilator)

# Folder
FOLDERS = testbench

# Verilator cpp source file
CPP_SRCS += $(foreach folder,$(FOLDER), $(shell find $(_PATH)/$(foder) -name "*.cpp"))

# main function file
CPP_SRCS += $(_PATH)/tests/$(TOP)_main.cpp

# Verilator include directory
CPP_INCS += $(addprefix $(_PATH)/,$(FOLDERS))

