# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/16/2023
# ------------------------------------------------------------------------------------------------

# path
VER_PATH = src/sim/verilator

# Verilator cpp source file
CPP_SRCS += $(shell find $(VER_PATH)/env -name "*.cpp")

# Verilator include directory
CPP_INCS += $(VER_PATH)/env

