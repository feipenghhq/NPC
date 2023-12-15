# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

# Select Target CPU
CPU ?= CORE_S

ifeq ($(CPU),CORE_S)
TOP = core_s
endif

# Include filelist
include src/filelist.mk

# Include verilator
include scripts/verilator.mk

