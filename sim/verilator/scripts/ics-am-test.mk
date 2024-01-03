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
else
AM_KERNELS_PATH = $(AM_KERNELS_HOME)
endif

# Usage: $(call add_tests,name,target,path,maxlen)
define add_tests
$(1) = $(sort $(basename $(notdir $(shell find $(3) -name "*-npc.bin"))))
$$($(1)): $(OBJECT) $(RESULT)
	$(call run_sim,$(3)/$$@.bin,$(3)/$$@.elf,$(TEST_SUITES),$$@,$(TOP),$(4))
$(2): $$($(1))
	@cat $(RESULT)
endef

# ics am-kernel cpu-test
$(eval $(call add_tests,ICS_AM_CPU_TESTS,ics_am_cpu_test,$(AM_KERNELS_PATH)/tests/cpu-tests/build,26))

# ics am-kernel am-test
$(eval $(call add_tests,ICS_AM_AM_TESTS,ics_am_am_test,$(AM_KERNELS_PATH)/tests/am-tests/build,26))

# ics am-kernel alu-test
$(eval $(call add_tests,ICS_AM_AM_TESTS,ics_am_alu_test,$(AM_KERNELS_PATH)/tests/alu-tests/build,26))
