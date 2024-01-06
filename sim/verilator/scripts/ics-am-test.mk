# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 01/01/2023
# -----------------------------------------------------------------------------------------------

# Set the AM (Abstract Machine) directory path. AM is from NJU ICS lab
ifeq ($(AM_KERNELS_HOME),)
$(error "Please set AM_KERNELS_HOME to am-kernels path")
endif

# Set the FCEUX-AM directory path. FCEUX-AM is from NJU ICS lab
ifeq ($(FCEUX_AM_HOME),)
$(error "Please set FCEUX_AM_HOME to fceux-am path")
endif

# Usage: $(call add_tests,name,target,path,maxlen)
define add_tests
$(1) = $(sort $(basename $(notdir $(shell find $(3) -name "*-npc.bin"))))
$$($(1)): $(OBJECT) $(RESULT)
	$(call run_sim,$(3)/build/$$@.bin,$(3)/build/$$@.elf,$(TEST_SUITES),$$@,$(TOP),$(4))
$(2): $$($(1))
	@cat $(RESULT)
endef

# ics am-kernel cpu-test
$(eval $(call add_tests,CPU_TESTS,cpu-test,$(AM_KERNELS_HOME)/tests/cpu-tests,26))

# ics am-kernel am-test
$(eval $(call add_tests,AM_TESTS,am-test,$(AM_KERNELS_HOME)/tests/am-tests,26))

# ics am-kernel alu-test
$(eval $(call add_tests,ALU_TESTS,alu-test,$(AM_KERNELS_HOME)/tests/alu-tests,26))

# ics am-kernel benchmark
$(eval $(call add_tests,COREMARK,coremark,$(AM_KERNELS_HOME)/benchmarks/coremark,26))
$(eval $(call add_tests,DHRYSTONE,dhrystone,$(AM_KERNELS_HOME)/benchmarks/dhrystone,26))
$(eval $(call add_tests,MICROBENCH,microbench,$(AM_KERNELS_HOME)/benchmarks/microbench,26))

# ics am-kernel kernels
$(eval $(call add_tests,DEMO,demo,$(AM_KERNELS_HOME)/kernels/demo,26))
$(eval $(call add_tests,TYPING-GAME,typing-game,$(AM_KERNELS_HOME)/kernels/typing-game,26))

# ics fceux-am
$(eval $(call add_tests,FCEUX,fceux,$(FCEUX_AM_HOME),26))
