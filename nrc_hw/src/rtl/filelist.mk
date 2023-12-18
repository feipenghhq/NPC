# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

# path
RTL_PATH = src/rtl

# verilog source file
V_SRCS-CORE_S += $(wildcard $(RTL_PATH)/core_s/*.sv $(RTL_PATH)/core_s/*.v)

# verilog include directory
V_INCS += $(RTL_PATH)/include

ifeq ($(CPU),CORE_S)
$(info Target CPU: core_s)
V_SRCS += $(V_SRCS-CORE_S)
endif

