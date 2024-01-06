# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

# path
RTL_PATH = core/src/rtl

# Verilog source file
ifeq ($(TOP),core_s)
VERILOG_SRCS += $(wildcard $(RTL_PATH)/core_s/*.sv $(RTL_PATH)/core_s/*.v)
endif

# Verilog include directory
VERILOG_INCS += $(RTL_PATH)/include

