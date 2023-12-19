# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/14/2023
# ------------------------------------------------------------------------------------------------

# Directory
VERILATOR_DIR = $(BUILD_DIR)/verilator

# verilator build flags and options
VERILATOR_FLAGS += --x-assign unique --x-initial unique
VERILATOR_FLAGS += --cc --exe -j 0
VERILATOR_FLAGS += --Mdir $(VERILATOR_DIR) --top-module $(TOP)

# CFLAGS for g++ build
CFLAGS += -CFLAGS -mcmodel=large

# Include CPP filelist
include src/sim/verilator/filelist.mk

# RTL source file
RTL_SRCS  += $(V_SRCS)
RTL_SRCS  += $(addprefix -I,$(V_INCS))

# TB source file
TB_SRCS   += $(CPP_SRCS)
TB_SRCS   += $(addprefix -CFLAGS -I, $(abspath $(CPP_INCS)))

# Object
OBJECT = V$(TOP)

all: run

# Run simulation
run: $(OBJECT)
	$(info --> Running Test)
	@$(VERILATOR_DIR)/$(OBJECT)

# Build the Verilator executable
build: $(OBJECT)

$(OBJECT): $(BUILD_DIR)/.COMPILE_PASS
	$(info --> Building Verilator Executable)
	@$(MAKE) V$(TOP) -C $(VERILATOR_DIR) -f V$(TOP).mk -s

# Compile the RTL and TB
compile: $(BUILD_DIR)/.COMPILE_PASS

$(BUILD_DIR)/.COMPILE_PASS: $(V_SRCS) $(V_INCS) $(CPP_SRC)
	$(info --> Verilatring)
	@mkdir -p $(BUILD_DIR)
	@verilator $(VERILATOR_FLAGS) $(CFLAGS) $(RTL_SRCS) $(TB_SRCS) && touch $(VERILATOR_DIR)/.COMPILE_PASS

# Lint the RTL
lint: $(V_SRCS)
	$(info --> Linting RTL)
	@verilator --lint-only $(RTL_OPTS) $(RTL_SRCS)

clean_verilator:
	rm -rf $(VERILATOR_DIR)
