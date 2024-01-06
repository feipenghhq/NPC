# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 01/05/2023
# ------------------------------------------------------------------------------------------------

## Makefile for simulation flow

## --------------------------------------------------------
## Directories
## --------------------------------------------------------

OUTPUT_DIR= $(WORK_DIR)/output/sim
BUILD_DIR = $(OUTPUT_DIR)/build
VERIL_DIR = $(WORK_DIR)/sim/verilator

$(mkdir -p $(OUTPUT_DIR))
$(mkdir -p $(BUILD_DIR))

## --------------------------------------------------------
## Tool
## --------------------------------------------------------

CC = gcc
AR = ar

## --------------------------------------------------------
## C source files
## --------------------------------------------------------

C_SRCS += $(shell realpath $(shell find $(VERIL_DIR) -name "*.c") --relative-to .)
C_HDRS += $(shell find $(VERIL_DIR) -name "*.h")

C_INC += $(sort $(dir $(C_HDRS)))
C_INC += include/generated

C_OBJS += $(patsubst %.c,$(BUILD_DIR)/%.o,$(C_SRCS))

## --------------------------------------------------------
## C Build flags
## --------------------------------------------------------

CFLAGS += -g -Wall -O1 -Wextra -rdynamic -MMD
CFLAGS += -mcmodel=large
CFLAGS += $(shell sdl2-config --cflags)
CFLAGS += $(shell llvm-config --cflags)
CFLAGS += $(addprefix -I,$(C_INC))

LDFLAGS += $(shell sdl2-config --libs))
LDFLAGS += $(shell llvm-config --ldflags --libs))
LDFLAGS +=-lreadline

## --------------------------------------------------------
## Target and Commands to build executable
## --------------------------------------------------------

C_TARGET = $(BUILD_DIR)/verilator_c_lib.a

verilator_c_lib.a: $(C_OBJS)
	@echo +AR "->" $(shell realpath $(BUILD_DIR)/$@ --relative-to .)
	@$(AR) rcs $(BUILD_DIR)/$@ $(C_OBJS)

$(BUILD_DIR)/%.o: %.c
	@mkdir -p $(dir $@)
	@echo +CC $<
	@$(CC) $(CFLAGS) -c -o $@ $<

sim.clean:
	@rm -rf $(C_OBJS) $(C_TARGET)
